package controller;
import dto.*;
import helpers.SecurePasswordHasher;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.User.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.*;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "api/users")
@CrossOrigin
@Api
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CenterService centerService;

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private PatientMedicalReportService patientMedicalReportService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private NurseService nurseService;

    @Autowired
    private CenterAdminService centerAdminService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @PutMapping(value = "/update/{email}")
    @ApiOperation("Обновление(добавление, изменение) данных пользователя")
    public ResponseEntity<Void> updateUser(@RequestBody UserDTO dto, @PathVariable("email") String email) {
        log.info("Changing user data with email '{}'.", email);
        User user = userService.findByEmail(email);

        if (user != null) {
            user.setUsername(dto.getUsername());
            user.setFirstname(dto.getFirstname());
            user.setLastname(dto.getLastname());
            user.setCity(dto.getCity());
            user.setState(dto.getState());
            user.setPhone(dto.getPhone());
            user.setDate_of_birth(dto.getDate_of_birth());
            userService.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping(value = "/update/password/{email}", consumes = "application/json")
    @ApiOperation("Обновление(изменение) пароля")
    public ResponseEntity<Void> updatePassword(@PathVariable("email") String email, @RequestBody PasswordDTO dto) {
        System.out.println(email);
        log.info("Changing the password of a user with email '{}'.", email);
        User user = userService.findByEmail(email);

        if(user != null) {
            try {
                String oldPassword = dto.getOldPassword();
                String oldHash = SecurePasswordHasher.getInstance().encode(oldPassword);

                if (user.getPassword().equals(oldHash)) {
                    String newPassword = dto.getNewPassword();
                    String newHash = SecurePasswordHasher.getInstance().encode(newPassword);
                    user.setPassword(newHash);
                    userService.save(user);
                    return new ResponseEntity<>(HttpStatus.OK);
                }

            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/getPatient/{email}")
    @ApiOperation("Получение данных пациентов")
    public ResponseEntity<PatientDTO> getPatient(@PathVariable("email") String email) {
        log.info("Getting data of patients by email '{}'.", email);
        Patient patient = patientService.findByEmail(email);

        if (patient == null || patient.getDeleted()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new PatientDTO(patient), HttpStatus.OK);
    }

    @GetMapping(value = "/getDoctor/{email}")
    @ApiOperation("Получение данных докторов")
    public ResponseEntity<DoctorDTO> getDoctor(@PathVariable("email") String email) {
        log.info("Getting data of doctors by email '{}'.", email);
        Doctor doctor = doctorService.findByEmail(email);

        if (doctor == null || doctor.getDeleted()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new DoctorDTO(doctor), HttpStatus.OK);
    }

    @GetMapping(value = "/getNurse/{email}")
    @ApiOperation("Получение данных медработников")
    public ResponseEntity<NurseDTO> getNurse(@PathVariable("email") String email) {
        log.info("Getting data of nurses by email '{}'.", email);
        Nurse nurse = nurseService.findByEmail(email);

        if (nurse == null || nurse.getDeleted()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new NurseDTO(nurse), HttpStatus.OK);
    }

    @GetMapping(value = "/getCentreAdmin/{email}")
    @ApiOperation("Получение данных администраторов центров")
    public ResponseEntity<CenterAdminDTO> getCentreAdmin(@PathVariable("email") String email) {
        log.info("Getting data of Center Administrator by email '{}'.", email);
        CenterAdmin centerAdmin = centerAdminService.findByEmail(email);

        if (centerAdmin == null || centerAdmin.getDeleted()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new CenterAdminDTO(centerAdmin), HttpStatus.OK);
    }

    @GetMapping(value = "/getUser/{email}")
    @ApiOperation("Получение данных обычных пользователей")
    public ResponseEntity<UserDTO> getUser(@PathVariable("email") String email) {
        log.info("Getting user data by email '{}'.", email);
        User user = userService.findByEmail(email);

        if (user == null || user.getDeleted()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new UserDTO(user), HttpStatus.OK);
    }

    @GetMapping(value = "/{userId}")
    @ApiOperation("Получение профилей пользователей по Id")
    public ResponseEntity<UserDTO> getUserProfile(@PathVariable("userId") String userId) {
        log.info("Getting user data by id '{}'.", userId);
        User user = userService.findById(Long.parseLong(userId));

        if (user == null || user.getDeleted()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new UserDTO(user), HttpStatus.OK);
    }


    @GetMapping(value = "/getAll/{role}")
    @ApiOperation("Получение всех пользователей по конкретным role")
    public ResponseEntity<List<UserDTO>> geAllUserByRole(@PathVariable("role") UserRole role) {
        log.info("Getting all users by role '{}'.", role);
        List<User> user = userService.getAll(role);
        List<UserDTO> dtos = new ArrayList<UserDTO>();
        for(User u : user) {
            if(!u.getDeleted()) {
                dtos.add(new UserDTO(u));
            }
        }

        if(user == null) {
            return new ResponseEntity<>(dtos, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping(value = "/getAll")
    @ApiOperation("Получение всех пользователей")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Getting all users.");
        List<User> user = userService.getAll();
        List<UserDTO> dtos = new ArrayList<UserDTO>();
        for(User u : user) {
            if (!u.getDeleted()) {
                dtos.add(new UserDTO(u));
            }
        }

        if(user == null) {
            return new ResponseEntity<>(dtos, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping(value = "/patient/getMedicalRecord/{email}")
    @ApiOperation("Получение медкарт пациентов")
    public ResponseEntity<MedicalRecordDTO> getMedicalRecord(@PathVariable("email") String email) {
        log.info("Getting patient medical records by email '{}'.", email);
        Patient patient = patientService.findByEmail(email);

        if (patient == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        MedicalRecord mr = medicalRecordService.findByPatient(patient);
        MedicalRecordDTO dto = new MedicalRecordDTO(mr);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PutMapping(value = "/patient/updateMedicalRecord/{email}")
    @ApiOperation("Обновление(изменение) данных медкарт пациентов")
    ResponseEntity<Void> updateMedicalRecord(@PathVariable("email") String email, @RequestBody MedicalRecordDTO dto) {
        log.info("Changing the data of the patient's medical record with the email '{}'.", email);
        Patient patient = patientService.findByEmail(email);
        MedicalRecord record = medicalRecordService.findByPatient(patient);

        if (patient == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (record == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        record.setBloodType(dto.getBloodType());
        record.setHeight(dto.getHeight());
        record.setWeight(dto.getWeight());
        record.setAlergies(dto.getAlergies());
        medicalRecordService.save(record);
        patient.setMedicalRecord(record);
        patientService.save(patient);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
