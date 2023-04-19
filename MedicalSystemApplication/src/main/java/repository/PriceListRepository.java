package repository;

import model.Center;
import model.Priceslist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceListRepository extends JpaRepository<Priceslist, Long> {

    public List<Priceslist> findAllByCenter(Center c);

    public Priceslist findByTypeOfExaminationAndCenterAndDeleted(String typeOfExamination, Center center, Boolean deleted);

    public Priceslist findByTypeOfExaminationAndDeleted(String typeOfExamination, Boolean deleted);

    public List<Priceslist> findAllByPrice(Long price);

    public List<Priceslist> findAllByTypeOfExamination(String typeOfExamination);

}
