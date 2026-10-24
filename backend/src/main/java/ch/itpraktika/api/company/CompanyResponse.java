package ch.itpraktika.api.company;

import java.util.List;

public record CompanyResponse(
        String id,
        String name,
        String standort,
        List<String> spezialitaeten,
        String email,
        String contactPerson,
        String telefon,
        String beschreibung,
        String website,
        boolean partner,
        boolean initiativeListed,
        String internshipUrl,
        String logoUrl
) {

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getLocation(),
                company.getSpecialties(),
                company.getEmail(),
                company.getContactPerson(),
                company.getPhone(),
                company.getDescription(),
                company.getWebsite(),
                company.isPartner(),
                company.isInitiativeListed(),
                company.getInternshipUrl(),
                company.getLogoUrl()
        );
    }
}
