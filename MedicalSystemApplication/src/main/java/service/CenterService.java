package service;

import dto.ReviewDTO;
import helpers.DateUtil;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import repository.CenterRepository;
import repository.NurseRepository;
import repository.UserRepository;


import javax.validation.ValidationException;
import java.util.List;
import java.util.Optional;

@Service
public class CenterService {

    @Autowired
    private CenterRepository centerRepository;
    @Autowired
    private NurseRepository nurseRepository;

    @Autowired
    private UserRepository userRepository;

    public Center findByName(String name) {
        return centerRepository.findByName(name);
    }

    public Center findById (Long id){return centerRepository.findById(id).get() ;}
    public Center findByDoctor(Doctor d) {
        return centerRepository.findByDoctors(d);
    }
    public Center getByName(String name){return  centerRepository.getByName(name);}

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void rateCentreSafe(ReviewDTO dto)  {
        Center center = findByName(dto.getCentreName());
        Patient patient = (Patient) userRepository.findByEmailAndDeleted(dto.getPatientEmail(), false);

        if (center == null || patient == null) {
            throw new ValidationException("Center or patient not found");
        }

        CenterReview cr = new CenterReview(dto.getRating(), DateUtil.getInstance().now("dd-MM-yyyy"), patient);
        center.getReviews().add(cr);
        center.calculateRating();

        save(center);
    }

    public void save(Center center) {
        centerRepository.save(center);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<Center> findAllSafe() {
        return centerRepository.findAll();
    }

    public List<Center> findAll() {
        return centerRepository.findAll();
    }
}
