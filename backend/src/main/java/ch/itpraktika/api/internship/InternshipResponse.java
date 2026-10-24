package ch.itpraktika.api.internship;

import java.time.LocalDate;
import java.util.List;

public record InternshipResponse(
        String id,
        String firmaId,
        String titel,
        String firma,
        String standort,
        String dauer,
        String startOption,
        LocalDate startDatum,
        String beruf,
        String ansprechperson,
        String email,
        String telefon,
        String bewerbungsUrl,
        String entlohnung,
        String beschreibung,
        List<String> anforderungen,
        String status
) {

    public static InternshipResponse from(Internship internship) {
        return new InternshipResponse(
                internship.getId(),
                internship.getCompanyId(),
                internship.getTitle(),
                internship.getCompanyName(),
                internship.getLocation(),
                internship.getDuration(),
                internship.getStartOption().name().toLowerCase(),
                internship.getStartDate(),
                internship.getCareerField(),
                internship.getContactPerson(),
                internship.getContactEmail(),
                internship.getContactPhone(),
                internship.getApplicationUrl(),
                internship.getCompensation(),
                internship.getDescription(),
                internship.getRequirements(),
                internship.getStatus().name().toLowerCase()
        );
    }
}
