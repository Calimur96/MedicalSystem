package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import lombok.Data;
import model.Appointment.AppointmentType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Data
@Entity
public class AppointmentRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "startingDateAndTime", nullable = false)
	private Date date;

	@OneToOne
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@ManyToOne
	private Hall hall;

	@OneToOne
	@JoinColumn(name = "centre_id")
	private Center center;

	@ManyToMany(fetch = FetchType.EAGER)
	private List<Doctor> doctors;

	@OneToOne
	@JoinColumn(name = "priceslist_id")
	private Priceslist priceslist;

	@Column(name = "appointmentType", nullable = true)
	private AppointmentType appointmentType;

	@Column(name = "timestamp")
	private Date timestamp;

	public AppointmentRequest() {
		super();
		this.doctors = new ArrayList<>();
	}
}
