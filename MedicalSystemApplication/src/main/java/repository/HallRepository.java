package repository;

import model.Center;
import model.Hall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HallRepository extends JpaRepository<Hall, Long> {

    public Hall findByNumberAndCenterAndDeleted(int number, Center center, Boolean deleted);

    public List<Hall> findByCenter(Center c);
}
