package ch.itpraktika.api.company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "companies", indexes = {
        @Index(name = "idx_companies_partner_name", columnList = "is_partner, name")
})
public class Company {

    @Id
    @Column(length = 36, nullable = false, columnDefinition = "char(36)")
    private String id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 160)
    private String location;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json")
    private List<String> specialties;

    @Column(length = 254)
    private String email;

    @Column(name = "contact_person", length = 200)
    private String contactPerson;

    @Column(length = 40)
    private String phone;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 2048)
    private String website;

    @Column(name = "is_partner", nullable = false)
    private boolean partner;

    @Column(name = "initiative_listed", nullable = false)
    private boolean initiativeListed;

    @Column(name = "internship_url", length = 2048)
    private String internshipUrl;

    @Column(name = "logo_url", length = 2048)
    private String logoUrl;

    protected Company() {
    }

    Company(
            String id,
            String name,
            String location,
            List<String> specialties,
            String email,
            String contactPerson,
            String phone,
            String description,
            String website,
            boolean partner,
            boolean initiativeListed,
            String internshipUrl,
            String logoUrl
    ) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.specialties = specialties;
        this.email = email;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.description = description;
        this.website = website;
        this.partner = partner;
        this.initiativeListed = initiativeListed;
        this.internshipUrl = internshipUrl;
        this.logoUrl = logoUrl;
    }
// Setter and Getter
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public String getEmail() {
        return email;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsite() {
        return website;
    }

    public boolean isPartner() {
        return partner;
    }

    public boolean isInitiativeListed() {
        return initiativeListed;
    }

    public String getInternshipUrl() {
        return internshipUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void updateFrom(CompanyDto companyDto) {
        this.name = companyDto.name();
        this.location = companyDto.location();
        this.specialties = companyDto.specialties();
        this.email = companyDto.email();
        this.contactPerson = companyDto.contactPerson();
        this.phone = companyDto.phone();
        this.description = companyDto.description();
        this.website = companyDto.website();
        this.partner = companyDto.partner();
        this.initiativeListed = companyDto.initiativeListed();
        this.internshipUrl = companyDto.internshipUrl();
        this.logoUrl = companyDto.logoUrl();
    }
}
