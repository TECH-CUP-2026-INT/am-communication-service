package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.dto.FaqRequest;
import co.edu.escuelaing.techcup.communications.dto.FaqResponse;
import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.mapper.FaqMapper;
import co.edu.escuelaing.techcup.communications.service.CreateFaqUseCase;
import co.edu.escuelaing.techcup.communications.service.DeleteFaqUseCase;
import co.edu.escuelaing.techcup.communications.service.GetFaqUseCase;
import co.edu.escuelaing.techcup.communications.service.ListFaqsUseCase;
import co.edu.escuelaing.techcup.communications.service.UpdateFaqUseCase;
import co.edu.escuelaing.techcup.communications.service.command.UpdateFaqCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final CreateFaqUseCase createFaqUseCase;
    private final UpdateFaqUseCase updateFaqUseCase;
    private final DeleteFaqUseCase deleteFaqUseCase;
    private final GetFaqUseCase getFaqUseCase;
    private final ListFaqsUseCase listFaqsUseCase;
    private final FaqMapper faqMapper;

    @PostMapping
    public ResponseEntity<FaqResponse> create(@Valid @RequestBody FaqRequest request,
                                              UriComponentsBuilder uriBuilder) {
        Faq faq = createFaqUseCase.create(faqMapper.toCreateCommand(request));
        URI location = uriBuilder.path("/faqs/{id}").buildAndExpand(faq.getId()).toUri();
        return ResponseEntity.created(location).body(faqMapper.toResponse(faq));
    }

    @GetMapping
    public ResponseEntity<List<FaqResponse>> list() {
        List<FaqResponse> faqs = listFaqsUseCase.listAll().stream()
                .map(faqMapper::toResponse)
                .toList();
        return ResponseEntity.ok(faqs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FaqResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(faqMapper.toResponse(getFaqUseCase.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FaqResponse> update(@PathVariable UUID id, @Valid @RequestBody FaqRequest request) {
        Faq faq = updateFaqUseCase.update(new UpdateFaqCommand(id, request.keywords(), request.answer()));
        return ResponseEntity.ok(faqMapper.toResponse(faq));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deleteFaqUseCase.delete(id);
    }
}
