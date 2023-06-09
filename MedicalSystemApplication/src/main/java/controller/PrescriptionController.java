package controller;
import dto.PrescriptionDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import model.Prescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import service.PrescriptionService;
import service.UserService;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "api/prescription")
@CrossOrigin
@Api
public class PrescriptionController {
    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/getAllPrescriptions")
    @ApiOperation("Получение всех рецептов")
    public ResponseEntity<List<PrescriptionDTO>> getDrugs() {
        log.info("Getting all prescriptions.");
        List<Prescription> prescriptions = prescriptionService.findAll();
        List<PrescriptionDTO> prescriptionsDTO = new ArrayList<>();
        if (prescriptions == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for (Prescription p : prescriptions) {
            PrescriptionDTO dto = new PrescriptionDTO(p);
            if (p.getIsValid() == true) {
                prescriptionsDTO.add(dto);
            }
        }

        return new ResponseEntity<>(prescriptionsDTO, HttpStatus.OK);
    }

    @PutMapping(value = "/validate/{email}")
    @ApiOperation("Подтверждение регистрации")
    public ResponseEntity<Void> confirmRegister(@RequestBody PrescriptionDTO dto, @PathVariable("email") String email) {
        log.info("Registration confirmation by '{}'.", email);
        dto.setNurseEmail(email);

        try {
            prescriptionService.validate(dto);

        } catch (ObjectOptimisticLockingFailureException e) {

            return new ResponseEntity<>(HttpStatus.CONFLICT);

        } catch (ValidationException e) {

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
