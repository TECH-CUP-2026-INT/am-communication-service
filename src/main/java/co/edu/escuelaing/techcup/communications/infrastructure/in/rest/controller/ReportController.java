package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller;

import co.edu.escuelaing.techcup.communications.infrastructure.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ReportedMessageResponse;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.ResolveReportRequest;
import co.edu.escuelaing.techcup.communications.application.mapper.ReportMapper;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.ResolveReportUseCase;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ResolveReportCommand;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller.swagger.ReportControllerSwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController implements ReportControllerSwagger {

    private final ResolveReportUseCase resolveReportUseCase;
    private final ReportMapper reportMapper;

    @Override
    @PostMapping("/{id}/resolve")
    public ResponseEntity<ReportedMessageResponse> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveReportRequest request,
            @AuthenticationPrincipal AuthenticatedUser caller) {

        ReportedMessage report = resolveReportUseCase.resolve(
                new ResolveReportCommand(
                        id,
                        caller.userId(),
                        request.resolutionStatus(),
                        request.note(),
                        request.actionType()
                ));

        return ResponseEntity.ok(
                reportMapper.toResponse(report)
        );
    }
}
