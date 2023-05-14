package controller;

import com.infobip.ApiException;
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
import service.SMSSender;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/reg")
@Slf4j
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final ConfirmationCodesService confirmationCodesService;
    private final SMSSender smsSender;

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

    @PostMapping(value = "/confirmRegisterEmail/{email}")
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

    @PostMapping(value = "/confirmRegisterPhone/{phone}")
    public ResponseEntity<Void> verifyAccountByPhone(@PathVariable("phone") String phone, HttpServletRequest request) throws IOException {
        log.info("Password confirmation during registration with phone '{}'.", phone);
        User user = userService.findByPhone(phone);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ConfirmationCodes confirmationCodes = confirmationCodesService.findByUserId(user.getId());
        String messageText = "Your request for registration for the Medical Center has been accepted. Your phone verification code: " + confirmationCodes.getPhoneVerificationCode();

        try {
            smsSender.sendMessage(phone, messageText);
            log.info("SMS was successfully sent to the number '{}'.", phone);
        } catch (Exception e) {
            log.info("SMS was not successfully sent to the number '{}'", phone);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/verifyAccountEmail/{email}")
    @ApiOperation("Верификация электронного ящика cозданного аккаунта")
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

    @PostMapping(value = "/verifyAccountPhone/{phone}")
    @ApiOperation("Верификация телефона cозданного аккаунта")
    public ResponseEntity<Void> verifyAccountByPhone(@PathVariable("phone") String phone, @RequestBody CodeDto codeDto) throws ApiException {
        log.info("Checking and updating the created account with phone '{}'.", phone);
        User u = userService.findByPhone(phone);
        ConfirmationCodes confirmationCodes = confirmationCodesService.findByUserId(u.getId());

        if (u == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (u.getVerifiedPhone()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        if (codeDto.getCode() != confirmationCodes.getPhoneVerificationCode()){
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        u.setVerifiedPhone(true);
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

    public int codeGeneration() {
        int generatedNumber = (int) (Math.random() * 10000);
        return generatedNumber;
    }

}
