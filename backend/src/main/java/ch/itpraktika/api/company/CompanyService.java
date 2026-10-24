package ch.itpraktika.api.company;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> listCompanies() {
        return companyRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CompanyResponse::from)
                .toList();
    }

    @Transactional
    public CompanyResponse createCompany(CompanyDto request) {
        Company company = new Company(
                UUID.randomUUID().toString(),
                request.name(),
                request.location(),
                request.specialties(),
                request.email(),
                request.contactPerson(),
                request.phone(),
                request.description(),
                request.website(),
                request.partner(),
                request.initiativeListed(),
                request.internshipUrl(),
                request.logoUrl()
        );
        return CompanyResponse.from(companyRepository.save(company));
    }

    @Transactional
    public void deleteCompany(UUID id) {
        Company company = companyRepository.findById(id.toString())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Die Firma wurde nicht gefunden."));
        companyRepository.delete(company);
    }

    @Transactional
    public CompanyResponse updateCompany(UUID id, CompanyDto companyDto) {
        Company company = companyRepository.findById(id.toString())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Die Firma wurde nicht gefunden."));
        company.updateFrom(companyDto);
        return CompanyResponse.from(companyRepository.save(company));
    }
}
