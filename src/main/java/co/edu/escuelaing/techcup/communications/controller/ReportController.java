package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.config.AuthenticatedUser;
import co.edu.escuelaing.techcup.communications.dto.ReportedMessageResponse;
import co.edu.escuelaing.techcup.communications.dto.ResolveReportRequest;
import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import co.edu.escuelaing.techcup.communications.mapper.ReportMapper;
import co.edu.escuelaing.techcup.communications.service.ResolveReportUseCase;
import co.edu.escuelaing.techcup.communications.service.command.ResolveReportCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ResolveReportUseCase resolveReportUseCase;
    private final ReportMapper reportMapper;

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ReportedMessageResponse> resolve(@PathVariable UUID id,
                                                           @Valid @RequestBody ResolveReportRequest request,
                                                           @AuthenticationPrincipal AuthenticatedUser caller) {
        ReportedMessage report = resolveReportUseCase.resolve(new ResolveReportCommand(
                id,
                caller.userId(),
                request.resolutionStatus(),
                request.note(),
                request.actionType()));
        return ResponseEntity.ok(reportMapper.toResponse(report));
    }
}
