package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dto.DoctorDTO;
import helpers.DateUtil;
import helpers.UserBuilder;
import lombok.Data;

@Data
@Entity
public class Doctor extends User
{
	@Column(name = "type", nullable = true)
	private String type;
		
	@Column(name = "shiftStart", nullable = true)
    private Date shiftStart;
	
    @Column(name = "shiftEnd", nullable = true)
    private Date shiftEnd;
    
    @Column(name = "averageRating", nullable = true)
    private float averageRating;

	@JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    private Centre centre;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ReviewDoctor> reviews;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY)
	private List<Appointment> appointments;
	
	@OneToMany(fetch = FetchType.LAZY)
	private List<Vacation> vacations;



	public Doctor() {

		super();
		vacations = new ArrayList<Vacation>();
		// TODO Auto-generated constructor stub
	}

	public Doctor(Long id, String username, String password, String email, String firstname, String lastname, String city,
			 String state, String date_of_birth, String phone) {
		super(id, username, password, email, firstname, lastname, city, state, date_of_birth, phone, UserRole.Doctor);
		this.setIsFirstLog(true);
		this.appointments = new ArrayList<>();
		this.vacations = new ArrayList<Vacation>();
		this.reviews = new ArrayList<ReviewDoctor>();
	}

	public Doctor(User user) {
		super(user);
		this.setRole(UserRole.Doctor);
		this.setIsFirstLog(true);
		this.appointments = new ArrayList<>();
		this.vacations = new ArrayList<Vacation>();
		this.reviews = new ArrayList<ReviewDoctor>();
	}
	
	public Doctor(DoctorDTO dto)
	{
		super(dto.getUser());
		this.setRole(UserRole.Doctor);
		this.setIsFirstLog(true);
		this.shiftStart = DateUtil.getInstance().getDate(dto.getShiftStart(), "HH:mm");
		this.shiftEnd = DateUtil.getInstance().getDate(dto.getShiftEnd(), "HH:mm");
		this.appointments = new ArrayList<Appointment>();
		this.vacations = new ArrayList<Vacation>();
		this.reviews = new ArrayList<ReviewDoctor>();
	}

	public Boolean IsFreeOn(Date date)
	{
		for(Vacation v: vacations)
		{
			Date start = v.getStartDate();
			Date end = v.getEndDate();
			
			if(date.after(start) && date.before(end))
			{
				return false;
			}
		}
		
		return true;
	}
		
	public List<Vacation> getVacations() {
		return vacations;
	}

	public void setVacations(List<Vacation> vacations) {
		this.vacations = vacations;
	}

	public Date getShiftStart() {
		return shiftStart;
	}

	public void setShiftStart(Date shiftStart) {
		this.shiftStart = shiftStart;
	}

	public Date getShiftEnd() {
		return shiftEnd;
	}

	public void setShiftEnd(Date shiftEnd) {
		this.shiftEnd = shiftEnd;
	}

	public Centre getCentre() {
		return centre;
	}

	public void setCentre(Centre centre) {
		this.centre = centre;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
			
	public float getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(float averageRating) {
		this.averageRating = averageRating;
	}
	
	public float calculateRating()
	{
		List<ReviewDoctor> reviews = getReviews();
		List<Integer> ratings = new ArrayList<Integer>();
		float sum = 0;
		
		if(reviews.isEmpty())
		{
			return -1;
		}
		
		for(ReviewDoctor cr : reviews)
		{
			if(cr.getRating() >= 0)
			{
				ratings.add(cr.getRating());
			}
		}
	
		for(Integer r : ratings)
		{
			sum = sum + r;
		}
		
		 return sum = (Float) sum / ratings.size();
	}

	public List<ReviewDoctor> getReviews() {
		return reviews;
	}

	public void setReviews(List<ReviewDoctor> reviews) {
		this.reviews = reviews;
	}

	public List<Appointment> getAppointments() {
		return appointments;
	}

	public void setAppointments(List<Appointment> appointments) {
		this.appointments = appointments;
	}
	
	public static class Builder extends UserBuilder
	{
		private String type;
		private String insuranceId;
	    private Date shiftStart;
	    private Date shiftEnd;
	    public Centre centre;

		protected Builder(Long id)
		{
			super(id);
		}

		protected Builder withEmail(String email) {
			super.withEmail(email);
			return this;
		}


		public Builder withUsername(String username)
		{
			super.withUsername(username);

			return this;
		}

		public Builder withPassword(String password)
		{
			super.withPassword(password);
			
			return this;
		}

		public Builder withFirstname(String firstname)
		{
			super.withFirstname(firstname);
			
			return this;
		}
	
		public Builder withLastname(String lastname)
		{
			super.withLastname(lastname);
			
			return this;
		}
		
		public Builder withCity(String city)
		{
			super.withCity(city);
			
			return this;
		}

		
		public Builder withState(String state)
		{
			super.withState(state);
			
			return this;
		}

		public Builder withDate_of_birth(String date_of_birth)
		{
			super.withDate_of_birth(date_of_birth);

			return this;
		}
		
		public Builder withPhone(String phone)
		{
			super.withPhone(phone);
			
			return this;
		}

		
		public Builder withType(String type)
		{
			this.type = type;
			
			return this;
		}
		
		public Builder withShiftStart(Date shiftStart)
		{
			this.shiftStart = shiftStart;
			
			return this;
		}
		
		public Builder withShiftEnd(Date shiftEnd)
		{
			this.shiftEnd = shiftEnd;
			
			return this;
		}
		
		public Builder withCentre(Centre centre)
		{
			this.centre = centre;
			
			return this;
		}

		
		public Doctor build()
		{
			super.withRole(UserRole.Doctor);
			User user = super.build();
			Doctor d = new Doctor(user);
			d.setType(this.type);
			d.setShiftStart(this.shiftStart);
			d.setShiftEnd(this.shiftEnd);
			d.setCentre(this.centre);
			return d;
		}
	}
}
