package controller;

import dto.PatientDTO;
import helpers.SecurePasswordHasher;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Patient;
import model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.PatientService;
import service.UserService;

import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping(value = "api/patient")
@Api
@CrossOrigin
@Slf4j
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final UserService userService;

    @PostMapping(value = "/makeNewPatient", consumes = "application/json")
    @ApiOperation("Добавление нового пациента")
    public ResponseEntity<Void> makeNewPatient(@RequestBody PatientDTO dto) {
        User user = userService.findByEmail(dto.getUser().getEmail());
        log.info("Adding a new patient with email '{}'.", user.getEmail());
        String pass = user.getPassword();

        try {
            pass = SecurePasswordHasher.getInstance().encode(pass);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Patient patient = new Patient(user);
        patient.setPassword(pass);
        patientService.save(patient);
        setUserDeleted(user);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private void setUserDeleted(User user) {
        user.setDeleted(true);
        userService.save(user);
    }

}
