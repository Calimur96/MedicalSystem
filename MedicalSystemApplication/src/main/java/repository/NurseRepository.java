package repository;

import model.Center;
import model.Nurse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NurseRepository extends JpaRepository<Nurse,Long> {

        Nurse findByEmail(String email);
        List<Nurse> findAllByCenter(Center center);

        List<Nurse> findByType(String type);

        List<Nurse> findAllByCenterAndType(Center center, String type);

}
