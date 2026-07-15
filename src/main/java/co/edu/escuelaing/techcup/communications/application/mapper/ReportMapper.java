package co.edu.escuelaing.techcup.communications.application.mapper;

import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ReportedMessageResponse;
import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    ReportedMessageResponse toResponse(ReportedMessage report);
}
