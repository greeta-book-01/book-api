package net.greeta.book.employee.repository;

import net.greeta.book.employee.EmployeeDTO;
import org.springframework.data.repository.CrudRepository;
import net.greeta.book.employee.model.Employee;

import java.util.List;

public interface EmployeeRepository extends CrudRepository<Employee, Long> {

    List<EmployeeDTO> findByDepartmentId(Long departmentId);
    List<EmployeeDTO> findByOrganizationId(Long organizationId);
    void deleteByOrganizationId(Long organizationId);

}
