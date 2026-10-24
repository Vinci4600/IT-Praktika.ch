package ch.itpraktika.api.internship;

import ch.itpraktika.api.company.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "internships", indexes = {
        @Index(name = "idx_internships_status_created", columnList = "status, created_at"),
        @Index(name = "idx_internships_location_field", columnList = "location, career_field")
})
public class Internship {

    @Id
    @Column(length = 36, nullable = false, columnDefinition = "char(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 160)
    private String location;

    @Column(nullable = false, length = 100)
    private String duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_option", nullable = false)
    private StartOption startOption;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "career_field", nullable = false, length = 160)
    private String careerField;

    @Column(name = "contact_person", length = 200)
    private String contactPerson;

    @Column(name = "contact_email", nullable = false, length = 254)
    private String contactEmail;

    @Column(name = "contact_phone", length = 40)
    private String contactPhone;

    @Column(name = "application_url", length = 2048)
    private String applicationUrl;

    @Column(nullable = false, length = 160)
    private String compensation;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json")
    private List<String> requirements;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InternshipStatus status;

    protected Internship() {
    }

    Internship(
            String id,
            Company company,
            String companyName,
            String title,
            String location,
            String duration,
            StartOption startOption,
            LocalDate startDate,
            String careerField,
            String contactPerson,
            String contactEmail,
            String contactPhone,
            String applicationUrl,
            String compensation,
            String description,
            List<String> requirements,
            InternshipStatus status
    ) {
        this.id = id;
        this.company = company;
        this.companyName = companyName;
        this.title = title;
        this.location = location;
        this.duration = duration;
        this.startOption = startOption;
        this.startDate = startDate;
        this.careerField = careerField;
        this.contactPerson = contactPerson;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.applicationUrl = applicationUrl;
        this.compensation = compensation;
        this.description = description;
        this.requirements = requirements;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getCompanyName() {
        return company != null ? company.getName() : companyName;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getDuration() {
        return duration;
    }

    public StartOption getStartOption() {
        return startOption;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public String getCareerField() {
        return careerField;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public String getCompensation() {
        return compensation;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public InternshipStatus getStatus() {
        return status;
    }

    public String getCompanyId() {
        return company != null ? company.getId() : null;
    }

    public void updateFrom(InternshipRequest request, Company company) {
        this.company = company;
        this.companyName = company != null ? company.getName() : request.firma();
        this.title = request.titel();
        this.location = request.standort();
        this.duration = request.dauer();
        this.startOption = request.startOption();
        this.startDate = request.startDatum();
        this.careerField = request.beruf();
        this.contactPerson = request.ansprechperson();
        this.contactEmail = request.email();
        this.contactPhone = request.telefon();
        this.applicationUrl = request.bewerbungsUrl();
        this.compensation = request.entlohnung();
        this.description = request.beschreibung();
        this.requirements = request.anforderungen();
        this.status = request.status();
    }

    public enum StartOption {
        IMMEDIATE,
        BY_AGREEMENT,
        DATE
    }

    public enum InternshipStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }
}
