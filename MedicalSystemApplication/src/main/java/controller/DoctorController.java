package controller;

import dto.CenterDTO;
import dto.DateIntervalDTO;
import dto.DoctorDTO;
import dto.ReviewDTO;
import helpers.DateInterval;
import helpers.DateUtil;
import helpers.Scheduler;
import helpers.SecurePasswordHasher;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.*;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "api/doctors")
@CrossOrigin
@Api
public class DoctorController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private CenterService centerService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @PostMapping(value = "/makeNewDoctor", consumes = "application/json")
    @ApiOperation("Добавление нового доктора")
    public ResponseEntity<Void> addNewDoctor(@RequestBody DoctorDTO dto) {
        log.info("Adding a new doctor to the center '{}'.", dto.getCentreName());
        Doctor d = doctorService.findByEmail(dto.getUser().getEmail());
        Center c = centerService.findByName(dto.getCentreName());

        if (d != null) {
            return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
        }

        if (c == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String pass = dto.getUser().getPassword();
        try {
            pass = SecurePasswordHasher.getInstance().encode(pass);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Doctor doctor = new Doctor(dto);
        doctor.setPassword(pass);
        doctor.setCenter(c);
        doctorService.save(doctor);

        c.getDoctors().add(doctor);
        centerService.save(c);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping(value = "/addReview")
    @ApiOperation("Добавить отзыв")
    public ResponseEntity<Void> addReview(@RequestBody ReviewDTO dto) {
        log.info("Adding a review from a patient with email '{}'.", dto.getPatientEmail());
        Doctor doctor = doctorService.findByEmail(dto.getDoctorEmail());
        Patient patient = patientService.findByEmail(dto.getPatientEmail());

        if (doctor == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for (ReviewDoctor review : doctor.getReviews()) {
            if (review.getPatient().getEmail().equals(dto.getPatientEmail())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        ReviewDoctor rd = new ReviewDoctor(dto.getRating(), DateUtil.getInstance().now("dd-MM-yyyy"), patient);
        doctor.getReviews().add(rd);
        doctorService.save(doctor);

        notificationService.sendNotification(patient.getEmail(), "You rated the doctor!", "Your rating of" + dto.getRating() + "stars for the doctor" + dto.getDoctorEmail() + "it is successfully recorded! Thank you for the review!");
        notificationService.sendNotification(doctor.getEmail(), "You're rated!", "Your work is evaluated by" + dto.getPatientEmail() + "with a rating of" + dto.getRating() + "stars!");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/getCenter/{doctorEmail}")
    @ApiOperation("Получение центров по работающим в них докторам")
    public ResponseEntity<CenterDTO> getCentreByDoctor(@PathVariable("doctorEmail") String email) {
        log.info("Receiving centers by doctors working in them by email '{}'.", email);
        Doctor d = doctorService.findByEmail(email);

        if (d == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Center c = centerService.findByDoctor(d);
        CenterDTO dto = new CenterDTO(c);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @DeleteMapping(value = "/removeDoctor/{email}")
    @ApiOperation("Удаление докторов")
    public ResponseEntity<Void> removeDoctor(@PathVariable("email") String email) {
        log.info("Deleting a doctor with email '{}'.", email);
        HttpHeaders header = new HttpHeaders();
        Doctor doc = doctorService.findByEmail(email);

        if (doc == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments = appointmentService.findAllByDoctor(doc);

        if (appointments != null) {
            if (appointments.size() > 0) {
                header.set("responseText", "Doctor(" + email + ") it cannot be deleted because it has " + appointments.size() + " scheduled examinations");
                return new ResponseEntity<>(header, HttpStatus.CONFLICT);
            }
        } else {
            return new ResponseEntity<Void>(header, HttpStatus.CONFLICT);
        }
        doc.setDeleted(true);
        doctorService.save(doc);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/getBusyTime/{doctor}/{date}")
    @ApiOperation("Получение времени когда докторы заняты")
    public ResponseEntity<List<DateIntervalDTO>> getBusyTime(@PathVariable("doctor") String doctorEmail, @PathVariable("date") String date) {
        log.info("Getting time when doctors are busy by email '{}'.", doctorEmail);
        Doctor d = doctorService.findByEmail(doctorEmail);

        if (d == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<DateInterval> intervals = Scheduler.getFreeIntervals(d, DateUtil.getInstance().getDate(date, "dd-MM-yyyy"));
        List<DateIntervalDTO> dtos = new ArrayList<DateIntervalDTO>();

        for (DateInterval di : intervals) {
            dtos.add(new DateIntervalDTO(di, "HH:mm"));
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping(value = "/getFreeTime/{doctor}/{date}")
    @ApiOperation("Получение времени когда докторы свободны")
    public ResponseEntity<List<DateIntervalDTO>> getFreeTime(@PathVariable("doctor") String doctorEmail, @PathVariable("date") String date) {
        log.info("Getting time when doctors are free by email '{}'.", doctorEmail);
        Doctor d = doctorService.findByEmail(doctorEmail);

        if (d == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<DateInterval> intervals = Scheduler.getFreeIntervals(d, DateUtil.getInstance().getDate(date, "dd-MM-yyyy"));
        List<DateIntervalDTO> dtos = new ArrayList<DateIntervalDTO>();

        for (DateInterval di : intervals) {
            dtos.add(new DateIntervalDTO(di, "HH:mm"));
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

}
