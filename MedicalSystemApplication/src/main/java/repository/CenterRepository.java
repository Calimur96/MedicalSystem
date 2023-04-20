package repository;

import model.Center;
import model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface CenterRepository extends JpaRepository<Center, Long> {

     Center findByName(String name);
     Optional<Center> findById(Long id);
     Center getOne(Long id);
     Center findByDoctors(Doctor d);
     Center getByName(String name);

    @Lock(value = LockModeType.PESSIMISTIC_READ)
     List<Center> findAll();
}
