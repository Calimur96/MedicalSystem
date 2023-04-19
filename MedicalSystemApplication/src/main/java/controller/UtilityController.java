package controller;

import dto.DateIntervalDTO;
import dto.MonthDTO;
import dto.WeekDTO;
import helpers.DateInterval;
import helpers.DateUtil;
import helpers.Scheduler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import model.Appointment;
import model.Center;
import model.Doctor;
import model.Hall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "api/utility")
@CrossOrigin
@Api
public class UtilityController {

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @GetMapping(value = "/date/getWeekInfo")
    @ApiOperation("Получение информации о неделе")
    public ResponseEntity<WeekDTO> getWeekInfo() {
        log.info("Getting information about the current week.");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date weekStart = cal.getTime();
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        Date weekEnd = cal.getTime();

        return new ResponseEntity<WeekDTO>(new WeekDTO(weekStart, weekEnd), HttpStatus.OK);
    }

    @GetMapping(value = "/date/getMonthInfo")
    @ApiOperation("Получение информации о месяце")
    public ResponseEntity<MonthDTO> getMonthInfo() {
        log.info("Getting information about the current month.");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date monthStart = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.setTime(new Date());
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DATE, -1);

        Date monthEnd = cal.getTime();
        return new ResponseEntity<>(new MonthDTO(monthStart, monthEnd), HttpStatus.OK);
    }

    @GetMapping(value = "/testDate/{date1}/{date2}/{date3}/{date4}")
    @ApiOperation("Проверка дат")
    public ResponseEntity<Void> testDate(@PathVariable("date1") String d1, @PathVariable("date2") String d2, @PathVariable("date3") String d3, @PathVariable("date4") String d4) {
        log.info("Checking available dates ('{}', '{}', '{}', '{}').", d1, d2, d3, d4);
        DateUtil util = DateUtil.getInstance();
        Date date1 = util.getDate(d1, "dd-MM-yyyy HH:mm");
        Date date2 = util.getDate(d2, "dd-MM-yyyy HH:mm");
        Date date3 = util.getDate(d3, "dd-MM-yyyy HH:mm");
        Date date4 = util.getDate(d4, "dd-MM-yyyy HH:mm");

        DateInterval di1 = new DateInterval(date1, date2);
        DateInterval di2 = new DateInterval(date3, date4);

        if (util.overlappingInterval(di1, di2)) {
            return new ResponseEntity<>(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/getBusyTime/{doctor}")
    @ApiOperation("Получение времен занятости докторов")
    public ResponseEntity<List<DateIntervalDTO>> getBusy(@PathVariable("doctor") String doctorEmail) {
        log.info("Getting the doctor's busy time by '{}'.", doctorEmail);
        Doctor d = doctorService.findByEmail(doctorEmail);

        List<DateInterval> intervals = Scheduler.getFreeIntervals(d, DateUtil.getInstance().getDate("21-01-2020", "dd-mm-yyyy"));
        List<DateIntervalDTO> dtos = new ArrayList<DateIntervalDTO>();

        for (DateInterval di : intervals) {
            dtos.add(new DateIntervalDTO(di, "HH:mm"));
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @Autowired
    private HallService hallService;

    @Autowired
    private AppointmentService apService;

    @Autowired
    private CenterService centerService;

    @GetMapping(value = "/hall/getBusyTime/{hallNumber}/{date}/{centreName}")
    @ApiOperation("Получение времени загруженности аптек")
    public ResponseEntity<List<DateIntervalDTO>> getBusyHall(@PathVariable("hallNumber") int num, @PathVariable("date") String date, @PathVariable("centreName") String centreName) {
        log.info("Getting the workload of pharmacies '{}' in '{}'.", num, centreName);
        Center center = centerService.findByName(centreName);

        if (center == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Hall hall = hallService.findByNumberAndCentre(num, center);

        List<Appointment> apps = apService.findAllByHall(hall);

        List<DateInterval> intervals = Scheduler.getBusyIntervals(apps, DateUtil.getInstance().getDate(date, "dd-mm-yyyy"));
        List<DateIntervalDTO> dtos = new ArrayList<DateIntervalDTO>();

        for (DateInterval di : intervals) {
            dtos.add(new DateIntervalDTO(di, "HH:mm"));
        }

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }
}
