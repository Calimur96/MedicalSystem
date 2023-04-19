package service;

import dto.VacationDTO;
import model.Center;
import model.User;
import model.Vacation;
import model.VacationRequest;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repository.VacationRepository;
import repository.VacationRequestRepository;

import javax.validation.ValidationException;
import java.util.List;
import java.util.Optional;


@Service
public class VacationRequestService {

    @Autowired
    public VacationRequestRepository vacationRequestRepository;

    @Autowired
    private VacationRepository vacationRepository;

    public List<VacationRequest> findAllByUser(User user)
    {
        return vacationRequestRepository.findAllByUser(user);
    }

    public List<VacationRequest> findAllByCentre(Center center)
    {
        return vacationRequestRepository.findAllByCenter(center);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public Vacation resolveVacationRequestLock(List<VacationRequest> vrq, VacationDTO dto, Boolean isAccepted) throws OptimisticEntityLockException
    {
        VacationRequest req = null;

        for(VacationRequest request : vrq)
        {
            if(request.getId().equals(dto.getId()))
            {
                req = request;
            }
        }

        if(req == null)
        {
            throw new ValidationException("Request not found.");
        }

        if(req.getVersion() != dto.getVersion())
        {
            throw new OptimisticEntityLockException(req,"LOCKED");
        }

        Vacation vacation = new Vacation();
        vacation.setStartDate(req.getStartDate());
        vacation.setEndDate(req.getEndDate());
        vacation.setUser(req.getUser());

        if(isAccepted)
        {
            vacationRepository.save(vacation);
            vacationRequestRepository.delete(req);
        }
        else
        {
            vacationRequestRepository.delete(req);
        }

        return vacation;
    }

    public void save(VacationRequest request)
    {
        vacationRequestRepository.save(request);
    }

    public void delete(VacationRequest request)
    {
        vacationRequestRepository.delete(request);
    }

    public Optional<VacationRequest> findById(VacationRequest request)
    {
        return vacationRequestRepository.findById(request.getId());
    }
}
