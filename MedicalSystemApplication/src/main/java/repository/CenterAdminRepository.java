package repository;

import model.CenterAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CenterAdminRepository extends JpaRepository<CenterAdmin,Long> {

    CenterAdmin findByEmail(String email);
}

