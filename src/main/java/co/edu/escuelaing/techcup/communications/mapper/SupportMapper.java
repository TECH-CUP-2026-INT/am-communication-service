package co.edu.escuelaing.techcup.communications.mapper;

import co.edu.escuelaing.techcup.communications.dto.SupportTicketResponse;
import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupportMapper {

    SupportTicketResponse toResponse(SupportTicket ticket);
}
