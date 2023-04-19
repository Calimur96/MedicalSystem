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
import repository.CenterRepository;
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
    private CenterRepository centerRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<AppointmentRequest> findById(long id) {
        return appointmentRequestRepository.findById(id);
    }

    public List<AppointmentRequest> getAllByCentre(String centre) {
        Center c = centerRepository.findByName(centre);
        return appointmentRequestRepository.findAllByCenter(c);
    }

    public List<AppointmentRequest> getAllByPatient(Patient p) {
        return appointmentRequestRepository.findAllByPatient(p);
    }

    public AppointmentRequest findAppointmentRequest(Date date, Patient patient, Center center) {
        return appointmentRequestRepository.findByDateAndPatientAndCenter(date, patient, center);
    }

    public AppointmentRequest findAppointmentRequest(Date date, Hall hall, Center center) {
        return appointmentRequestRepository.findByDateAndHallAndCenter(date, hall, center);
    }

    public AppointmentRequest findAppointmentRequest(String date, String patientEmail, String centre) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            Date d = df.parse(date);
            Patient p = (Patient) userRepository.findByEmailAndDeleted(patientEmail, false);
            Center c = centerRepository.findByName(centre);
            return findAppointmentRequest(d, p, c);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public AppointmentRequest findAppointmentRequest(String date, int hallNumber, String centre) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            Date d = df.parse(date);
            Center c = centerRepository.findByName(centre);
            Hall h = hallRepository.findByNumberAndCenterAndDeleted(hallNumber, c, false);
            return findAppointmentRequest(d, h, c);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public void delete(AppointmentRequest request) {
        appointmentRequestRepository.delete(request);
    }

    public void save(AppointmentRequest request) {
        appointmentRequestRepository.save(request);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void saveLock(AppointmentRequest request) throws ConcurrentModificationException {
        AppointmentRequest req = findAppointmentRequest(request.getDate(), request.getHall(), request.getCenter());

        if (req != null) {
            throw new ConcurrentModificationException("Already made");
        }

        Date start = request.getDate();
        Date end = Scheduler.addHoursToJavaUtilDate(start, 1);

        for (Doctor d : request.getDoctors()) {
            List<Appointment> appointments = d.getAppointments();

            for (Appointment app : appointments) {
                if (DateUtil.getInstance().overlappingInterval(start, end, app.getDate(), app.getEndDate())) {
                    throw new ConcurrentModificationException("Overlap");
                }
            }
        }

        appointmentRequestRepository.save(request);
    }

    public List<AppointmentRequest> findAll() {
        return appointmentRequestRepository.findAll();
    }

}
