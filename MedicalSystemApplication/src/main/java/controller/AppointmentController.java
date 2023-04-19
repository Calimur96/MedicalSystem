package controller;

import dto.AppointmentDTO;
import helpers.DateInterval;
import helpers.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.User.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import service.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping(value = "api/appointments")
@CrossOrigin
@Api
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRequestService appointmentRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private CenterService centerService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private HallService hallService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PriceListService priceslistService;

    @GetMapping(value = "/get")
    @ApiOperation("Записаться на приемы")
    public ResponseEntity<AppointmentDTO> getAppointment(@RequestBody AppointmentDTO dto) {
        log.info("Making an appointment at the medical center '{}'.", dto.getCentreName());
        String centre = dto.getCentreName();
        String date = dto.getDate();
        int hallNumber = dto.getHallNumber();

        Appointment appointment = appointmentService.findAppointment(date, hallNumber, centre);

        HttpHeaders header = new HttpHeaders();
        if (appointment == null) {
            header.set("responseText", "Appointment not found for: (" + date + "," + hallNumber + ")");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new AppointmentDTO(appointment), HttpStatus.OK);
    }

    // Get By doctor and patient
    @GetMapping(value = "/getAppointments/{doctorEmail}/{patientEmail}")
    @ApiOperation("Назначить приемы")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatient(@PathVariable("doctorEmail") String doctorEmail, @PathVariable("patientEmail") String patientEmail) {
        log.info("Assigning a patient '{}' to an appointment with a doctor '{}'.", patientEmail, doctorEmail);
        Patient patient = patientService.findByEmail(patientEmail);
        Doctor doctor = doctorService.findByEmail(doctorEmail);

        if (patient == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (doctor == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Appointment> app = appointmentService.findAllByDoctorAndPatient(doctor, patient);
        List<AppointmentDTO> appDTO = new ArrayList<>();

        if (app == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for (Appointment a : app) {
            appDTO.add(new AppointmentDTO(a));
        }

        return new ResponseEntity<List<AppointmentDTO>>(appDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/getAppointment/{centreName}/{date}/{hallNumber}")
    @ApiOperation("Записаться на конкретные приемы")
    public ResponseEntity<AppointmentDTO> getAppointment(@PathVariable("centreName") String centre, @PathVariable("date") String date, @PathVariable("hallNumber") int hallNumber) {
        log.info("Making an appointment at the medical center '{}' on '{}'.", centre, date);
        Appointment appointment = appointmentService.findAppointment(date, hallNumber, centre);

        HttpHeaders header = new HttpHeaders();
        if (appointment == null) {
            header.set("responseText", "Appointment not found for: (" + date + "," + hallNumber + ")");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new AppointmentDTO(appointment), HttpStatus.OK);
    }

    @GetMapping(value = "/getAll")
    @ApiOperation("Получение всевозможных назначений на приемы")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        log.info("Getting all sorts of appointments for appointments.");
        List<Appointment> app = appointmentService.findAll();
        List<AppointmentDTO> appDTO = new ArrayList<AppointmentDTO>();

        if (app == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for (Appointment a : app) {
            appDTO.add(new AppointmentDTO(a));
        }

        return new ResponseEntity<>(appDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/getRequest")
    @ApiOperation("Получение запросов на приемы")
    public ResponseEntity<AppointmentDTO> getAppointmentRequest(@RequestBody AppointmentDTO dto) {
        log.info("Receiving requests for appointments at the medical center '{}'.", dto.getCentreName());
        String centre = dto.getCentreName();
        String date = dto.getDate();
        int hallNumber = dto.getHallNumber();
        AppointmentRequest appointmentReq = appointmentRequestService.findAppointmentRequest(date, hallNumber, centre);

        HttpHeaders header = new HttpHeaders();

        if (appointmentReq == null) {
            header.set("responseText", "Appointment not found for: (" + date + "," + hallNumber + ")");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new AppointmentDTO(appointmentReq), HttpStatus.OK);
    }

    @GetMapping(value = "/center/getAllRequests/{centreName}")
    @ApiOperation("Получение всех запросов на приемы")
    public ResponseEntity<AppointmentDTO[]> getAllAppointmentRequests(@PathVariable("centreName") String centre) {
        log.info("Receiving all requests for appointments at the medical center '{}'.", centre);
        List<AppointmentRequest> list = appointmentRequestService.getAllByCentre(centre);

        if (list == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<AppointmentDTO> dtos = new ArrayList<AppointmentDTO>();

        for (AppointmentRequest req : list) {
            dtos.add(new AppointmentDTO(req));
        }

        return new ResponseEntity<>(dtos.toArray(new AppointmentDTO[dtos.size()]), HttpStatus.OK);
    }

    @GetMapping(value = "/center/getAllAppointments/{centreName}")
    @ApiOperation("Получение всех назначений центров")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsCentre(@PathVariable("centreName") String centreName) {
        log.info("Receipt of all appointments in the medical center '{}'.", centreName);
        Center center = centerService.findByName(centreName);

        if (center == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Appointment> list = appointmentService.findAllByCentre(center);

        if (list == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<AppointmentDTO> dtos = new ArrayList<AppointmentDTO>();

        for (Appointment app : list) {
            dtos.add(new AppointmentDTO(app));
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping(value = "/center/getAllAppointmentsToday/{centreName}")
    @ApiOperation("Получение всех сегодняшних назначений центров")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsCentreToday(@PathVariable("centreName") String centreName) {
        log.info("Receiving all appointments for today at the medical center '{}'.", centreName);
        Date today = new Date();

        Center center = centerService.findByName(centreName);

        if (center == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Appointment> list = appointmentService.findAllByCentre(center);

        if (list == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<AppointmentDTO> dtos = new ArrayList<AppointmentDTO>();

        for (Appointment app : list) {
            if (DateUtil.getInstance().isSameDay(today, app.getDate())) {
                dtos.add(new AppointmentDTO(app));
            }
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }


    @GetMapping(value = "/center/getAllAppointmentsWeek/{centreName}")
    @ApiOperation("Еженедельное получение всех назначений центров")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsCentreWeekly(@PathVariable("centreName") String centreName) {
        log.info("Weekly receipt of all appointments to the medical center '{}'.", centreName);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date weekStart = cal.getTime();
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        Date weekEnd = cal.getTime();

        Center center = centerService.findByName(centreName);

        if (center == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Appointment> list = appointmentService.findAllByCentre(center);

        if (list == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<AppointmentDTO> dtos = new ArrayList<AppointmentDTO>();

        for (Appointment app : list) {
            if (app.getDate().after(weekStart) && app.getDate().before(weekEnd)) {
                dtos.add(new AppointmentDTO(app));
            }
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping(value = "/center/getAllAppointmentsMonth/{centreName}")
    @ApiOperation("Месячное получение всех назначений центров")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsCentreMonth(@PathVariable("centreName") String centreName) {
        log.info("Monthly receipt of all appointments to the medical center '{}'.", centreName);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date monthStart = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date monthEnd = cal.getTime();

        Center center = centerService.findByName(centreName);

        if (center == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Appointment> list = appointmentService.findAllByCentre(center);

        if (list == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<AppointmentDTO> dtos = new ArrayList<AppointmentDTO>();

        for (Appointment app : list) {
            if (app.getDate().after(monthStart) && app.getDate().before(monthEnd)) {
                dtos.add(new AppointmentDTO(app));
            }
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping(value = "/hall/getAll/{centreName}/{hallNumber}")
    @ApiOperation("Получение всех записей по аптекам")
    public ResponseEntity<List<AppointmentDTO>> getAllByHall(@PathVariable("centreName") String centreName, @PathVariable("hallNumber") int hallNumber) {
        log.info("Getting all pharmacy records for '{}', '{}'.", centreName, hallNumber);
        Center center = centerService.findByName(centreName);

        if (center == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Hall hall = hallService.findByNumberAndCentre(hallNumber, center);

        if (hall == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Appointment> apps = appointmentService.findAllByHallAndCentre(hall, center);
        List<AppointmentDTO> dtos = new ArrayList<AppointmentDTO>();

        for (Appointment app : apps) {
            AppointmentDTO dto = new AppointmentDTO(app);
            dto.setDate(app.getDate().toString());
            dto.setEndDate(app.getEndDate().toString());
            dtos.add(dto);
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping(value = "/patient/getAllRequests/{email}")
    @ApiOperation("Получение всех запросов пациентов")
    public ResponseEntity<List<AppointmentDTO>> getPatientRequests(@PathVariable("email") String email) {
        log.info("Receiving all patient requests by email '{}'.", email);
        Patient patient = patientService.findByEmail(email);

        if (patient == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<AppointmentRequest> list = appointmentRequestService.getAllByPatient(patient);

        List<AppointmentDTO> dtos = new ArrayList<AppointmentDTO>();

        for (AppointmentRequest req : list) {
            dtos.add(new AppointmentDTO(req));
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping(value = "/patient/getAll/{email}")
    @ApiOperation("Получение всех назначений пациентов")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments(@PathVariable("email") String email) {
        log.info("Receiving all appointments of patients by email '{}'.", email);
        Patient patient = null;

        try {
            patient = patientService.findByEmail(email);
        } catch (ClassCastException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders header = new HttpHeaders();

        if (patient == null) {
            header.set("responseText", "User not found : (" + email + ")");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments = appointmentService.findAllByPatient(patient);
        List<AppointmentDTO> dto = new ArrayList<AppointmentDTO>();

        for (Appointment app : appointments) {
            dto.add(new AppointmentDTO(app));
        }

        if (appointments == null) {
            header.set("responseText", "Appointments not found : (" + email + ")");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping(value = "/getAllPredefined")
    @ApiOperation("Получение всех заданий")
    public ResponseEntity<AppointmentDTO[]> getPredefined() {
        log.info("Getting all tasks.");
        List<Appointment> appointments = appointmentService.findAllByPredefined();

        if (appointments.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<AppointmentDTO> dtos = new ArrayList<AppointmentDTO>();

        for (Appointment app : appointments) {
            if (app.getPatient() != null) {
                dtos.add(new AppointmentDTO(app));
            }
        }

        return new ResponseEntity<>(dtos.toArray(new AppointmentDTO[dtos.size()]), HttpStatus.OK);
    }

    @GetMapping(value = "/doctor/getAllAppointments/{email}")
    @ApiOperation("Получение всех записей на приемы к докторам")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsDoctor(@PathVariable("email") String email) {
        log.info("Receiving all records for appointments with doctors by email '{}'.", email);
        Doctor doctor = null;

        try {
            doctor = doctorService.findByEmail(email);
        } catch (ClassCastException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders header = new HttpHeaders();

        if (doctor == null) {
            header.set("responseText", "Doctor not found : (" + email + ")");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments = doctor.getAppointments();
        List<AppointmentDTO> dto = new ArrayList<AppointmentDTO>();

        for (Appointment app : appointments) {
            dto.add(new AppointmentDTO(app));
        }

        if (appointments == null) {
            header.set("responseText", "Appointments not found : (" + email + ")");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping(value = "/doctor/getAllAppointmentsByDate/{email}/{date}")
    @ApiOperation("Получение всех записей на приемы к докторам по дате")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsDoctorByDate(@PathVariable("email") String email, @PathVariable("date") String date) {
        log.info("Retrieve all records for appointments with doctors by email '{}' and date '{}'.", email, date);
        Doctor doctor = null;

        try {
            doctor = doctorService.findByEmail(email);
        } catch (ClassCastException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders header = new HttpHeaders();

        if (doctor == null) {
            header.set("responseText", "Doctor not found : (" + email + ")");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments = doctor.getAppointments();
        List<AppointmentDTO> dto = new ArrayList<AppointmentDTO>();

        for (Appointment app : appointments) {
            Boolean sameDay = DateUtil.getInstance().isSameDay(app.getDate(), DateUtil.getInstance().getDate(date, "dd-MM-yyyy"));
            if (sameDay) {
                dto.add(new AppointmentDTO(app));
            }
        }

        if (appointments == null) {
            header.set("responseText", "Appointments not found : (" + email + ")");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping(value = "/doctor/getAllAppointmentsCalendar/{email}")
    @ApiOperation("Получение всех записей на приемы к докторам в календаре")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsDoctorCalendar(@PathVariable("email") String email) {
        log.info("Receiving all entries for appointments with doctors in the calendar by email '{}'.", email);
        Doctor doctor = null;

        try {
            doctor = doctorService.findByEmail(email);
        } catch (ClassCastException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders header = new HttpHeaders();

        if (doctor == null) {
            header.set("responseText", "Doctor not found : (" + email + ")");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments = doctor.getAppointments();
        List<AppointmentDTO> dto = new ArrayList<AppointmentDTO>();

        for (Appointment app : appointments) {
            //if(app.getPatient() != null && !app.getPredefined())
            {
                AppointmentDTO dt = new AppointmentDTO(app);
                dt.setDate(app.getDate().toString());
                dt.setEndDate(app.getEndDate().toString());
                dto.add(dt);
            }
        }

        if (appointments == null) {
            header.set("responseText", "Appointments not found : (" + email + ")");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);

    }

    @PostMapping(value = "/makePredefined")
    @ApiOperation("Cоздание заданий")
    public ResponseEntity<Void> makePredefined(@RequestBody AppointmentDTO dto) {
        log.info("Creating tasks in the medical center '{}'.", dto.getCentreName());
        HttpHeaders header = new HttpHeaders();
        Center center = centerService.findByName(dto.getCentreName());

        if (center == null) {
            header.set("responseText", "Center " + dto.getCentreName() + " is not found");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        Patient patient = patientService.findByEmail(dto.getPatientEmail());

        if (patient == null) {
            log.info("PATIENT='{}' was not found.", dto.getPatientEmail());
            header.set("message", "Patient not found: " + dto.getPatientEmail());
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        Date date = DateUtil.getInstance().getDate(dto.getDate(), "yyyy-MM-dd HH:mm");
        Date endDate = DateUtil.getInstance().getDate(dto.getEndDate(), "yyyy-MM-dd HH:mm");

        Hall hall = hallService.findByNumberAndCentre(dto.getHallNumber(), center);

        if (hall == null) {
            header.set("responseText", "Hall " + dto.getHallNumber() + " is not found");

            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments = appointmentService.findAllByHallAndCentre(hall, center);

        for (Appointment app : appointments) {
            DateInterval d1 = new DateInterval(app.getDate(), app.getEndDate());
            DateInterval d2 = new DateInterval(date, endDate);
            if (DateUtil.getInstance().overlappingInterval(d1, d2)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        Priceslist p = priceslistService.findByTypeOfExaminationAndCentre(dto.getTypeOfExamination(), dto.getCentreName());

        if (p == null) {
            header.set("responseText", "Priceslist " + dto.getTypeOfExamination() + " is not found");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        ArrayList<Doctor> doctors = new ArrayList<Doctor>();

        for (String email : dto.getDoctors()) {
            Doctor d = doctorService.findByEmail(email);

            for (Appointment app : d.getAppointments()) {
                DateInterval d1 = new DateInterval(app.getDate(), app.getEndDate());
                DateInterval d2 = new DateInterval(date, endDate);

                if (DateUtil.getInstance().overlappingInterval(d1, d2)) {
                    return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
                }
            }

            doctors.add(d);
        }

        Appointment a = appointmentService.findAppointment(date, hall, center);

        if (a != null) {
            return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
        }

        Appointment app = new Appointment.Builder(date)
                .withEndingDate(endDate)
                .withCentre(center)
                .withHall(hall)
                .withType(dto.getType())
                .withPriceslist(p)
                .withDoctors(doctors)
                .withPatient(patient)
                .build();

        app.setPredefined(true);
        appointmentService.save(app);

        for (Doctor d : doctors) {
            d.getAppointments().add(app);
            userService.save(d);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping(value = "/confirmRequest")
    @ApiOperation("Подтверждение запросов на приемы")
    public ResponseEntity<Void> confirmAppointmentRequest(@RequestBody AppointmentDTO dto, HttpServletRequest httpRequest) {
        log.info("Confirmation of requests for appointments at the medical center '{}'.", dto.getCentreName());
        HttpHeaders header = new HttpHeaders();
        AppointmentRequest request = appointmentRequestService.findAppointmentRequest(dto.getDate(), dto.getHallNumber(), dto.getCentreName());

        if (request == null) {
            header.set("responseText", "Request not found: " + dto.getDate() + " ," + dto.getHallNumber() + ", " + dto.getCentreName());
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        Hall hall = hallService.findByNumberAndCentre(dto.getHallNumber(), request.getCenter());

        if (hall == null) {
            header.set("responseText", "Hall not found " + dto.getHallNumber());
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        List<Appointment> apps = appointmentService.findAllByHall(hall);
        ArrayList<Doctor> doctors = new ArrayList<>();
        DateUtil util = DateUtil.getInstance();
        Date desiredStartTime = util.getDate(dto.getDate(), "yyyy-MM-dd HH:mm");
        Date desiredEndTime = util.getDate(dto.getEndDate(), "yyyy-MM-dd HH:mm");

        String parts[] = dto.getNewDate().split(" ");
        String dat = parts[0];

        if (!dat.equals("undefined")) {
            desiredStartTime = util.getDate(dto.getNewDate(), "yyyy-MM-dd HH:mm");
            desiredEndTime = util.getDate(dto.getNewEndDate(), "yyyy-MM-dd HH:mm");
        }

        for (String email : dto.getDoctors()) {
            Doctor doctor = doctorService.findByEmail(email);
            doctors.add(doctor);
        }

        Appointment appointment = new Appointment.Builder(desiredStartTime)
                .withCentre(request.getCenter())
                .withHall(hall)
                .withPatient(request.getPatient())
                .withType(request.getAppointmentType())
                .withPriceslist(request.getPriceslist())
                .withEndingDate(desiredEndTime)
                .withDoctors(doctors)
                .build();

        appointment.setConfirmed(false);

        try {
            appointmentService.confirmAppointmentRequest(appointment, dto);
            appointmentRequestService.delete(request);

            if (appointment.getAppointmentType() == Appointment.AppointmentType.Examination) {
                for (Doctor doctor : doctors) {
                    notificationService.sendNotification(doctor.getEmail(), "A new reception is scheduled", "A new reception is scheduled in your work calendar.\nThe date of the reception is " + desiredStartTime + ", in the center  " + appointment.getCenter().getName() + ", in the hall " + appointment.getHall().getName() + ", number " + appointment.getHall().getNumber() + ".");

                }

                if (!dat.equals("undefined")) {
                    notificationService.sendNotification(request.getPatient().getEmail(), "Your reception is scheduled.", "Request for examination accepted.\nThe date of the reception is " + dto.getDate() + ", in the center  " + appointment.getCenter().getName() + ", in the hall " + appointment.getHall().getName() + ", number " + appointment.getHall().getNumber() + ".");
                } else {
                    notificationService.sendNotification(request.getPatient().getEmail(), "Reception date changed.", "The date of the reception, which was scheduled " + dto.getDate() + ", it has been changed to " + dto.getNewDate() + ". The reception is scheduled in the center  " + appointment.getCenter().getName() + ", in the hall " + appointment.getHall().getName() + ", number " + appointment.getHall().getNumber() + ".");
                }

            }

            String requestURL = httpRequest.getRequestURL().toString();
            UriComponentsBuilder builderRootAccept = UriComponentsBuilder.fromUriString(requestURL.split("api")[0])
                    .queryParam("center", appointment.getCenter().getName())
                    .queryParam("date", DateUtil.getInstance().getString(appointment.getDate(), "yyyy-MM-dd HH:mm"))
                    .queryParam("hall", appointment.getHall().getNumber())
                    .queryParam("confirmed", true);

            UriComponentsBuilder builderRootDeny = UriComponentsBuilder.fromUriString(requestURL.split("api")[0])
                    .queryParam("center", appointment.getCenter().getName())
                    .queryParam("date", DateUtil.getInstance().getString(appointment.getDate(), "yyyy-MM-dd HH:mm"))
                    .queryParam("hall", appointment.getHall().getNumber())
                    .queryParam("confirmed", false);

            notificationService.sendNotification("prerecover07@gmail.com", "Confirm preview", "The center administrator has approved your request for examination.\nConfirm by going to the link:" + builderRootAccept.toUriString() + " Refuse by going to the link:" + builderRootDeny.toUriString());

            notificationService.sendNotification(appointment.getDoctors().get(0).getEmail(), "Admin has booked an appointment to review", "Admin booked a date review " + DateUtil.getInstance().getString(appointment.getDate(), "yyyy-MM-dd HH:mm") + ", in the center " + appointment.getCenter().getName() + ", in Room № " + appointment.getHall().getNumber() + " and he choose you as a doctor.");

        } catch (ConcurrentModificationException e) {
            header.set("responseText", "conflict");
            return new ResponseEntity<>(header, HttpStatus.CONFLICT);
        } catch (ValidationException e) {
            if (e.getMessage() == "Hall") {
                header.set("responseText", "hall");
                return new ResponseEntity<>(header, HttpStatus.CONFLICT);
            } else {
                header.set("responseText", e.getMessage());
            }
            return new ResponseEntity<>(header, HttpStatus.CONFLICT);

        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(value = "/confirmAppointment")
    @ApiOperation("Подтвердить приемы")
    public ResponseEntity<Void> confirmAppointment(@RequestBody AppointmentDTO dto) {
        log.info("Confirmation of appointments at the medical center '{}'.", dto.getCentreName());
        Appointment app = appointmentService.findAppointment(dto.getDate(), dto.getHallNumber(), dto.getCentreName());

        if (app == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        app.setConfirmed(false);
        appointmentService.save(app);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/denyAppointment")
    @ApiOperation("Удаление записей на приемы")
    public ResponseEntity<Void> denyAppointment(@RequestBody AppointmentDTO dto) {
        log.info("Deleting appointments at the medical center '{}'.", dto.getCentreName());
        HttpHeaders header = new HttpHeaders();
        Appointment app = appointmentService.findAppointment(dto.getDate(), dto.getHallNumber(), dto.getCentreName());

        if (app == null)
        {
            header.set("responseText", "Appointment not found: " + dto.getDate() + " ," + dto.getHallNumber() + ", " + dto.getCentreName());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        appointmentService.delete(app);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/denyRequest")
    @ApiOperation("Отклонение запросов на приемы")
    public ResponseEntity<Void> denyAppointmentRequest(@RequestBody AppointmentDTO dto) {
        log.info("Rejection of appointments at the medical center '{}'.", dto.getCentreName());
        HttpHeaders header = new HttpHeaders();
        AppointmentRequest request = appointmentRequestService.findAppointmentRequest(dto.getDate(), dto.getPatientEmail(), dto.getCentreName());

        if (request == null) {
            header.set("responseText", "Request not found: " + dto.getDate() + " ," + dto.getHallNumber() + ", " + dto.getCentreName());
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        //Send mail
        notificationService.sendNotification(dto.getPatientEmail(), "Your request for examination has been denied", "Your request for review(" + request.getPriceslist().getTypeOfExamination() + ") date " + dto.getDate() + " was rejected by the center administrator.");
        appointmentRequestService.delete(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/sendRequest")
    @ApiOperation("Добавление новых записей на приемы")
    public ResponseEntity<Void> addAppointmentRequest(@RequestBody AppointmentDTO dto) {
        log.info("Adding new entries for appointments at the medical center '{}'.", dto.getCentreName());
        HttpHeaders header = new HttpHeaders();
        AppointmentRequest request = new AppointmentRequest();
        request.setTimestamp(DateUtil.getInstance().getDate(new Date().getTime(), "yyyy-MM-dd HH:mm"));
        Center center = centerService.findByName(dto.getCentreName());

        AppointmentRequest databaseRequest = appointmentRequestService.findAppointmentRequest(dto.getDate(), dto.getHallNumber(), dto.getCentreName());

        if (databaseRequest != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        if (center == null) {
            log.info("CENTRE='{}' was not found.", dto.getCentreName());
            header.set("message", "Center not found: " + dto.getCentreName());
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }
        request.setCenter(center);

        Patient patient = patientService.findByEmail(dto.getPatientEmail());

        if (patient == null) {
            log.info("PATIENT='{}' was not found.", dto.getPatientEmail());
            header.set("message", "Patient not found: " + dto.getPatientEmail());
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }
        request.setPatient(patient);

        Date date = DateUtil.getInstance().getDate(dto.getDate(), "yyyy-MM-dd HH:mm");

        request.setDate(date);
        request.setAppointmentType(dto.getType());

        for (String email : dto.getDoctors()) {
            Doctor doctor = doctorService.findByEmail(email);

            if (doctor == null) {
                log.info("DOCTOR='{}' was not found.", email);
                header.set("message", "Doctor not found: " + email);
                return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
            }

            request.getDoctors().add(doctor);
        }

        Hall hall = hallService.findByNumberAndCentre(dto.getHallNumber(), center);

        if (hall == null) {
            log.info("HALL='{}' was not found.", dto.getHallNumber());
            header.set("message", "Hall not found: " + dto.getHallNumber());
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }
        request.setHall(hall);

        Priceslist pl = priceslistService.findByTypeOfExaminationAndCentre(dto.getTypeOfExamination(), center);

        if (pl == null) {
            System.out.println("PRICELIST");
            header.set("message", "Priceslist not found: " + dto.getTypeOfExamination());
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        request.setPriceslist(pl);

        List<User> admins = userService.getAll(UserRole.CentreAdmin);

        for (User user : admins) {
            CenterAdmin admin = (CenterAdmin) user;

            if (admin.getCenter().getName().equals(center.getName())) {
                notificationService.sendNotification(admin.getEmail(), "New review request.", "You have a new examination request..");
            }
        }

        request.setAppointmentType(dto.getType());

        try {
            appointmentRequestService.saveLock(request);
        } catch (ConcurrentModificationException e) {
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @DeleteMapping(value = "/cancelRequest/{role}")
    @ApiOperation("Отмены запросов на приемы")
    public ResponseEntity<Void> cancelAppointmentRequest(@RequestBody AppointmentDTO dto, @PathVariable("role") UserRole role) {
        log.info("Cancellation of requests for appointments at the medical center '{}' for '{}'.", dto.getCentreName(), role);
        AppointmentRequest req = appointmentRequestService.findAppointmentRequest(dto.getDate(), dto.getPatientEmail(), dto.getCentreName());

        if (req == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Date tm = req.getTimestamp();

        Date date = new Date();

        if (date.getTime() > tm.getTime() + 24 * DateUtil.HOUR_MILLIS) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<User> admins = userService.getAll(UserRole.CentreAdmin);
        Patient p = req.getPatient();

        if (role == UserRole.Patient) {
            for (User user : admins) {
                CenterAdmin admin = (CenterAdmin) user;

                if (admin.getCenter().getName().equals(dto.getCentreName())) {

                    notificationService.sendNotification(admin.getEmail(), "The patient cancelled the examination.", "Request for review scheduled for " + dto.getDate() + " is canceled by the patient: " + p.getEmail());
                }
            }

            notificationService.sendNotification(p.getEmail(), "Your examination request has been cancelled.", "Request for review scheduled for " + dto.getDate() + "has been cancelled at your request.");
        } else {
            for (User user : admins) {
                CenterAdmin admin = (CenterAdmin) user;

                if (admin.getCenter().getName().equals(dto.getCentreName())) {

                    notificationService.sendNotification(admin.getEmail(), "Review cancelled.", "Request for review scheduled for " + dto.getDate() + " was canceled by the center administrator.");
                }
            }

            notificationService.sendNotification(p.getEmail(), "Your examination request has been cancelled.", "Request for review scheduled for " + dto.getDate() + " cancelled by admin.");
        }

        appointmentRequestService.delete(req);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(value = "/reservePredefined/{email}")
    @ApiOperation("Зарезирвировать заранее")
    public ResponseEntity<Void> reservePredefined(@RequestBody AppointmentDTO dto, @PathVariable("email") String email) {
        log.info("Book in advance at the medical center '{}' for '{}'.", dto.getCentreName(), email);
        HttpHeaders headers = new HttpHeaders();

        Patient p = patientService.findByEmail(email);

        if (p == null) {
            headers.set("responseText", "Patient with email " + email + " not found");
            return new ResponseEntity<>(headers, HttpStatus.NOT_FOUND);
        }

        Appointment app = appointmentService.findAppointment(dto.getDate(), dto.getHallNumber(), dto.getCentreName());

        if (app == null) {
            headers.set("responseText", "Appointment not found for: " + dto.getDate() + " " + dto.getHallNumber() + " " + dto.getCentreName());
            return new ResponseEntity<>(headers, HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments = appointmentService.findAllByPatient(p);

        for (Appointment appointment : appointments) {
            DateInterval interval1 = new DateInterval(appointment.getDate(), appointment.getEndDate());
            DateInterval interval2 = new DateInterval(app.getDate(), app.getEndDate());

            if (DateUtil.getInstance().overlappingInterval(interval1, interval2)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        app.setPatient(p);
        app.setVersion(dto.getVersion());

        try {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("You have successfully scheduled an examination(");
            strBuilder.append(app.getPriceslist().getTypeOfExamination());
            strBuilder.append(") for date ");
            strBuilder.append(app.getDate());
            strBuilder.append(" the center ");
            strBuilder.append(app.getCenter().getName());
            strBuilder.append(" in Room № ");
            strBuilder.append(app.getHall().getNumber());
            strBuilder.append(". Your doctor is ");
            strBuilder.append(app.getDoctors().get(0).getFirstname() + " " + app.getDoctors().get(0).getLastname());
            strBuilder.append(". The price of the examination is ");
            strBuilder.append(app.getPriceslist().getPrice());
            strBuilder.append("rsd.");
            notificationService.sendNotification(p.getEmail(), "You made an appointment!", strBuilder.toString());
            appointmentService.saveLock(app);
        } catch (ObjectOptimisticLockingFailureException e) {
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(value = "/appointmentIsDone")
    @ApiOperation("Приемы закончены")
    public ResponseEntity<Void> appointmentIsDone(@RequestBody AppointmentDTO dto) {
        log.info("Completed appointments at the medical center '{}'.", dto.getCentreName());
        HttpHeaders headers = new HttpHeaders();

        Appointment app = appointmentService.findAppointment(dto.getDate(), dto.getHallNumber(), dto.getCentreName());

        if (app == null) {
            headers.set("responseText", "Appointment not found for: " + dto.getDate() + " " + dto.getHallNumber() + " " + dto.getCentreName());
            return new ResponseEntity<>(headers, HttpStatus.NOT_FOUND);
        }

        app.setDone(false);
        appointmentService.save(app);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
