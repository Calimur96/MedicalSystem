package controller;

import dto.DenyRegisterDTO;
import dto.DoctorDTO;
import dto.RegistrationRequestDTO;
import dto.UserDTO;
import helpers.SecurePasswordHasher;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import service.NotificationService;
import service.UserService;

import javax.persistence.Access;
import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/reg")
@Slf4j
public class RegistrationController {
    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;
    //Метод первичной регистрации
    @PostMapping(value = "/registerRequest", consumes = "application/json")
    public ResponseEntity<Void> signUpFirstStep(@RequestBody RegistrationRequestDTO req){
        if (userService.findByEmail(req.getEmail())!= null){
            return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
        }
        User user = new User(req);
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setVerified(false);
        userService.save(user);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    @GetMapping(value = "/verifyAccount/{email}")
    @ApiOperation("Проверка и обновление cозданного аккаунта")
    public ResponseEntity<Void> verifyAccountByEmail(@PathVariable("email") String email) {
        log.info("Checking and updating the created account with email '{}'.", email);
        User u = userService.findByEmail(email);

        if (u == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (u.getVerified()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        u.setVerified(true);
        userService.save(u);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/confirmRegister/{email}")
    @ApiOperation("Подтверждение пароля при регистрации")
    public ResponseEntity<Void> confirmRegister(@PathVariable("email") String email, HttpServletRequest httpRequest) {
        log.info("Password confirmation during registration with email '{}'.", email);
        User user = userService.findByEmail(email);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

            String verifyAccountUrl = getVerifyAccountUrl(httpRequest, email);
            notificationService.sendNotification(user.getEmail(), "Registration Center",
                    "Your request for registration for the Medical Center has been accepted.\nPlease confirm your registration by visiting the link:\n" + verifyAccountUrl);

            return new ResponseEntity<>(HttpStatus.CREATED);

        }



    @DeleteMapping(value = "/denyRegister")
    @ApiOperation("Отмена регистрации")
    public ResponseEntity<Void> denyRegistration(@RequestBody DenyRegisterDTO denyRegisterDTO) {
        String email = denyRegisterDTO.getEmail();
        log.info("Cancellation of registration: '{}'.", email);
        String rejectionReason = denyRegisterDTO.getRejectionReason();

       User user = userService.findByEmail(email);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            notificationService.sendNotification(user.getEmail(), "Registration Center",
                    "Your request for registration of an order for a Medical Center has been denied.\nThe reason for the refusal is as follows:\n" + "'" + rejectionReason + "'");
            userService.delete(user);
        } catch (MailException e) {
            log.info("Unregistration message for user '{}' was not sent!.", email);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/verifyAccount2/{phone}")
    @ApiOperation("Проверка и обновление cозданного аккаунта")
    public ResponseEntity<Void> verifyAccountByPhone(@PathVariable("phone") String phone) {
        log.info("Checking and updating the created account with phone '{}'.", phone);
        User u = userService.findByPhone(phone);

        if (u == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (u.getVerified()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        u.setVerified(true);
        userService.save(u);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    public String getVerifyAccountUrl(HttpServletRequest httpRequest, String email) {
        String requestURL = httpRequest.getRequestURL().toString();
        String url = requestURL.split("confirmRegister")[0];
        return UriComponentsBuilder.fromHttpUrl(url)
                .pathSegment("verifyAccount")
                .pathSegment(email)
                .toUriString();
    }

}
