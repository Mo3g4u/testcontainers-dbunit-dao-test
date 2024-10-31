package org.example.company;

import java.time.LocalDate;
import java.util.Objects;

/*
 * 社員を表すDTO
 */
public class Employee {
    // 社員ID
    private Integer employeeId;
    // 社員名
    private String employeeName;
    // 部署名
    private String departmentName;
    // 入社年月日
    private LocalDate entranceDate;
    // 役職名
    private String jobName;
    // 月給
    private Integer salary;

    // 引数なしのコンストラクタ
    public Employee() {
    }

    // コンストラクタ
    public Employee(Integer employeeId, String employeeName, String departmentName,
                    LocalDate entranceDate, String jobName, Integer salary) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.departmentName = departmentName;
        this.entranceDate = entranceDate;
        this.jobName = jobName;
        this.salary = salary;
    }

    // アクセサメソッド
    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public LocalDate getEntranceDate() {
        return entranceDate;
    }

    public void setEntranceDate(LocalDate entranceDate) {
        this.entranceDate = entranceDate;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "Employee [employeeId=" + employeeId + ", employeeName=" + employeeName
                + ", departmentName=" + departmentName + ", entranceDate=" + entranceDate
                + ", jobName=" + jobName + ", salary=" + salary + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(departmentName, employeeId, employeeName, entranceDate, jobName,
                salary);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Employee other = (Employee) obj;
        return Objects.equals(departmentName, other.departmentName)
                && Objects.equals(employeeId, other.employeeId)
                && Objects.equals(employeeName, other.employeeName)
                && Objects.equals(entranceDate, other.entranceDate)
                && Objects.equals(jobName, other.jobName) && Objects.equals(salary, other.salary);
    }
}