package controller;

import dto.DenyRegisterDTO;
import dto.LoginDTO;
import dto.RegistrationRequestDTO;
import dto.SessionUserDTO;
import helpers.SecurePasswordHasher;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import service.NotificationService;
import service.UserService;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HEAD;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "api/auth")
@CrossOrigin
@Api
public class AuthController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @PostMapping(value = "/login", consumes = "application/json")
    @ApiOperation("Вход")
    public ResponseEntity<SessionUserDTO> login(@RequestBody LoginDTO dto, HttpServletResponse response) {
        log.info("User login with email address '{}'.", dto.getEmail());
        HttpHeaders header = new HttpHeaders();

        User u = userService.findByEmail(dto.getEmail());

        if (u == null) {
            header.set("responseText", "User with that email doesn't exist!");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        if (!u.getVerifiedEmail()) {
            return new ResponseEntity<>(header, HttpStatus.UNAUTHORIZED);
        }

        String token = dto.getPassword();

        try {
            String hash = SecurePasswordHasher.getInstance().encode(token);

            if (hash.equals(u.getPassword())) {
                response.addCookie(new Cookie("email", u.getEmail()));
                return new ResponseEntity<>(new SessionUserDTO(u), HttpStatus.OK);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        header.set("Response", "Password incorrect!");
        return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/sessionUser")
    @ApiOperation("Получение и нахождения конкретного пользователя приложения")
    public ResponseEntity<SessionUserDTO> getSessionUser(@CookieValue(value = "email", defaultValue = "none") String email) {
        log.info("Getting and finding a specific application user ({}).", email);
        if (email == null || email == "none") {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }


        User user = userService.findByEmail(email);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        SessionUserDTO dto = new SessionUserDTO(user);

        return new ResponseEntity<SessionUserDTO>(dto, HttpStatus.OK);
    }

    @PostMapping(value = "/logout")
    @ApiOperation("Выход")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        log.info("User logout.");
        Cookie cookie = new Cookie("email", null);
        cookie.setMaxAge(0);

        response.addCookie(cookie);
        return new ResponseEntity<>(HttpStatus.OK);
    }

//    @GetMapping(value = "/getAllRegRequest")
//    @ApiOperation("Получение всех запросов на регистрацию")
//    public ResponseEntity<List<RegistrationRequestDTO>> getRegRequests() {
//        log.info("Receive all registration requests.");
//        List<RegistrationRequestDTO> ret = authService.getAll();
//
//        return new ResponseEntity<>(ret, HttpStatus.OK);
//    }



}
