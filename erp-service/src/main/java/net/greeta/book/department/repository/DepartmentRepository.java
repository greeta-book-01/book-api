package net.greeta.book.department.repository;

import net.greeta.book.department.model.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import net.greeta.book.department.DepartmentDTO;

import java.util.List;

public interface DepartmentRepository extends CrudRepository<Department, Long> {

    @Query("""
           SELECT new net.greeta.book.department.DepartmentDTO(d.id, d.organizationId, d.name)
           FROM Department d
           WHERE d.id = :id
           """)
    DepartmentDTO findDTOById(Long id);

    @Query("""
           SELECT new net.greeta.book.department.DepartmentDTO(d.id, d.organizationId, d.name)
           FROM Department d
           WHERE d.organizationId = :organizationId
           """)
    List<DepartmentDTO> findByOrganizationId(Long organizationId);

    void deleteByOrganizationId(Long organizationId);
}
