package repository;

import model.ConfirmationCodes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfirmationCodesRepository extends JpaRepository<ConfirmationCodes, Long> {
}
