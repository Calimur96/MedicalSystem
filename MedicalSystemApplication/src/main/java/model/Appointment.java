package model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

@Data
@Entity
public class Appointment {

	public enum AppointmentType {
        Examination
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "startingDateAndTime")
    private Date date;

    @Column(name = "endingDateAndTime")
    private Date endDate;

    @ManyToOne
    private Hall hall;

    @OneToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @OneToOne
    @JoinColumn(name = "centre_id")
    private Center center;

    @Column(name = "duration", nullable = true)
    private long duration;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Doctor> doctors;

    @OneToOne
    @JoinColumn(name = "priceslist_id")
    private Priceslist priceslist;

    @Column(name = "appointmentType")
    private AppointmentType appointmentType;

    @Column(name = "predefined")
    private Boolean predefined = false;

    @Column(name = "done")
    private Boolean done = false;

    @Column(name = "confirmed")
    private Boolean confirmed = false;

    private Date newDate;
    private Date newEndDate;

    @Version
    private Integer version;

    public Appointment() {
        super();
        this.doctors = new ArrayList<>();
        this.confirmed = true;
        // TODO Auto-generated constructor stub
    }

    public Appointment(Date date, Hall hall, Patient patient, Center center, long duration,
                       Priceslist priceslist, AppointmentType appointmentType) {
        super();
        this.date = date;
        this.hall = hall;
        this.patient = patient;
        this.center = center;
        this.duration = duration;
        this.doctors = new ArrayList<>();
        this.priceslist = priceslist;
        this.appointmentType = appointmentType;
        this.confirmed = true;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Date getNewEndDate() {
        return newEndDate;
    }

    public void setNewEndDate(Date newEndDate) {
        this.newEndDate = newEndDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Date getNewDate() {
        return newDate;
    }

    public void setNewDate(Date newDate) {
        this.newDate = newDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getPredefined() {
        return predefined;
    }

    public void setPredefined(Boolean predefined) {
        this.predefined = predefined;
    }

    public void setPriceslist(Priceslist priceslist) {
        this.priceslist = priceslist;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Center getCenter() {
        return center;
    }

    public void setCenter(Center center) {
        this.center = center;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<Doctor> getDoctors() {
        return doctors;
    }

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors;
    }

    public Priceslist getPriceslist() {
        return priceslist;
    }

    public void setPricelist(Priceslist priceslist) {
        this.priceslist = priceslist;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public static class Builder {
        private Date date;
        private Date endDate;
        private Hall hall;
        private Patient patient;
        private Center center;
        private Priceslist priceslist;
        private List<Doctor> doctors;
        private AppointmentType appointmentType;
        private long duration;

        public Builder(Date date) {
            this.date = date;
            doctors = new ArrayList<>();
        }

        public Builder withHall(Hall hall) {
            this.hall = hall;
            return this;
        }

        public Builder withPatient(Patient patient) {
            this.patient = patient;
            return this;
        }

        public Builder withCentre(Center center) {
            this.center = center;
            return this;
        }

        public Builder withEndingDate(Date date) {
            this.endDate = date;
            return this;
        }

        public Builder withDoctors(ArrayList<Doctor> doctors) {
            this.doctors = doctors;
            return this;
        }

        public Builder withPriceslist(Priceslist priceslist) {
            this.priceslist = priceslist;
            return this;
        }

        public Builder withType(AppointmentType appointmentType) {
            this.appointmentType = appointmentType;
            return this;
        }

        public Builder withDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public Appointment build() {
            Appointment app = new Appointment();
            app.setDate(this.date);
            app.setPatient(this.patient);
            app.setCenter(this.center);
            app.setHall(this.hall);
            app.setDoctors(this.doctors);
            app.setPricelist(priceslist);
            app.setAppointmentType(this.appointmentType);
            app.setDuration(this.duration);
            app.setEndDate(this.endDate);
            return app;
        }

    }

}
