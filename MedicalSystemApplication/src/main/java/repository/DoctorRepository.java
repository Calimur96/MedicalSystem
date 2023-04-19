package repository;

import model.Center;
import model.Doctor;
import model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Doctor findByEmail(String email);

    List<Doctor> findByType(String type);

    List<Doctor> findAllByCenterAndType(Center center, String type);


     List<User> findAllByRole(User.UserRole role);

}
