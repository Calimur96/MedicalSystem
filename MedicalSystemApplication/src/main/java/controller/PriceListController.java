package controller;

import dto.PriceListDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import model.Appointment;
import model.Centre;
import model.Priceslist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.AppointmentService;
import service.CentreService;
import service.PriceListService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "api/priceList")
@CrossOrigin
@Api
public class PriceListController {

    @Autowired
    private PriceListService priceListService;

    @Autowired
    private CentreService centreService;

    @Autowired
    private AppointmentService appointmentService;

    @DeleteMapping(value="/deletePriceList/{typeOfExamination}/{centreName}")
    @ApiOperation("Удаление pricelist")
    public ResponseEntity<Void> deletePriceList(@PathVariable("typeOfExamination") String typeOfExamination, @PathVariable("centreName") String centreName)
    {
        Priceslist priceList = priceListService.findByTypeOfExaminationAndCentre(typeOfExamination, centreName);

        if (priceList == null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Centre c = centreService.findByName(centreName);

        if (c == null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }

        List<Appointment> appointments = appointmentService.findAllByCentre(c);

        for (Appointment app : appointments)
        {
            if(app.getPriceslist().getTypeOfExamination().equals(priceList.getTypeOfExamination()))
            {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        priceList.setDeleted(true);
        priceListService.save(priceList);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @GetMapping(value="/getAllByCentre/{centreName}")
    @ApiOperation("Получение всех typeOfExamination по названиям центров")
    public ResponseEntity<List<PriceListDTO>> getAllTypeExaminationByCentre(@PathVariable("centreName") String centreName)
    {
        Centre c  = centreService.findByName(centreName);

        if(c == null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Priceslist> pricesList = priceListService.findAllByCentre(c);
        List<PriceListDTO> priceListDTO = new ArrayList<PriceListDTO>();

        if(pricesList == null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for(Priceslist pr : pricesList)
        {
            if(!pr.getDeleted())
            {
                PriceListDTO dto = new PriceListDTO(pr);
                priceListDTO.add(dto);
            }
        }

        return new ResponseEntity<>(priceListDTO,HttpStatus.OK);
    }

    @GetMapping(value="/getAll")
    @ApiOperation("Получение всех typeOfExamination")
    public ResponseEntity<List<PriceListDTO>> getAllTypeExamination()
    {
        List<Priceslist> pricesList = priceListService.findAll();
        List<PriceListDTO> priceListDTO = new ArrayList<PriceListDTO>();

        if(pricesList == null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for(Priceslist pr : pricesList)
        {
            if(!pr.getDeleted())
            {
                PriceListDTO dto = new PriceListDTO(pr);
                priceListDTO.add(dto);
            }
        }

        return new ResponseEntity<>(priceListDTO,HttpStatus.OK);
    }

    @GetMapping(value="/get/{typeOfExamination}/{centreName}")
    @ApiOperation("Получение конкретных typeOfExamination по названиям центров")
    public ResponseEntity<PriceListDTO> getTypeOfExamination(@PathVariable("typeOfExamination") String typeOfExamination, @PathVariable("centreName") String centreName)
    {
        Priceslist priceList= priceListService.findByTypeOfExaminationAndCentre(typeOfExamination, centreName);
        if (priceList == null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (priceList.getDeleted())
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        PriceListDTO pr = new PriceListDTO(priceList);
        return new ResponseEntity<>(pr,HttpStatus.OK);
    }

    @PutMapping(value="/update/{typeOfExamination}/{centreName}")
    @ApiOperation("Обновление(изменение) typeOfExamination по названиям центров")
    public ResponseEntity<Void> update(@RequestBody PriceListDTO pricesList, @PathVariable("typeOfExamination") String typeOfExamination, @PathVariable("centreName") String centreName)
    {
        Priceslist oldPricesList = priceListService.findByTypeOfExaminationAndCentre(typeOfExamination, centreName);

        if (oldPricesList == null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Appointment> apps = appointmentService.findAllByPricesList(oldPricesList);

        if (apps != null)
        {
            if (apps.size() > 0)
            {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        oldPricesList.setPrice(pricesList.getPrice());
        oldPricesList.setTypeOfExamination(pricesList.getTypeOfExamination());

        priceListService.save(oldPricesList);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value ="/add", consumes = "application/json")
    @ApiOperation("Добавление")
    public ResponseEntity<Void> add(@RequestBody PriceListDTO dto)
    {

        Centre c = centreService.findByName(dto.getCentreName());
        Priceslist pl = priceListService.findByTypeOfExaminationAndCentre(dto.getTypeOfExamination(), dto.getCentreName());

        if (c == null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (pl == null)
        {
            Priceslist newPl = new Priceslist(c,dto.getTypeOfExamination(),dto.getPrice());
            priceListService.save(newPl);
        } else {
            if (pl.getDeleted() == false)
            {
                return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
            } else {
                //pl.setDeleted(false)
                //priceListService.save(pl);
                Priceslist newPl = new Priceslist(c,dto.getTypeOfExamination(),dto.getPrice());
                priceListService.save(newPl);
            }

        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
