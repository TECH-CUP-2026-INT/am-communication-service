package co.edu.escuelaing.techcup.communications.mapper;

import co.edu.escuelaing.techcup.communications.dto.ChatResponse;
import co.edu.escuelaing.techcup.communications.dto.CreateChatRequest;
import co.edu.escuelaing.techcup.communications.dto.ParticipantRequest;
import co.edu.escuelaing.techcup.communications.dto.ParticipantResponse;
import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Participant;
import co.edu.escuelaing.techcup.communications.service.command.CreateChatCommand;
import co.edu.escuelaing.techcup.communications.service.command.ParticipantCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    ChatResponse toResponse(Chat chat);

    ParticipantResponse toResponse(Participant participant);

    CreateChatCommand toCommand(CreateChatRequest request);

    ParticipantCommand toCommand(ParticipantRequest request);
}
