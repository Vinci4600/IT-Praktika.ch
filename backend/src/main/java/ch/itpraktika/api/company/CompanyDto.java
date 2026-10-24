package ch.itpraktika.api.company;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CompanyDto(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 160) String location,
        @NotEmpty @Size(max = 20) List<@NotBlank @Size(max = 100) String> specialties,
        @Email @Size(max = 254) String email,
        @Size(max = 200) String contactPerson,
        @Size(max = 40) String phone,
        @Size(max = 10000) String description,
        @Pattern(regexp = "https?://\\S+") @Size(max = 2048) String website,
        boolean partner,
        boolean initiativeListed,
        @Pattern(regexp = "https?://\\S+") @Size(max = 2048) String internshipUrl,
        @Pattern(regexp = "https?://\\S+") @Size(max = 2048) String logoUrl
) {
}
