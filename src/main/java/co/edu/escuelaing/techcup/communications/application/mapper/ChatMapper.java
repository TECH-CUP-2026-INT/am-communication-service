package co.edu.escuelaing.techcup.communications.application.mapper;

import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ChatResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.CreateChatRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.ParticipantRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ParticipantResponse;
import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.Participant;
import co.edu.escuelaing.techcup.communications.application.usecase.command.CreateChatCommand;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ParticipantCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    ChatResponse toResponse(Chat chat);

    ParticipantResponse toResponse(Participant participant);

    CreateChatCommand toCommand(CreateChatRequest request);

    ParticipantCommand toCommand(ParticipantRequest request);
}
