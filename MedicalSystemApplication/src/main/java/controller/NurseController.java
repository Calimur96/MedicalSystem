package controller;

import dto.NurseDTO;
import helpers.SecurePasswordHasher;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Center;
import model.Nurse;
import model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.CenterService;
import service.NurseService;
import service.UserService;

import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/nurses")
@CrossOrigin
@Api
@RequiredArgsConstructor
public class NurseController {

    private final CenterService centerService;
    private final NurseService nurseService;
    private final UserService userService;

    @GetMapping(value = "/getNursesList/{id}")
    @ApiOperation("Получение списка специалистов центра по id центра")
    public ResponseEntity<List<Nurse>> getNursesListByCenterId(@PathVariable Long id) {
        log.info("Getting a List of nurses in center where id '{}'.", id);
        Center c = centerService.findById(id);
        List<Nurse> n = nurseService.findAllByCenter(c);

        if (n == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(n, HttpStatus.OK);
    }

    @PostMapping(value = "/makeNewNurse", consumes = "application/json")
    @ApiOperation("Добавление нового специалиста")
    public ResponseEntity<Void> addNewNurse(@RequestBody NurseDTO dto) {
        log.info("Adding a new Nurse to the center '{}'", dto.getCentreName());
        Nurse n = nurseService.findByEmail(dto.getUser().getEmail());
        Center c = centerService.getByName(dto.getCentreName());
        User user = userService.findByEmail(dto.getUser().getEmail());

        if (n != null) {
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

        Nurse nurse = new Nurse(dto);
        nurse.setPassword(pass);
        nurse.setCenter(c);
        nurseService.save(nurse);
        setUserDeleted(user);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private void setUserDeleted(User user) {
        user.setDeleted(true);
        userService.save(user);
    }

}
