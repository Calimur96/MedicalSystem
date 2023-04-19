package controller;

import helpers.DateInterval;
import helpers.DateUtil;
import helpers.Scheduler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import model.Appointment;
import model.AppointmentRequest;
import model.Doctor;
import model.Hall;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import service.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "api/scheduled")
@CrossOrigin
@EnableScheduling
@Api
public class ScheduledAppointmentsController {

    @Autowired
    AppointmentRequestService appointmentRequestService;
    @Autowired
    AppointmentService appointmentService;
    @Autowired
    HallService hallService;
    @Autowired
    DoctorService doctorService;
    @Autowired
    UserService userService;

    @Scheduled(cron = "0 0 0 * * *")
    public void onSchedule() {
        log.info("Starting automatic scheduling.");

        try {
            RestTemplate rest = new RestTemplate();
            rest.put("http://localhost:8080/api/scheduled/reserve", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("Automatic scheduling ended.");
    }


    @PutMapping(value = "/reserve")
    @ApiOperation("Обновление(изменение) записи")
    public ResponseEntity<Void> Reserve() {
        log.info("Updating (changing) a record.");
        reserveAlgorithmExamination();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    public void reserveAlgorithmExamination() {
        List<AppointmentRequest> requests = appointmentRequestService.findAll();

        for (AppointmentRequest request : requests) {
            if (request.getAppointmentType().equals(Appointment.AppointmentType.Examination)) {
                List<Doctor> doctors = request.getDoctors();
                reserve(request, request.getDate());
            }
        }
    }

    @Transactional
    public void reserve(AppointmentRequest request, Date start) {
        int hoursDelta = 1;

        if (request.getAppointmentType().equals(Appointment.AppointmentType.Examination)) {
            hoursDelta = 1;
        }

        Date end = Scheduler.addHoursToJavaUtilDate(start, hoursDelta);

        Hall hall = findAvailableHall(request, start, end);

        if (hall == null) {
            start = Scheduler.addHoursToJavaUtilDate(start, hoursDelta);
            reserve(request, start);
            return;
        }

        Doctor doctor = findAvailableDoctor(request, start, end);

        if (doctor == null) {
            start = Scheduler.addHoursToJavaUtilDate(start, hoursDelta);
            reserve(request, start);
        } else {
            ArrayList<Doctor> d = new ArrayList<Doctor>();
            d.add(doctor);

            Appointment appointment = new Appointment.Builder(start)
                    .withCentre(request.getCenter())
                    .withHall(hall)
                    .withPatient(request.getPatient())
                    .withType(request.getAppointmentType())
                    .withPriceslist(request.getPriceslist())
                    .withEndingDate(end)
                    .withDoctors(d)
                    .build();

            try {
                appointmentService.save(appointment);
                doctor.getAppointments().add(appointment);
                userService.save(doctor);
                appointmentRequestService.delete(request);
            } catch (Exception e) {
                log.error("Failed saving: " + e.getMessage());
            }

        }

    }

    @Transactional
    public Doctor findAvailableDoctor(AppointmentRequest request, Date start, Date end) {
        DateUtil util = DateUtil.getInstance();

        Hibernate.initialize(request.getDoctors());
        List<Doctor> doctors = request.getDoctors();

        for (Doctor d : doctors) {
            DateInterval di = new DateInterval(util.transformToDay(start, d.getShiftStart()), util.transformToDay(end, d.getShiftEnd()));

            if (d.IsFreeOn(start) //Vacations
                    && checkAppointments(d, start, end) //Appointments
                    && util.insideInterval(start, di)//Shift start
                    && util.insideInterval(end, di)) {//Shift end

                return d;
            }
        }

        doctors = doctorService.findAllByCentreAndType(request.getCenter(), request.getPriceslist().getTypeOfExamination());

        for (Doctor d : doctors) {
            if (d.IsFreeOn(start) && checkAppointments(d, start, end)) {
                return d;
            }
        }

        return null;
    }

    @Transactional
    public Boolean checkAppointments(Doctor d, Date start, Date end) {
        List<Appointment> apps = d.getAppointments();

        for (Appointment app : apps) {
            DateInterval di1 = new DateInterval(start, end);
            DateInterval di2 = new DateInterval(app.getDate(), app.getEndDate());

            if (DateUtil.getInstance().overlappingInterval(di1, di2)) {
                return false;
            }
        }

        return true;
    }

    @Transactional
    public Hall findAvailableHall(AppointmentRequest request, Date start, Date end) {
        List<Hall> halls = hallService.findAllByCentre(request.getCenter());
        for (Hall hall : halls) {
            List<Appointment> apps = appointmentService.findAllByHall(hall);
            List<DateInterval> intervals = Scheduler.getFreeIntervals(apps, start);

            if (intervals.size() == 0) {
                return hall;
            }

            for (DateInterval di : intervals) {
                if (DateUtil.getInstance().insideInterval(start, di) && DateUtil.getInstance().insideInterval(end, di)) {
                    return hall;
                }
            }
        }

        return null;
    }
}
