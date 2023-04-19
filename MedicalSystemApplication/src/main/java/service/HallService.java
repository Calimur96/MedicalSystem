package service;

import model.Center;
import model.Hall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.HallRepository;

import java.util.List;

@Service
public class HallService {

    @Autowired
    private HallRepository hallRepository;

    public Hall findByNumberAndCentre(int number, Center center) {
        return hallRepository.findByNumberAndCenterAndDeleted(number, center, false);
    }

    public void save(Hall hall) {
        hallRepository.save(hall);
    }

    public void delete(Hall hall) {
        hallRepository.delete(hall);
    }

    public List<Hall> findAll() {
        return hallRepository.findAll();
    }

    public List<Hall> findAllByCentre(Center c) {
        return hallRepository.findByCenter(c);
    }
}
