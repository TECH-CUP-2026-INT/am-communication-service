package co.edu.escuelaing.techcup.communications.mapper;

import co.edu.escuelaing.techcup.communications.dto.ReportedMessageResponse;
import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    ReportedMessageResponse toResponse(ReportedMessage report);
}
