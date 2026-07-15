package co.edu.escuelaing.techcup.communications.application.mapper;

import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.FaqRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.FaqResponse;
import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import co.edu.escuelaing.techcup.communications.application.usecase.command.CreateFaqCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FaqMapper {

    FaqResponse toResponse(Faq faq);

    CreateFaqCommand toCreateCommand(FaqRequest request);
}
