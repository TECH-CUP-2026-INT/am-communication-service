package co.edu.escuelaing.techcup.communications.application.mapper;

import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.SupportTicketResponse;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupportMapper {

    SupportTicketResponse toResponse(SupportTicket ticket);
}
