package ch.itpraktika.api.internship;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/stellen")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInternshipController {

    private final InternshipService internshipService;

    public AdminInternshipController(InternshipService internshipService) {
        this.internshipService = internshipService;
    }

    @GetMapping
    public List<InternshipResponse> listAll() {
        return internshipService.listAll();
    }

    @PostMapping
    public ResponseEntity<InternshipResponse> create(@Valid @RequestBody InternshipRequest request) {
        InternshipResponse response = internshipService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/admin/stellen/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    public InternshipResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody InternshipRequest request
    ) {
        return internshipService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        internshipService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
