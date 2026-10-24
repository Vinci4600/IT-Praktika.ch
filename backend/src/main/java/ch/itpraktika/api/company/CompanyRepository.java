package ch.itpraktika.api.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, String> {

    List<Company> findAllByOrderByNameAsc();
}
