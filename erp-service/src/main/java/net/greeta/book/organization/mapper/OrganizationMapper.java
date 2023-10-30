package net.greeta.book.organization.mapper;

import net.greeta.book.organization.model.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import net.greeta.book.organization.OrganizationDTO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrganizationMapper {
    OrganizationDTO organizationToOrganizationDTO(Organization organization);
    Organization organizationDTOToOrganization(OrganizationDTO organizationDTO);
}
