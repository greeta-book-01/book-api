package net.greeta.book.organization;

import net.greeta.book.department.DepartmentDTO;
import net.greeta.book.employee.EmployeeDTO;

import java.util.ArrayList;
import java.util.List;

public record OrganizationDTO(Long id,
                              String name,
                              String address,
                              List<DepartmentDTO> departments,
                              List<EmployeeDTO> employees) {
    public OrganizationDTO(Long id, String name, String address) {
        this(id, name, address, new ArrayList<>(), new ArrayList<>());
    }
}
