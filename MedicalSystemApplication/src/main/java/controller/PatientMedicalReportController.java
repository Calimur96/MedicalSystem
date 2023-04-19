package controller;

import dto.PatientMedicalReportDTO;
import dto.PrescriptionDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "api/reports")
@CrossOrigin
@Api
public class PatientMedicalReportController {

    @Autowired
    private UserService userService;

    @Autowired
    private CenterService centerService;

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private PatientMedicalReportService patientMedicalReportService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @GetMapping(value = "/getAllReports/{email}")
    @ApiOperation("Получение всевозможных отчетов согласно email")
    public ResponseEntity<List<PatientMedicalReportDTO>> getAllReports(@PathVariable("email") String email) {
        log.info("Getting all kinds of reports according to email '{}'.", email);
        HttpHeaders header = new HttpHeaders();

        Patient patient = patientService.findByEmail(email);
        if (patient == null) {
            header.set("responseText", "patient not found: " + email);
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        List<PatientMedicalReport> ret = patient.getMedicalRecord().getReports();

        List<PatientMedicalReportDTO> dtos = new ArrayList<>();
        for (PatientMedicalReport p : ret) {
            dtos.add(new PatientMedicalReportDTO(p));
        }

        System.out.println(dtos);

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @PutMapping(value = "/updateReport/{id}")
    @ApiOperation("Обновление(изменение) отчета по id")
    public ResponseEntity<Void> updateReport(@PathVariable("id") long id, @RequestBody PatientMedicalReportDTO dto) {
        log.info("Update (change) report by id '{}'.", id);
        HttpHeaders header = new HttpHeaders();

        PatientMedicalReport report = patientMedicalReportService.findById(id);
        if (report == null) {
            header.set("responseText", "report not found: " + id);
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        report.getDiagnosis().clear();
        for (String d : dto.getDiagnosis()) {
            Diagnosis diagnosis = diagnosisService.findByName(d);
            report.getDiagnosis().add(diagnosis);
        }
        report.setDescription(dto.getDescription());

        long idPres = report.getPrescription().getId();
        Prescription prescription = prescriptionService.findById(idPres);

        prescription.setDescription(dto.getPrescription().getDescription());
        report.getPrescription().getDrugs().clear();
        for (String d : dto.getPrescription().getDrugs()) {
            Drug drug = drugService.findByName(d);
            report.getPrescription().getDrugs().add(drug);
        }
        prescription.setValid(false);
        prescription.setNurse(null);
        prescription.setValidationDate(null);

        prescriptionService.save(prescription);

        report.setPrescription(prescription);
        patientMedicalReportService.save(report);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/getReportPrescription/{reportId}")
    @ApiOperation("Получение отчета по рецепту согласно его id")
    public ResponseEntity<PrescriptionDTO> getReport(@PathVariable("reportId") long reportId) {
        log.info("Getting a recipe report according to its id '{}'.", reportId);
        HttpHeaders header = new HttpHeaders();

        PatientMedicalReport report = patientMedicalReportService.findById(reportId);
        if (report == null) {
            header.set("responseText", "report not found: " + reportId);
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        Prescription ret = report.getPrescription();
        PrescriptionDTO dto = new PrescriptionDTO(ret);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping(value = "/addPatientMedicalReport/{email}")
    @ApiOperation("Cоздание нового медицинского отчета пациента")
    public ResponseEntity<Void> addPatientMedicalReport(@PathVariable("email") String email, @RequestBody PatientMedicalReportDTO dto) {
        log.info("Create a new patient medical report with '{}'.", email);
        HttpHeaders header = new HttpHeaders();

        Patient patient = patientService.findByEmail(email);
        if (patient == null) {
            header.set("responseText", "patient not found: " + email);
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        Doctor doctor = doctorService.findByEmail(dto.getDoctorEmail());
        if (doctor == null) {
            header.set("responseText", "doctor not found: " + dto.getDoctorEmail());
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        Center center = centerService.findByName(dto.getCentreName());
        if (center == null) {
            header.set("responseText", "center not found: " + dto.getCentreName());
            return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
        }

        Prescription pr = new Prescription();
        pr.setDescription(dto.getPrescription().getDescription());
        for (String name : dto.getPrescription().getDrugs()) {
            Drug drug = drugService.findByName(name);

            if (drug == null) {
                header.set("responseText", "drug not found: " + name);
                return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
            }

            pr.getDrugs().add(drug);

        }
        pr.setValid(false);
        prescriptionService.save(pr);

        PatientMedicalReport report = new PatientMedicalReport();
        report.setCenter(center);
        report.setDescription(dto.getDescription());
        report.setDoctor(doctor);
        report.setPrescription(pr);
        report.setDateAndTime(dto.getDateAndTime());
        report.setPatient(patient);

        for (String name : dto.getDiagnosis()) {
            Diagnosis diagnosis = diagnosisService.findByName(name);

            if (diagnosis == null) {
                header.set("responseText", "diagnosis not found: " + name);
                return new ResponseEntity<>(header, HttpStatus.NOT_FOUND);
            }

            report.getDiagnosis().add(diagnosis);
        }

        patientMedicalReportService.save(report);

        MedicalRecord mr = medicalRecordService.findByPatient(patient);
        mr.getReports().add(report);
        medicalRecordService.save(mr);

        userService.save(patient);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}


