package controller;

import dto.HallDTO;
import filters.FilterFactory;
import filters.HallFilter;
import helpers.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import model.Appointment;
import model.Center;
import model.Hall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.AppointmentService;
import service.CenterService;
import service.HallService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "api/hall")
@CrossOrigin
@Api
public class HallController {

    @Autowired
    private HallService hallService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private CenterService centerService;

    @GetMapping(value = "/getHallBusyDays/{centreName}/{hallNumber}")
    @ApiOperation("Получение максимально-загруженных дней в аптеках")
    public ResponseEntity<List<Date>> getHallBusyFromHallAndCentre(@PathVariable("centreName") String centreName, @PathVariable("hallNumber") int hallNumber) {
        log.info("Getting the maximum-loaded days in the pharmacy '{}' medical center'{}'.", hallNumber, centreName);
        Center c = centerService.findByName(centreName);
        List<Date> busyHall = new ArrayList<Date>();

        if (c == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Hall h = hallService.findByNumberAndCentre(hallNumber, c);
        if (h == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Appointment> app = appointmentService.findAllByHallAndCentre(h, c);
        if (app == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for (Appointment a : app) {
            busyHall.add(a.getDate());
        }

        return new ResponseEntity<>(busyHall, HttpStatus.OK);
    }

    @GetMapping(value = "/getAll")
    @ApiOperation("Получение всех аптек")
    public ResponseEntity<List<HallDTO>> getHalls() {
        log.info("Getting all pharmacies.");
        List<Hall> halls = hallService.findAll();
        List<HallDTO> ret = new ArrayList<HallDTO>();
        if (halls == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for (Hall hall : halls) {
            if (!hall.getDeleted()) {
                HallDTO dto = new HallDTO(hall);
                ret.add(dto);
            }
        }

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @PostMapping(value = "/getAllByFilter", consumes = "application/json")
    @ApiOperation("Поиск всех аптек при помощи фильтра")
    public ResponseEntity<HallDTO[]> getHallsFilter(@RequestBody HallDTO dto) {
        log.info("Search all pharmacies using the filter ('{}', '{}').", dto.getCentreName(), dto.getNumber());
        HttpHeaders header = new HttpHeaders();
        Center c = centerService.findByName(dto.getCentreName());

        if (c == null) {
            header.set("responseText", "center");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        List<HallDTO> ret = new ArrayList<HallDTO>();
        HallFilter filter = (HallFilter) FilterFactory.getInstance().get("hall");

        for (Hall hall : c.getHalls()) {
            if (dto.getDate() != null && dto.getDate() != "") {
                Date startDate = DateUtil.getInstance().getDate(dto.getDate(), "dd-MM-yyyy");
                long hours = 0;
                List<Appointment> appointments = appointmentService.findAllByHall(hall);

                for (Appointment app : appointments) {
                    if (DateUtil.getInstance().isSameDay(app.getDate(), startDate)) {
                        hours += DateUtil.getInstance().getTimeBetween(app.getDate(), app.getEndDate());
                    }
                }

                System.out.println(hall.getNumber() + " : " + hours);
                if (hours > 23 * DateUtil.HOUR_MILLIS) {
                    continue;
                }
            }

            if (!hall.getDeleted()) {
                if (filter.test(hall, dto)) {
                    ret.add(new HallDTO(hall));
                }
            }
        }

        HallDTO[] retArray = ret.toArray(new HallDTO[ret.size()]);

        return new ResponseEntity<>(retArray, HttpStatus.OK);
    }

    @GetMapping(value = "/getAllByCentre/{centreName}")
    @ApiOperation("Получение всех аптек для центров")
    public ResponseEntity<List<HallDTO>> getHalls(@PathVariable("centreName") String centreName) {
        log.info("Getting all pharmacies for the medical center '{}'.", centreName);
        HttpHeaders header = new HttpHeaders();
        Center c = centerService.findByName(centreName);

        if (c == null) {
            header.set("responseText", "center");
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        List<HallDTO> ret = new ArrayList<HallDTO>();
        for (Hall hall : c.getHalls()) {
            if (!hall.getDeleted()) {
                HallDTO dto = new HallDTO(hall);
                ret.add(dto);
            }
        }

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @DeleteMapping(value = "/deleteHall/{number}/{centreName}")
    @ApiOperation("Удаление аптек")
    public ResponseEntity<Void> deleteHall(@PathVariable("number") int number, @PathVariable("centreName") String centreName) {
        log.info("Removal of the pharmacy '{}' medical center '{}'.", number, centreName);
        Center center = centerService.findByName(centreName);

        if (center == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Hall hall = hallService.findByNumberAndCentre(number, center);

        if (hall == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Appointment> list = appointmentService.findAllByHall(hall);

        if (list != null) {
            if (list.size() > 0) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        hall.setDeleted(true);
        hall.setNumber(-1);
        hall.setName("");
        hallService.save(hall);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(value = "/changeHall/{oldNumber}/{newNumber}/{newName}/{centreName}")
    @ApiOperation("Обновление(изменение) данных аптек")
    public ResponseEntity<Void> changeHall(@PathVariable("oldNumber") int oldNumber, @PathVariable("newNumber") int newNumber, @PathVariable("newName") String newName, @PathVariable("centreName") String centreName) {
        log.info("Updating (changing) the data of the pharmacy '{}' medical center '{}'.", newNumber, centreName);
        Center center = centerService.findByName(centreName);

        if (center == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Hall hall = hallService.findByNumberAndCentre(oldNumber, center);

        if (hall == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Appointment> apps = appointmentService.findAllByHall(hall);

        if (apps != null) {
            if (apps.size() > 0) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        hall.setNumber(newNumber);
        hall.setName(newName);
        hallService.save(hall);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/get/{number}/{centreName}")
    @ApiOperation("Получение аптек по номерам")
    public ResponseEntity<HallDTO> getHallByNumber(@PathVariable("number") int number, @PathVariable("centreName") String centreName) {
        log.info("Getting pharmacies by numbers '{}' medical center '{}'.", number, centreName);
        Center center = centerService.findByName(centreName);

        if (center == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }

        Hall hall = hallService.findByNumberAndCentre(number, center);
        if (hall == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (hall.getDeleted()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new HallDTO(hall), HttpStatus.OK);
    }

    @PostMapping(value = "/addHall", consumes = "application/json")
    @ApiOperation("Добавления новых аптек")
    public ResponseEntity<Void> add(@RequestBody HallDTO hall) {
        log.info("Adding a new pharmacy '{}'.", hall.getName());
        Center center = centerService.findByName(hall.getCentreName());

        if (center == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Hall h = hallService.findByNumberAndCentre(hall.getNumber(), center);

        if (h == null) {
            Hall newHall = new Hall(center, hall.getNumber(), hall.getName());
            hallService.save(newHall);
            center.getHalls().add(newHall);
            centerService.save(center);

        } else {
            return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
