package service;

import helpers.DateUtil;
import helpers.Scheduler;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repository.AppointmentRequestRepository;
import repository.CentreRepository;
import repository.HallRepository;
import repository.UserRepository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AppointmentRequestService {

    @Autowired
    private AppointmentRequestRepository appointmentRequestRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private CentreRepository centreRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<AppointmentRequest> findById(long id)
    {
        return appointmentRequestRepository.findById(id);
    }

    public List<AppointmentRequest> getAllByCentre(String centre)
    {
        Centre c = centreRepository.findByName(centre);

        return appointmentRequestRepository.findAllByCentre(c);
    }

    public List<AppointmentRequest> getAllByPatient(Patient p)
    {
        return appointmentRequestRepository.findAllByPatient(p);
    }

    public AppointmentRequest findAppointmentRequest(Date date, Patient patient, Centre centre)
    {
        return appointmentRequestRepository.findByDateAndPatientAndCentre(date, patient, centre);
    }

    public AppointmentRequest findAppointmentRequest(Date date, Hall hall, Centre centre)
    {
        return appointmentRequestRepository.findByDateAndHallAndCentre(date, hall, centre);
    }

    public AppointmentRequest findAppointmentRequest(String date, String patientEmail, String centre)
    {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        try {
            Date d = df.parse(date);

            Patient p = (Patient) userRepository.findByEmailAndDeleted(patientEmail, false);

            Centre c = centreRepository.findByName(centre);

            return findAppointmentRequest(d, p, c);

        } catch (Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }


    public AppointmentRequest findAppointmentRequest(String date, int hallNumber,String centre)
    {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        try {
            Date d = df.parse(date);


            Centre c = centreRepository.findByName(centre);


            Hall h = hallRepository.findByNumberAndCentreAndDeleted(hallNumber, c, false);

            return findAppointmentRequest(d,h,c);

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public void delete(AppointmentRequest request)
    {
        appointmentRequestRepository.delete(request);
    }

    public void save(AppointmentRequest request)
    {
        appointmentRequestRepository.save(request);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void saveLock(AppointmentRequest request) throws ConcurrentModificationException
    {
        AppointmentRequest req = findAppointmentRequest(request.getDate(), request.getHall(), request.getCentre());

        if(req != null)
        {
            throw new ConcurrentModificationException("Already made");
        }

        Date start = request.getDate();
        Date end = Scheduler.addHoursToJavaUtilDate(start, 1);

        for(Doctor d : request.getDoctors())
        {
            List<Appointment> appointments = d.getAppointments();

            for(Appointment app : appointments)
            {
                if(DateUtil.getInstance().overlappingInterval(start, end, app.getDate(), app.getEndDate()))
                {
                    throw new ConcurrentModificationException("Overlap");
                }
            }
        }

        appointmentRequestRepository.save(request);
    }

    public List<AppointmentRequest> findAll(){
        return appointmentRequestRepository.findAll();
    }



}
