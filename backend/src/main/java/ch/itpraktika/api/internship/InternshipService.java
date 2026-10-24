package ch.itpraktika.api.internship;

import ch.itpraktika.api.company.Company;
import ch.itpraktika.api.company.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class InternshipService {

    private final InternshipRepository internshipRepository;
    private final CompanyRepository companyRepository;

    public InternshipService(
            InternshipRepository internshipRepository,
            CompanyRepository companyRepository
    ) {
        this.internshipRepository = internshipRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<InternshipResponse> listAll() {
        return internshipRepository.findAll()
                .stream()
                .map(InternshipResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listPublished(
            String search,
            String careerField,
            String location,
            int page,
            int size
    ) {
        Specification<Internship> filters = (root, query, builder) ->
                builder.equal(root.get("status"), Internship.InternshipStatus.PUBLISHED);
        if (careerField != null && !careerField.isBlank()) {
            filters = filters.and((root, query, builder) ->
                    builder.equal(root.get("careerField"), careerField.trim()));
        }
        if (location != null && !location.isBlank()) {
            filters = filters.and((root, query, builder) ->
                    builder.equal(root.get("location"), location.trim()));
        }
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            filters = filters.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("title")), pattern),
                    builder.like(builder.lower(root.get("companyName")), pattern),
                    builder.like(builder.lower(root.get("location")), pattern),
                    builder.like(builder.lower(root.get("description")), pattern)
            ));
        }

        Page<InternshipResponse> results = internshipRepository.findAll(
                        filters,
                        PageRequest.of(page, size)
                )
                .map(InternshipResponse::from);
        return Map.of(
                "data", results.getContent(),
                "page", results.getNumber(),
                "size", results.getSize(),
                "totalElements", results.getTotalElements(),
                "totalPages", results.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public InternshipResponse getPublished(UUID id) {
        return internshipRepository.findByIdAndStatus(
                        id.toString(),
                        Internship.InternshipStatus.PUBLISHED
                )
                .map(InternshipResponse::from)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Die Stelle wurde nicht gefunden."));
    }

    @Transactional
    public InternshipResponse create(InternshipRequest request) {
        Company company = findCompany(request.firmaId());
        Internship internship = fromRequest(UUID.randomUUID().toString(), request, company);
        return InternshipResponse.from(internshipRepository.save(internship));
    }

    @Transactional
    public InternshipResponse update(UUID id, InternshipRequest request) {
        Internship internship = findInternship(id);
        Company company = findCompany(request.firmaId());
        internship.updateFrom(request, company);
        return InternshipResponse.from(internshipRepository.save(internship));
    }

    @Transactional
    public void delete(UUID id) {
        internshipRepository.delete(findInternship(id));
    }

    private Company findCompany(UUID companyId) {
        if (companyId == null) {
            return null;
        }
        return companyRepository.findById(companyId.toString())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Die Firma wurde nicht gefunden."));
    }

    private Internship findInternship(UUID id) {
        return internshipRepository.findById(id.toString())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Die Stelle wurde nicht gefunden."));
    }

    private static Internship fromRequest(String id, InternshipRequest request, Company company) {
        return new Internship(
                id,
                company,
                company == null ? request.firma() : company.getName(),
                request.titel(),
                request.standort(),
                request.dauer(),
                request.startOption(),
                request.startDatum(),
                request.beruf(),
                request.ansprechperson(),
                request.email(),
                request.telefon(),
                request.bewerbungsUrl(),
                request.entlohnung(),
                request.beschreibung(),
                request.anforderungen(),
                request.status()
        );
    }
}
