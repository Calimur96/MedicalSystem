package service;

import dto.AppointmentDTO;
import helpers.DateInterval;
import helpers.DateUtil;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repository.AppointmentRepository;
import repository.CenterRepository;
import repository.HallRepository;
import repository.UserRepository;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@EnableTransactionManagement
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private CenterRepository centerRepository;

    @Autowired
    private UserRepository userRepository;

    public Appointment findAppointment(Date date, Hall hall, Center center)
    {

        return appointmentRepository.findByDateAndHallAndCenter(date, hall, center);
    }

    public List<Appointment> findAllByHallAndCentre(Hall hall, Center center)
    {
        return appointmentRepository.findAllByHallAndCenter(hall, center);
    }

    public List<Appointment> findAll()
    {
        return appointmentRepository.findAll();
    }

    public List<Appointment> findAllByPredefined()
    {
        return appointmentRepository.findAllByPredefined(true);
    }

    public List<Appointment> findAllByPricesList(Priceslist pl)
    {
        return appointmentRepository.findAllByPriceslist(pl);
    }

    public Appointment findAppointment(String date, int hallNumber, String centre)
    {
        Date d = DateUtil.getInstance().getDate(date, "yyyy-MM-dd HH:mm");

        Center c = centerRepository.findByName(centre);

        Hall h = hallRepository.findByNumberAndCenterAndDeleted(hallNumber, c, false);

        return appointmentRepository.findByDateAndHallAndCenter(d,h,c);
    }


    public void save(Appointment appointment)
    {
        appointmentRepository.save(appointment);
    }

    public void delete(Appointment appointment) {
        appointmentRepository.delete(appointment);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void saveLock(Appointment appointment) throws ObjectOptimisticLockingFailureException
    {
        Appointment app = appointmentRepository.findByDateAndHallAndCenter(appointment.getDate(), appointment.getHall(), appointment.getCenter());

        if(app != null)
        {
            if(app.getVersion() != appointment.getVersion())
            {
                throw new ObjectOptimisticLockingFailureException("Resource Locked.", app);
            }
        }

        appointmentRepository.save(appointment);
    }

    @Transactional(isolation =  Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void confirmAppointmentRequest(Appointment appointment, AppointmentDTO dto) throws ObjectOptimisticLockingFailureException {

        List<Appointment> apps = appointmentRepository.findAllByHall(appointment.getHall());
        DateUtil util = DateUtil.getInstance();

        Date desiredStartTime = util.getDate(dto.getDate(), "yyyy-MM-dd HH:mm");
        Date desiredEndTime = util.getDate(dto.getEndDate(), "yyyy-MM-dd HH:mm");

        String parts[] = dto.getNewDate().split(" ");
        String dat = parts[0];

        if(!dat.equals("undefined")){
            desiredStartTime = util.getDate(dto.getNewDate(), "yyyy-MM-dd HH:mm");
            desiredEndTime = util.getDate(dto.getNewEndDate(), "yyyy-MM-dd HH:mm");
        }

        DateInterval di1 = new DateInterval(desiredStartTime, desiredEndTime);

        for(Appointment app : apps)
        {
            DateInterval di2 = new DateInterval(app.getDate(), app.getEndDate());

            if(util.overlappingInterval(di1, di2)) {
                throw new ValidationException("hall");
            }
        }


        for(Doctor doctor : appointment.getDoctors())
        {
            List<Appointment> appointments = doctor.getAppointments();
            for(Appointment app : appointments)
            {
                DateInterval di2 = new DateInterval(app.getDate(), app.getEndDate());
                if(util.overlappingInterval(di1, di2) || !doctor.IsFreeOn(appointment.getDate()))
                {
                    throw new ValidationException("doctor,"+doctor.getFirstname() + " " + doctor.getLastname());
                }

            }
        }

        Appointment app = appointmentRepository.findByDateAndHallAndCenter(appointment.getDate(), appointment.getHall(), appointment.getCenter());

        if(app != null)
        {
            if(app.getVersion() != appointment.getVersion())
            {
                throw new ObjectOptimisticLockingFailureException("Resource Locked.", app);
            }
        }

        appointmentRepository.save(appointment);

        for(Doctor doc : appointment.getDoctors())
        {
            doc.getAppointments().add(appointment);
            userRepository.save(doc);
        }
    }


    public List<Appointment> findAllByPatient(Patient p)
    {
        return appointmentRepository.findAllByPatient(p);
    }

    public List<Appointment> findAllByDoctor(Long doctorID)
    {
        return appointmentRepository.findAllByDoctor(doctorID);
    }

    public List<Appointment> findAllByDoctorAndPatient(Doctor d,Patient p)
    {
        List<Appointment> apps = appointmentRepository.findAllByCenter(d.getCenter());
        List<Appointment> ret = new ArrayList<Appointment>();
        for(Appointment app : apps)
        {
            List<Doctor> doctors = app.getDoctors();

            if(app.getPatient() == null )
            {
                continue;
            }

            if(!app.getPatient().getEmail().equalsIgnoreCase(p.getEmail()))
            {
                continue;
            }

            for(Doctor doc : doctors)
            {
                if(doc.getEmail().equalsIgnoreCase(d.getEmail()))
                {
                    ret.add(app);
                }
            }
        }

        return ret;
    }

    public List<Appointment> findAllByDoctor(Doctor d)
    {
        List<Appointment> apps = appointmentRepository.findAllByCenter(d.getCenter());
        List<Appointment> ret = new ArrayList<Appointment>();
        for(Appointment app : apps)
        {
            List<Doctor> doctors = app.getDoctors();

            for(Doctor doc : doctors)
            {
                if(doc.getEmail().equals(d.getEmail()))
                {
                    ret.add(app);
                }
            }
        }

        return ret;
    }

    public List<Appointment> findAllByHall(Hall hall)
    {
        return appointmentRepository.findAllByHall(hall);
    }

    public List<Appointment> findAllByDate(Date date)
    {
        return appointmentRepository.findAllByDate(date);
    }

    public List<Appointment> findAllByCentre(Center c)
    {
        return appointmentRepository.findAllByCenter(c);
    }


}



