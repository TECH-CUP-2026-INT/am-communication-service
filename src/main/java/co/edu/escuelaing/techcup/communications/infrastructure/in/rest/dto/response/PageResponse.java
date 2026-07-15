package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        @Schema(description = "Elementos de la página actual") List<T> content,
        @Schema(description = "Número de la página actual", example = "0") int page,
        @Schema(description = "Cantidad de elementos por página", example = "20") int size,
        @Schema(description = "Cantidad total de elementos", example = "42") long totalElements,
        @Schema(description = "Cantidad total de páginas", example = "3") int totalPages
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
