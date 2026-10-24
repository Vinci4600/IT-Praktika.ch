package ch.itpraktika.api.internship;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/stellen")
public class InternshipController {

    private final InternshipService internshipService;

    public InternshipController(InternshipService internshipService) {
        this.internshipService = internshipService;
    }

    @GetMapping
    public Map<String, Object> listInternships(
            @RequestParam(required = false) @Size(max = 100) String suche,
            @RequestParam(required = false) @Size(max = 100) String beruf,
            @RequestParam(required = false) @Size(max = 100) String standort,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size
    ) {
        return internshipService.listPublished(suche, beruf, standort, page, size);
    }

    @GetMapping("/{id}")
    public InternshipResponse getInternship(@PathVariable UUID id) {
        return internshipService.getPublished(id);
    }
}
