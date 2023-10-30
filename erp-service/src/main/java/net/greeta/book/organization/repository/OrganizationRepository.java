package net.greeta.book.organization.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import net.greeta.book.organization.OrganizationDTO;
import net.greeta.book.organization.model.Organization;

public interface OrganizationRepository extends CrudRepository<Organization, Long> {

    @Query("""
           SELECT new net.greeta.book.organization.OrganizationDTO(o.id, o.name, o.address)
           FROM Organization o
           WHERE o.id = :id
           """)
    OrganizationDTO findDTOById(Long id);
}
