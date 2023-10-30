package net.greeta.book.organization.management;

import net.greeta.book.OrganizationAddEvent;
import net.greeta.book.OrganizationRemoveEvent;
import net.greeta.book.employee.EmployeeDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import net.greeta.book.department.DepartmentDTO;
import net.greeta.book.department.DepartmentInternalAPI;
import net.greeta.book.employee.EmployeeInternalAPI;
import net.greeta.book.organization.OrganizationDTO;
import net.greeta.book.organization.OrganizationExternalAPI;
import net.greeta.book.organization.mapper.OrganizationMapper;
import net.greeta.book.organization.repository.OrganizationRepository;

import java.util.List;

@Service
public class OrganizationManagement implements OrganizationExternalAPI {

    private final ApplicationEventPublisher events;
    private final OrganizationRepository repository;
    private final DepartmentInternalAPI departmentInternalAPI;
    private final EmployeeInternalAPI employeeInternalAPI;
    private final OrganizationMapper mapper;

    public OrganizationManagement(ApplicationEventPublisher events,
                                  OrganizationRepository repository,
                                  DepartmentInternalAPI departmentInternalAPI,
                                  EmployeeInternalAPI employeeInternalAPI,
                                  OrganizationMapper mapper) {
        this.events = events;
        this.repository = repository;
        this.departmentInternalAPI = departmentInternalAPI;
        this.employeeInternalAPI = employeeInternalAPI;
        this.mapper = mapper;
    }

    @Override
    public OrganizationDTO findByIdWithEmployees(Long id) {
        OrganizationDTO dto = repository.findDTOById(id);
        List<EmployeeDTO> dtos = employeeInternalAPI.getEmployeesByOrganizationId(id);
        dto.employees().addAll(dtos);
        return dto;
    }

    @Override
    public OrganizationDTO findByIdWithDepartments(Long id) {
        OrganizationDTO dto = repository.findDTOById(id);
        List<DepartmentDTO> dtos = departmentInternalAPI.getDepartmentsByOrganizationId(id);
        dto.departments().addAll(dtos);
        return dto;
    }

    @Override
    public OrganizationDTO findByIdWithDepartmentsAndEmployees(Long id) {
        OrganizationDTO dto = repository.findDTOById(id);
        List<DepartmentDTO> dtos = departmentInternalAPI.getDepartmentsByOrganizationIdWithEmployees(id);
        dto.departments().addAll(dtos);
        return dto;
    }

    @Override
    @Transactional
    public OrganizationDTO add(OrganizationDTO organization) {
        OrganizationDTO dto = mapper.organizationToOrganizationDTO(
                repository.save(mapper.organizationDTOToOrganization(organization))
        );
        events.publishEvent(new OrganizationAddEvent(dto.id()));
        return dto;
    }

    @Override
    @Transactional
    public void remove(Long id) {
        repository.deleteById(id);
        events.publishEvent(new OrganizationRemoveEvent(id));
    }

}
