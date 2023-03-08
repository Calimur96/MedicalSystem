package controller;

import dto.VacationDTO;
import helpers.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "api/vacation")
@CrossOrigin
@Api
public class VacationController {

    @Autowired
    private UserService userService;

    @Autowired
    private CentreService centreService;

    @Autowired
    private VacationRequestService vacationRequestService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VacationService vacationService;

    @PostMapping(value ="/checkAvailability/{centreName}")
    @ApiOperation("Проверка доступности тех или иных медработников по названиям центров")
    public ResponseEntity<Boolean> checkAvailabillity(@RequestBody VacationDTO vdto, @PathVariable("centreName") String centreName)
    {

        if (vdto.getUser() == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User user = userService.findByEmailAndDeleted(vdto.getUser().getEmail(), false);

        if (user == null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Date vacationStart = DateUtil.getInstance().getDate(vdto.getStartDate(), "dd-MM-yyyy");
        Date vacationEnd = DateUtil.getInstance().getDate(vdto.getEndDate(), "dd-MM-yyyy");

        List<VacationRequest> requests  = vacationRequestService.findAllByUser(user);

        if (requests != null)
        {
            for (VacationRequest request : requests)
            {

                if (vacationStart.before(request.getEndDate()) && vacationEnd.after(request.getStartDate())) {
                    return new ResponseEntity<>(false, HttpStatus.OK);
                }

            }
        }


        if (user instanceof Doctor) {
            Doctor doctor = (Doctor)user;

            List<Appointment> appointments = doctor.getAppointments();

            for (Appointment app : appointments) {
                Date date = app.getDate();

                if (date.after(vacationStart) && date.before(vacationEnd)) {
                    return new ResponseEntity<>(false, HttpStatus.OK);
                }
            }
        }

        else if (user instanceof Nurse) {

        }

        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PostMapping(value ="/makeVacationRequest/{centreName}")
    @ApiOperation("Создание нового запроса на отпуск")
    public ResponseEntity<Void> makeVacationRequest(@RequestBody VacationDTO vdto, @PathVariable("centreName") String centreName)
    {
        if (vdto.getUser() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User user = userService.findByEmailAndDeleted(vdto.getUser().getEmail(), false);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Centre centre = null;

        if (user instanceof Doctor) {
            Doctor doctor = (Doctor) user;
            centre = doctor.getCentre();
        }

        else if (user instanceof Nurse) {
            Nurse nurse = (Nurse)user;
            centre = nurse.getCentre();
        }

        VacationRequest vr = new VacationRequest();

        vr.setStartDate(DateUtil.getInstance().getDate(vdto.getStartDate(), "dd-MM-yyyy"));
        vr.setEndDate(DateUtil.getInstance().getDate(vdto.getEndDate(), "dd-MM-yyyy"));
        vr.setCentre(centre);
        vr.setUser(user);

        vacationRequestService.save(vr);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(value ="/getAllVacationRequestsByCentre/{centreName}")
    @ApiOperation("Получение всевозможных запросов на отпуски тех или иных медработников")
    public ResponseEntity<List<VacationDTO>> getAllVacationRequestsByCentre(@PathVariable("centreName") String centreName)
    {
        Centre c = centreService.findByName(centreName);

        if (c == null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<VacationRequest> vacationRequests = vacationRequestService.findAllByCentre(c);
        List<VacationDTO> vdto = new ArrayList<>();

        for (VacationRequest vrq : vacationRequests)
        {
            vdto.add(new VacationDTO(vrq));
        }

        return new ResponseEntity<>(vdto,HttpStatus.OK);

    }


    @PostMapping(value = "/confirmVacationRequest")
    @ApiOperation("Подтвердить запрос на отпуск")
    public ResponseEntity<Void> confirmVacationRequest(@RequestBody VacationDTO dto)
    {
        User user = userService.findByEmail(dto.getUser().getEmail());
        List<VacationRequest> vrq = vacationRequestService.findAllByUser(user);

        try
        {
            Vacation req = vacationRequestService.resolveVacationRequestLock(vrq, dto, true);
            notificationService.sendNotification(req.getUser().getEmail(), "Request for annual leave or leave ", "Dear," + "Your request for vacation or leave during the period of " + DateUtil.getInstance().getString(req.getStartDate(), "dd-MM-yyyy") + " due " + DateUtil.getInstance().getString(req.getEndDate(), "dd-MM-yyyy") + " is approved.");
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e)
        {
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

    }

    @DeleteMapping(value ="/denyVacationRequest/{denyText}")
    @ApiOperation("Отклонить запрос на отпуск")
    public ResponseEntity<Void> denyVacationRequest (@RequestBody VacationDTO dto, @PathVariable("denyText") String denyText)
    {
        User user = userService.findByEmail(dto.getUser().getEmail());
        List<VacationRequest> vrq = vacationRequestService.findAllByUser(user);

        try
        {
            Vacation req = vacationRequestService.resolveVacationRequestLock(vrq, dto, true);
            notificationService.sendNotification(req.getUser().getEmail(), "Request for annual leave or leave ", "Dear," + "Your request for vacation or leave during the period of " + DateUtil.getInstance().getString(req.getStartDate(), "dd-MM-yyyy") + " due " + DateUtil.getInstance().getString(req.getEndDate(), "dd-MM-yyyy") + " is denied.The reason for the refusal is as follows: " + denyText);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e)
        {
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

    }


    @GetMapping(value ="/getAllVacationsByUser/{email}")
    @ApiOperation("Получение всех отпусков по пользователям")
    public ResponseEntity<List<VacationDTO>> getAllVacationsByUser(@PathVariable ("email") String email)
    {
        User u = userService.findByEmail(email);

        if (u == null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Vacation> vacations = vacationService.findAllByUser(u);
        List<VacationDTO> vdto = new ArrayList<>();

        for (Vacation vac : vacations)
        {
            VacationDTO dt = new VacationDTO(vac);
            dt.setStartDate(vac.getStartDate().toString());
            dt.setEndDate(vac.getEndDate().toString());
            vdto.add(dt);
        }

        return new ResponseEntity<>(vdto,HttpStatus.OK);

    }

}
