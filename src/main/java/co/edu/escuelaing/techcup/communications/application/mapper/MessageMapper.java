package co.edu.escuelaing.techcup.communications.application.mapper;

import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.MessageResponse;
import co.edu.escuelaing.techcup.communications.domain.model.Message;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageResponse toResponse(Message message);
}
