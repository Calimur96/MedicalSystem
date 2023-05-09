package controller;

import dto.CodeDto;
import dto.DenyRegisterDTO;
import dto.RegistrationRequestDTO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.ConfirmationCodes;
import model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import service.ConfirmationCodesService;
import service.NotificationService;
import service.UserService;

@RestController
@RequestMapping("/api/reg")
@Slf4j
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    private final NotificationService notificationService;

    private final ConfirmationCodesService confirmationCodesService;

    //Метод первичной регистрации
    @PostMapping(value = "/registerRequest", consumes = "application/json")
    public ResponseEntity<Void> signUpFirstStep(@RequestBody RegistrationRequestDTO req) {
        if (userService.findByEmail(req.getEmail()) != null) {
            return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
        }
        User user = new User(req);
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userService.save(user);

        int emailVerificationCode = codeGeneration();
        int phoneVerificationCode = codeGeneration();

        ConfirmationCodes confirmationCodes = new ConfirmationCodes();
        confirmationCodes.setId(user.getId());
        confirmationCodes.setEmailVerificationCode(emailVerificationCode);
        confirmationCodes.setPhoneVerificationCode(phoneVerificationCode);
        confirmationCodesService.save(confirmationCodes);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping(value = "/confirmRegister/{email}")
    @ApiOperation("Подтверждение электронной почты при регистрации")
    public ResponseEntity<Void> confirmRegister(@PathVariable("email") String email) {
        log.info("Password confirmation during registration with email '{}'.", email);
        User user = userService.findByEmail(email);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ConfirmationCodes confirmationCodes = confirmationCodesService.findByUserId(user.getId());

        notificationService.sendNotification(user.getEmail(), "Registration Center",
                "Your request for registration for the Medical Center has been accepted.\nYour registration confirmation code:\n" + confirmationCodes.getEmailVerificationCode());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/verifyAccount/{email}")
    @ApiOperation("Проверка и обновление cозданного аккаунта")
    public ResponseEntity<Void> verifyAccountByEmail(@PathVariable("email") String email, @RequestBody CodeDto codeDto) {
        log.info("Checking and updating the created account with email '{}'.", email);
        User u = userService.findByEmail(email);
        ConfirmationCodes confirmationCodes = confirmationCodesService.findByUserId(u.getId());

        if (u == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (u.getVerifiedEmail()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        if (codeDto.getCode() != confirmationCodes.getEmailVerificationCode()){
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        u.setVerifiedEmail(true);
        userService.save(u);
        return new ResponseEntity<>(HttpStatus.OK);
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

        if (u.getVerifiedPhone()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        u.setVerifiedPhone(true);
        userService.save(u);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public int codeGeneration() {
        int generatedNumber = (int) (Math.random() * 10000);
        return generatedNumber;
    }

}
