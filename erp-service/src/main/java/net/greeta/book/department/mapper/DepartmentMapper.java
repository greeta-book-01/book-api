package net.greeta.book.department.mapper;

import net.greeta.book.department.model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import net.greeta.book.department.DepartmentDTO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepartmentMapper {
    DepartmentDTO departmentToEmployeeDTO(Department department);
    Department departmentDTOToEmployee(DepartmentDTO departmentDTO);
}
