package co.edu.escuelaing.techcup.communications.mapper;

import co.edu.escuelaing.techcup.communications.dto.MessageResponse;
import co.edu.escuelaing.techcup.communications.entity.Message;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageResponse toResponse(Message message);
}
