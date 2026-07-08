package co.edu.escuelaing.techcup.communications.mapper;

import co.edu.escuelaing.techcup.communications.dto.MessageResponse;
import co.edu.escuelaing.techcup.communications.dto.SendMessageRequest;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.service.command.SendMessageCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageResponse toResponse(Message message);

    SendMessageCommand toCommand(SendMessageRequest request);
}
