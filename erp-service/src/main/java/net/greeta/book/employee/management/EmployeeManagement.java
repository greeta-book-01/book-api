package net.greeta.book.employee.management;

import net.greeta.book.OrganizationRemoveEvent;
import net.greeta.book.employee.EmployeeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import net.greeta.book.employee.EmployeeExternalAPI;
import net.greeta.book.employee.EmployeeInternalAPI;
import net.greeta.book.employee.mapper.EmployeeMapper;
import net.greeta.book.employee.model.Employee;
import net.greeta.book.employee.repository.EmployeeRepository;

import java.util.List;

@Service
public class EmployeeManagement implements EmployeeInternalAPI, EmployeeExternalAPI {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeManagement.class);
    private EmployeeRepository repository;
    private EmployeeMapper mapper;

    public EmployeeManagement(EmployeeRepository repository,
                              EmployeeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<EmployeeDTO> getEmployeesByDepartmentId(Long departmentId) {
        return repository.findByDepartmentId(departmentId);
    }

    @Override
    public List<EmployeeDTO> getEmployeesByOrganizationId(Long id) {
        return repository.findByOrganizationId(id);
    }

    @Override
    @Transactional
    public EmployeeDTO add(EmployeeDTO employee) {
        Employee emp = mapper.employeeDTOToEmployee(employee);
        return mapper.employeeToEmployeeDTO(repository.save(emp));
    }

    @ApplicationModuleListener
    void onRemovedOrganizationEvent(OrganizationRemoveEvent event) {
        LOG.info("onRemovedOrganizationEvent(orgId={})", event.getId());
        repository.deleteByOrganizationId(event.getId());
    }

}
