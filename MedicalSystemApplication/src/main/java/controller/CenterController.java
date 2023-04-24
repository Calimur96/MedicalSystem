package controller;

import dto.CenterDTO;
import dto.CenterFilterDTO;
import dto.DoctorDTO;
import dto.UserDTO;
import filters.Filter;
import filters.FilterFactory;
import filters.PatientFilter;
import helpers.DateUtil;
import helpers.ListUtil;
import helpers.Scheduler;
import helpers.UserSortingComparator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping(value = "api/center")
@CrossOrigin
@Api
@RequiredArgsConstructor
public class CenterController {

    private final CenterService centerService;

    private final NurseService nurseService;

    private final AppointmentService appointmentService;

    private Center center;

    @PostMapping(value = "/registerCenter", consumes = "application/json")
    @ApiOperation("Создание центров")
    public ResponseEntity<Void> registerCentre(@RequestBody CenterDTO dto) {
        log.info("Creating a center named '{}'.", dto.getName());
        Center c = centerService.findByName((dto.getName()));

        if (c == null) {
            Center center = new Center();
            center.setName(dto.getName());
            center.setAddress(dto.getAddress());
            center.setCity(dto.getCity());
            center.setDescription(dto.getDescription());
            center.setState(dto.getState());
            center.setRating(dto.getRating());
            center.setDoctors(new ArrayList<>());
            center.setHalls(new ArrayList<>());
            center.setReviews(new ArrayList<>());
            centerService.save(center);
        } else {
            return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(value = "/getNurse/{nurseEmail}")
    @ApiOperation("Поиск центров по работающим в них медработникам")
    public ResponseEntity<CenterDTO> getCentreFromNurse(@PathVariable("nurseEmail") String nurseEmail) {
        log.info("Search for centers by health workers working in them with email '{}'.", nurseEmail);
        Nurse n = nurseService.findByEmail(nurseEmail);

        if (n == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CenterDTO dto = new CenterDTO(n.getCenter());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping(value = "/getAll")
    @ApiOperation("Поиск всех центров")
    public ResponseEntity<CenterDTO[]> getCentres() {
        log.info("Getting all medical centers.");
        List<Center> centers = centerService.findAllSafe();
        List<CenterDTO> centresDTO = new ArrayList<CenterDTO>();
        if (centers == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for (Center c : centers) {
            CenterDTO dto = new CenterDTO(c);
            centresDTO.add(dto);
        }

        return new ResponseEntity<>(centresDTO.toArray(new CenterDTO[centresDTO.size()]), HttpStatus.OK);
    }

    @GetMapping(value = "/findByCity/{city}")
    @ApiOperation("Поиск центров по городу")
    public ResponseEntity<List<Center>> getCenterByCity(@PathVariable String city){
        log.info("Getting all medical centers in city '{}'",city);
        List<Center> centers = centerService.findAllByCity(city);
        if (centers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(centers,HttpStatus.OK) ;
    }

    @GetMapping(value = "/findByState/{state}")
    @ApiOperation("Поиск центров по стране")
    public ResponseEntity<List<Center>> getCentersByState(@PathVariable String state){
        List<Center> centers = centerService.findAllByState(state);
        if (centers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(centers,HttpStatus.OK) ;

    }

    @GetMapping(value = "/findById/{id}")
    @ApiOperation("Поиск центра по id")
    public ResponseEntity<Center> findCenterById(@PathVariable("id") Long id) {
        log.info("Search center with id ('{}').", id);
        ResponseEntity<Center> resultEntity;

        try {
            center = centerService.findById(id);
            resultEntity = new ResponseEntity<>(center, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            System.err.println(e);
            resultEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return resultEntity;
    }

    @PostMapping(value = "/getAll/{date}/{type}")
    @ApiOperation("Поиск всех центров с фильтрами даты и типа")
    public ResponseEntity<CenterDTO[]> getCentresWithFilter(@RequestBody CenterFilterDTO dto, @PathVariable("date") String date, @PathVariable("type") String typeOfExamination) {
        log.info("Search all centers with date ('{}') and type ('{}') filters.", date, typeOfExamination);
        List<Center> centers = centerService.findAllSafe();
        List<CenterDTO> centresDTO = new ArrayList<CenterDTO>();

        if (centers == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Date realDate = DateUtil.getInstance().getDate(date, "dd-MM-yyyy");
        Filter filter = FilterFactory.getInstance().get("center");

        for (Center c : centers) {
            List<Doctor> doctors = c.getDoctors();

            for (Doctor d : doctors) {
                int freeTime = Scheduler.getFreeIntervals(d, realDate).size();

                if (d.IsFreeOn(realDate) && d.getType().equalsIgnoreCase(typeOfExamination) && freeTime > 0) {
                    if (filter.test(c, dto)) {
                        centresDTO.add(new CenterDTO());
                        break;
                    }
                }
            }
        }

        CenterDTO[] ret = centresDTO.toArray(centresDTO.toArray(new CenterDTO[centers.size()]));

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping(value = "/getPatients/{centerName}")
    @ApiOperation("Получение пациентов центров по названиям центров")
    public ResponseEntity<List<UserDTO>> getCentrePatients(@PathVariable("centerName") String centerName) {
        log.info("Get center patients by name '{}'.", centerName);
        Center c = centerService.findByName(centerName);

        if (c == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments = new ArrayList<Appointment>();
        ArrayList<UserDTO> patients = new ArrayList<UserDTO>();

        appointments = appointmentService.findAllByCentre(c);

        if (appointments.isEmpty()) {
            System.out.println("There's no check-up at that center");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for (Appointment app : appointments) {
            if (app.getPatient() == null)
                continue;

            if (!ListUtil.getInstance().containsWithEmail(patients, app.getPatient().getEmail())) {
                patients.add(new UserDTO(app.getPatient()));
            }
        }

        patients.sort(new UserSortingComparator());

        System.out.println(patients.size());

        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    @PostMapping(value = "/getPatientsByFilter/{centerName}", consumes = "application/json")
    @ApiOperation("Получение пациентов центров по названиям центром и фильтрация")
    public ResponseEntity<List<UserDTO>> getCentrePatientByFilter(@PathVariable("centerName") String centerName, @RequestBody UserDTO dto) {
        log.info("Get center patients by name '{}' with filtering.", centerName);
        Center c = centerService.findByName(centerName);

        if (c == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ArrayList<UserDTO> ret = new ArrayList<UserDTO>();
        List<Appointment> appointments = new ArrayList<Appointment>();

        PatientFilter filter = (PatientFilter) FilterFactory.getInstance().get("patient");
        appointments = appointmentService.findAllByCentre(c);

        if (appointments.isEmpty()) {
            System.out.println("There's no check-up at that center");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for (Appointment app : appointments) {
            Patient p = app.getPatient();

            if (p == null) {
                continue;
            }

            if (!ListUtil.getInstance().containsWithEmail(ret, p.getEmail())) {
                if (filter.test(app.getPatient(), dto)) {
                    ret.add(new UserDTO(app.getPatient()));
                }
            }
        }

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping(value = "/getDoctorsByType/{centerName}/{typeOfExamination}")
    @ApiOperation("Получение докторов по типам услуг и названиям центров")
    public ResponseEntity<List<DoctorDTO>> getClinicDoctorsByType(@PathVariable("centerName") String centerName, @PathVariable("typeOfExamination") String typeOfExamination) {
        log.info("Obtaining doctors by service types ('{}') and center names ('{}').", typeOfExamination, centerName);
        Center center = centerService.findByName(centerName);
        if (center == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Doctor> doctors = center.getDoctors();
        List<DoctorDTO> dtos = new ArrayList<DoctorDTO>();

        for (Doctor doc : doctors) {
            if (doc.getDeleted() == false && doc.getType().equalsIgnoreCase(typeOfExamination)) {
                DoctorDTO dto = new DoctorDTO(doc);
                dtos.add(dto);
            }
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping(value = "/getDoctorsByTypeAndVacation/{centerName}/{typeOfExamination}/{date}")
    @ApiOperation("Получение докторов по типам услуг, названиям центров и отпускам")
    public ResponseEntity<DoctorDTO[]> getCentreDoctorsByTypeAndVacation(@PathVariable("centerName") String centerName, @PathVariable("typeOfExamination") String typeOfExamination, @PathVariable("date") String date) {
        log.info("Obtaining doctors by service types ('{}'), center names ('{}') and vacations ('{}').", typeOfExamination, centerName, date);
        Center center = centerService.findByName(centerName);
        if (center == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Doctor> doctors = center.getDoctors();
        List<DoctorDTO> dtos = new ArrayList<DoctorDTO>();

        for (Doctor doc : doctors) {
            if (doc.getDeleted() == false && doc.getType().equalsIgnoreCase(typeOfExamination)
                    && doc.IsFreeOn(DateUtil.getInstance().getDate(date, "yyyy-MM-dd"))) {
                DoctorDTO dto = new DoctorDTO(doc);
                dtos.add(dto);
            }
        }

        return new ResponseEntity<>(dtos.toArray(new DoctorDTO[dtos.size()]), HttpStatus.OK);
    }
}
