package service;

import lombok.RequiredArgsConstructor;
import model.ConfirmationCodes;
import org.springframework.stereotype.Service;
import repository.ConfirmationCodesRepository;

@Service
@RequiredArgsConstructor
public class ConfirmationCodesService {

    private final ConfirmationCodesRepository confirmationCodesRepository;

    public void save(ConfirmationCodes confirmationCodes) {
        confirmationCodesRepository.save(confirmationCodes);
    }

    public ConfirmationCodes findByUserId(Long userId) {
        return confirmationCodesRepository.findById(userId).get();
    }

}
