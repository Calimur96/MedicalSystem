package controller;


import dto.CenterDTO;
import dto.UserDTO;
import helpers.SecurePasswordHasher;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import model.Center;
import model.CenterAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.AppointmentService;
import service.CenterAdminService;
import service.CenterService;
import service.UserService;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping(value = "/api/admins/center")
@CrossOrigin
@Api
public class CenterAdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private CenterService centerService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private CenterAdminService centerAdminService;


    @GetMapping(value = "/getCentreFromAdmin/{email}")
    @ApiOperation("Создание администраторов для центров")
    public ResponseEntity<CenterDTO> getCentreFromAdmin(@PathVariable("email") String email) {
        log.info("Creating an administrator for the center with the email '{}'.", email);
        CenterAdmin ca = centerAdminService.findByEmail(email);
        if (ca == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CenterDTO dto = new CenterDTO(ca.getCenter());
        return new ResponseEntity<CenterDTO>(dto, HttpStatus.OK);
    }

    @PostMapping(value = "/registerCentreAdmin/{centreName}")
    @ApiOperation("Поиск центров по Администраторам")
    public ResponseEntity<Void> registerCentreAdmin(@RequestBody UserDTO dto, @PathVariable("centreName") String centreName) {
        log.info("Search centers by admin with email '{}'.", centreName);
        CenterAdmin ca = centerAdminService.findByEmail(dto.getEmail());

        Center center = centerService.findByName(centreName);
        HttpHeaders header = new HttpHeaders();

        if (center == null) {
            header.set("Response", "Center not found");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        if (ca == null) {
            CenterAdmin centerAdmin = new CenterAdmin();
            centerAdmin.setUsername(dto.getUsername());
            centerAdmin.setFirstname(dto.getFirstname());
            centerAdmin.setCity(dto.getCity());
            centerAdmin.setLastname(dto.getLastname());
            centerAdmin.setState(dto.getState());
            centerAdmin.setPhone(dto.getPhone());
            centerAdmin.setDate_of_birth(dto.getDate_of_birth());
            centerAdmin.setEmail(dto.getEmail());
            centerAdmin.setVacationRequests(new ArrayList<>());
            centerAdmin.setAppointmentRequests(new ArrayList<>());
            centerAdmin.setCenter(center);
            String token = "admin1234";

            try {
                String hash = SecurePasswordHasher.getInstance().encode(token);
                centerAdmin.setPassword(hash);
                centerAdminService.save(centerAdmin);
                centerAdmin.setCenter(center);
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                header.set("Response", "Saving failed");
                return new ResponseEntity<>(header, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } else {
            header.set("Response", "Admin with that email already exists.");
            return new ResponseEntity<>(header, HttpStatus.ALREADY_REPORTED);
        }
    }
}
