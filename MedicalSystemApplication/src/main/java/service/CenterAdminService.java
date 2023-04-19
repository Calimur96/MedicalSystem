package service;

import model.CenterAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.CenterAdminRepository;
import repository.UserRepository;

@Service
public class CenterAdminService {

    @Autowired
    private CenterAdminRepository centerAdminRepository;

    @Autowired
    private UserRepository userRepository;

    public CenterAdmin findByEmail(String email) {
        return centerAdminRepository.findByEmail(email);
    }

    public void save(CenterAdmin centerAdmin) {
        centerAdminRepository.save(centerAdmin);
    }
}

