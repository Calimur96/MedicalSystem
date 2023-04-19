package repository;

import model.Center;
import model.User;
import model.VacationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacationRequestRepository extends JpaRepository<VacationRequest, Long> {

    public List<VacationRequest> findAllByUser(User user);

    public List<VacationRequest> findAllByCenter(Center center);


}
