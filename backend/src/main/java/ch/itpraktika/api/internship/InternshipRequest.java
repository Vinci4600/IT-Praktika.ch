package ch.itpraktika.api.internship;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InternshipRequest(
        UUID firmaId,
        @NotBlank @Size(max = 200) String firma,
        @NotBlank @Size(max = 200) String titel,
        @NotBlank @Size(max = 160) String standort,
        @NotBlank @Size(max = 100) String dauer,
        @NotNull Internship.StartOption startOption,
        LocalDate startDatum,
        @NotBlank @Size(max = 160) String beruf,
        @Size(max = 200) String ansprechperson,
        @NotBlank @Email @Size(max = 254) String email,
        @Size(max = 40) String telefon,
        @Pattern(regexp = "https?://\\S+") @Size(max = 2048) String bewerbungsUrl,
        @NotBlank @Size(max = 160) String entlohnung,
        @NotBlank @Size(max = 20000) String beschreibung,
        @NotEmpty @Size(max = 50) List<@NotBlank @Size(max = 200) String> anforderungen,
        @NotNull Internship.InternshipStatus status
) {
    @AssertTrue(message = "Bei einem konkreten Startdatum muss ein Datum angegeben werden.")
    public boolean isStartDateConsistent() {
        return startOption == Internship.StartOption.DATE
                ? startDatum != null
                : startDatum == null;
    }
}
