package repository;

import model.AppointmentRequest;
import model.Center;
import model.Hall;
import model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface AppointmentRequestRepository extends JpaRepository<AppointmentRequest, Long> {

    AppointmentRequest findByDateAndHallAndCenter(Date date, Hall hall, Center center);

    AppointmentRequest findByDateAndPatientAndCenter(Date date, Patient patient, Center center);
    List<AppointmentRequest> findAllByPatient(Patient patient);
    List<AppointmentRequest> findAllByCenter(Center center);

    List<AppointmentRequest> findAll();
}
