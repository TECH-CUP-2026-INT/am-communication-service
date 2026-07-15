package co.edu.escuelaing.techcup.communications.infrastructure.config;

import co.edu.escuelaing.techcup.communications.domain.exception.ChatClosedException;
import co.edu.escuelaing.techcup.communications.domain.exception.ChatNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.DomainException;
import co.edu.escuelaing.techcup.communications.domain.exception.FaqNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.domain.exception.MessageAlreadyReportedException;
import co.edu.escuelaing.techcup.communications.domain.exception.MessageNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.ParticipantNotAllowedException;
import co.edu.escuelaing.techcup.communications.domain.exception.ReportedMessageNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.SupportTicketNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.TeamNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.exception.UserAccessNotAllowedException;
import co.edu.escuelaing.techcup.communications.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            ChatNotFoundException.class,
            MessageNotFoundException.class,
            SupportTicketNotFoundException.class,
            ReportedMessageNotFoundException.class,
            FaqNotFoundException.class,
            UserNotFoundException.class,
            TeamNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(DomainException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ErrorResponse> handleIntegration(IntegrationException ex, HttpServletRequest request) {
        log.error("Downstream microservice call failed", ex);
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler({ParticipantNotAllowedException.class, UserAccessNotAllowedException.class})
    public ResponseEntity<ErrorResponse> handleForbidden(DomainException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler({
            ChatClosedException.class,
            MessageAlreadyReportedException.class,
            InvalidChatOperationException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(DomainException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                spanishReason(HttpStatus.BAD_REQUEST),
                "Error de validación: revise los campos indicados",
                request.getRequestURI(),
                fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(status.value(), spanishReason(status), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    static String spanishReason(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Solicitud incorrecta";
            case UNAUTHORIZED -> "No autorizado";
            case FORBIDDEN -> "Acceso denegado";
            case NOT_FOUND -> "No encontrado";
            case CONFLICT -> "Conflicto";
            case BAD_GATEWAY -> "Error en servicio externo";
            default -> status.getReasonPhrase();
        };
    }
}
