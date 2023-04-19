package service;

import model.Center;
import model.Doctor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.DoctorRepository;
import repository.UserRepository;

import java.util.List;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    public Doctor findByEmail(String email) {
        return doctorRepository.findByEmail(email);
    }

    public List<Doctor> findByType(String type) {
        return doctorRepository.findByType(type);
    }

    public List<Doctor> findAllByCentreAndType(Center center, String type) {
        return doctorRepository.findAllByCenterAndType(center, type);
    }

    public void save(Doctor doctor) {
        doctorRepository.save(doctor);

    }

}
