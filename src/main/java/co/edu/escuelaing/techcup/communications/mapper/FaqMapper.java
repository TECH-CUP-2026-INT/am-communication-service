package co.edu.escuelaing.techcup.communications.mapper;

import co.edu.escuelaing.techcup.communications.dto.FaqRequest;
import co.edu.escuelaing.techcup.communications.dto.FaqResponse;
import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.service.command.CreateFaqCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FaqMapper {

    FaqResponse toResponse(Faq faq);

    CreateFaqCommand toCreateCommand(FaqRequest request);
}
