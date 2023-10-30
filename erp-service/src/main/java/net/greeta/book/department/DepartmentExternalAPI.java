package net.greeta.book.department;

public interface DepartmentExternalAPI {

    DepartmentDTO getDepartmentByIdWithEmployees(Long id);
    DepartmentDTO add(DepartmentDTO department);
}
