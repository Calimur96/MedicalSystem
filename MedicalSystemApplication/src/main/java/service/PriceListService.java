package service;

import model.Center;
import model.Priceslist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.CenterRepository;
import repository.PriceListRepository;

import java.util.List;

@Service
public class PriceListService {

    @Autowired
    private PriceListRepository priceListRepository;
    @Autowired
    private CenterRepository centerRepository;

    public Priceslist findByTypeOfExaminationAndCentre(String typeOfExamination, String centreName) {

        Center center = centerRepository.findByName(centreName);
        return priceListRepository.findByTypeOfExaminationAndCenterAndDeleted(typeOfExamination, center, false);
    }

    public Priceslist findByTypeOfExaminationAndCentre(String typeOfExamination, Center center) {
        return priceListRepository.findByTypeOfExaminationAndCenterAndDeleted(typeOfExamination, center, false);
    }

    public List<Priceslist> findAllByCentre(Center c) {
        return priceListRepository.findAllByCenter(c);
    }

    public List<Priceslist> findAllByPrice(Long price) {
        return priceListRepository.findAllByPrice(price);
    }

    public List<Priceslist> findAllByTypeOfExamination(String typeOfExamination) {
        return priceListRepository.findAllByTypeOfExamination(typeOfExamination);
    }

    public Priceslist findByTypeOfExaminationAndDeleted(String typeOfExamination, Boolean deleted) {
        return priceListRepository.findByTypeOfExaminationAndDeleted(typeOfExamination, deleted);
    }

    public void save(Priceslist priceslist) {
        priceListRepository.save(priceslist);
    }

    public void delete(Priceslist priceslist) {
        priceListRepository.delete(priceslist);
    }

    public List<Priceslist> findAll() {
        return priceListRepository.findAll();
    }
}
