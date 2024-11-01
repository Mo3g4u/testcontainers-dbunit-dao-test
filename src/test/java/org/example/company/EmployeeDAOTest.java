package org.example.company;

import static org.dbunit.Assertion.assertEquals; // これを先にimportすることが重要！
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@DisplayName("EmployeeDAOを対象にしたテストクラス")
public class EmployeeDAOTest {
    // 初期スキーマを格納するディレクトリ
    private static final String SCHEMA_DIR = "SCHEMA";
    // DBUnitのリソースを格納するディレクトリ
    private static final String INIT_DATA_DIR = "src/test/resources/INIT_DATA";
    private static final String EXPECTED_DATA_DIR_1 = "src/test/resources/EXPECTED_DATA_1";
    private static final String EXPECTED_DATA_DIR_2 = "src/test/resources/EXPECTED_DATA_2";
    private static final String EXPECTED_DATA_DIR_3 = "src/test/resources/EXPECTED_DATA_3";

    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withInitScripts(
                    SCHEMA_DIR + "/1_COMPANY_DROP.sql",
                    SCHEMA_DIR + "/2_COMPANY_DDL.sql",
                    SCHEMA_DIR + "/3_COMPANY_DML.sql"
            );

    // テスト対象クラス
    EmployeeDAO employeeDAO;

    // DBUnitのためのテストフィクスチャ
    IDatabaseTester databaseTester;
    IDatabaseConnection databaseConnection;

    @BeforeAll
    static void beforeAll() {
        mysql.start();
    }

    @AfterAll
    static void afterAll() {
        mysql.stop();
    }

    /*
     *  テスト毎の DBUnit のテストフィクスチャやデータベース上のデータを初期化する
     */
    @BeforeEach
    void setUpDatabase() throws Exception {
        String driver = mysql.getDriverClassName();
        String url = mysql.getJdbcUrl();
        String user = mysql.getUsername();
        String password = mysql.getPassword();

        // IDatabaseTesterを初期化する
        databaseTester = new JdbcDatabaseTester(driver, url, user, password);

        // IDatabaseConnectionを取得する
        databaseConnection = databaseTester.getConnection();

        // MySQL用のDataTypeFactoryを設定する
        DatabaseConfig config = databaseConnection.getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());

        // IDatabaseConnectionからJDBCコネクションを取り出し、EmployeeDAOを初期化する
        employeeDAO = new EmployeeDAO(databaseConnection.getConnection());

        // 初期データをセットアップする
        initData();
    }

    /*
     * 初期データをセットアップする
     */
    private void initData() throws Exception {
        // CSVファイルから初期データを読み込む
        IDataSet dataSet = new CsvDataSet(new File(INIT_DATA_DIR));

        // データベースの対象テーブルから全件削除し、読み込んだ初期データを挿入する
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
    }


    @Test
    @DisplayName("主キー検索の結果をテストする")
    void test_SelectEmployee() {
        // テストを実行し、実測値を取得する
        Employee actual = employeeDAO.selectEmployee(10001);

        // 期待値を生成する
        Employee expected = new Employee(10001, "Alice", "SALES" ,LocalDate.of(2015, 4, 1),
                "MANAGER", 500000);

        // 期待値と実測値が一致しているかを検証する
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("条件検索の結果をテストする")
    void test_SelectEmployeesBySalary() throws Exception {
        // テストを実行し、実測値リストを取得する
        List<Employee> actualList = employeeDAO.selectEmployeesBySalary(300000, 400000);

        // 期待値リストのサイズと実測値リストのサイズが一致しているかを検証する
        assertEquals(3, actualList.size());

        // 期待値リストを生成する
        List<Employee> expectedList = List.of(
                new Employee(10003, "Carol", "HR" ,LocalDate.of(2015, 4, 1), "CHIEF", 350000),
                new Employee(10004, "Dave", "SALES" ,LocalDate.of(2015, 4, 1), "LEADER", 400000),
                new Employee(10005, "Ellen", "SALES" ,LocalDate.of(2017, 4, 1), "CHIEF", 300000));

        // 実測値リストをソートする
        Collections.sort(actualList, (e1, e2) -> {
            if (e1.getEmployeeId() < e2.getEmployeeId()) return -1;
            if (e1.getEmployeeId() > e2.getEmployeeId()) return 1;
            return 0;
        });

        // 期待値リストと実測値リストが一致しているかを検証する
        assertIterableEquals(expectedList, actualList);
    }

    @Test
    @DisplayName("挿入の結果をテストする")
    void test_InsertEmployee() throws Exception {
        // テストを実行する
        Employee employee = new Employee(10006, "Frank", "SALES", LocalDate.of(2019, 10, 1),
                null, 380000);
        employeeDAO.insertEmployee(employee);

        // DBUnitのAPIで、期待値テーブルをCSVファイルから取得する
        IDataSet expectedDataSet = new CsvDataSet(new File(EXPECTED_DATA_DIR_1));
        ITable expectedTable = expectedDataSet.getTable("EMPLOYEE");

        // DBUnitのAPIで、実測値テーブルをデータベースから取得する
        IDataSet databaseDataSet = databaseConnection.createDataSet();
        ITable actualTable = databaseDataSet.getTable("EMPLOYEE");

        // DBUnitのAPIで、期待値テーブルと実測値テーブルが一致しているかを検証する
        assertEquals(expectedTable, actualTable);
    }

    @Test
    @DisplayName("削除の結果をテストする")
    void test_DeleteEmployee() throws Exception {
        // テストを実行する
        employeeDAO.deleteEmployee(10004);

        // DBUnitのAPIで、期待値テーブルをCSVファイルから取得する
        IDataSet expectedDataSet = new CsvDataSet(new File(EXPECTED_DATA_DIR_2));
        ITable expectedTable = expectedDataSet.getTable("EMPLOYEE");

        // DBUnitのAPIで、実測値テーブルをデータベースから取得する
        IDataSet databaseDataSet = databaseConnection.createDataSet();
        ITable actualTable = databaseDataSet.getTable("EMPLOYEE");

        // DBUnitのAPIで、期待値テーブルと実測値テーブルが一致しているかを検証する
        assertEquals(expectedTable, actualTable);
    }

    @Test
    @DisplayName("一括更新の結果をテストする")
    void test_UpdateSalary() throws Exception {
        // テストを実行する
        employeeDAO.updateEmployeeSalary("SALES", 3000);

        // DBUnitのAPIで、期待値テーブルをCSVファイルから取得する
        IDataSet expectedDataSet = new CsvDataSet(new File(EXPECTED_DATA_DIR_3));
        ITable expectedTable = expectedDataSet.getTable("EMPLOYEE");

        // DBUnitのAPIで、実測値テーブルをデータベースから取得する
        IDataSet databaseDataSet = databaseConnection.createDataSet();
        ITable actualTable = databaseDataSet.getTable("EMPLOYEE");

        // DBUnitのAPIで、期待値テーブルと実測値テーブルが一致しているかを検証する
        assertEquals(expectedTable, actualTable);
    }
}