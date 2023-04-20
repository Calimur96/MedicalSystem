package service;

import model.Center;
import model.Nurse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.NurseRepository;
import repository.UserRepository;

import java.util.List;


@Service
public class NurseService {

    @Autowired
    private NurseRepository nurseRepository;

    @Autowired
    private UserRepository userRepository;

    public Nurse findByEmail(String email) {
        return nurseRepository.findByEmail(email);
    }

    public List<Nurse> findByType(String type) {
        return nurseRepository.findByType(type);
    }
    public List<Nurse> findAllByCenter(Center center){return nurseRepository.findAllByCenter(center);}

    public List<Nurse> findAllByCentreAndType(Center center, String type) {
        return nurseRepository.findAllByCenterAndType(center, type);
    }

    public void save(Nurse nurse) {
        nurseRepository.save(nurse);

    }
}
