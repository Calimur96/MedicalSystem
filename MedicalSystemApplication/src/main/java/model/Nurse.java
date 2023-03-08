package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import dto.NurseDTO;
import helpers.DateUtil;
import helpers.UserBuilder;
import lombok.Data;

@Data
@Entity
public class Nurse extends User {


	@Column(name = "type", nullable = true)
	private String type;

	@Column(name = "shiftStart")
    private Date shiftStart;
	
    @Column(name = "shiftEnd")
    private Date shiftEnd;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private Centre centre;
    
    @OneToMany(fetch = FetchType.LAZY)
    private List<Prescription> prescriptions;

	@OneToMany(fetch = FetchType.LAZY)
	private List<Vacation> vacations;


    public Nurse() {
        super();
    }


    public Nurse(String username, String password, String email, String firstname, String lastname, String city, String state, String date_of_birth, String phone) {
        super(username, password, email, firstname, lastname, city, state, date_of_birth, phone, UserRole.Nurse);
        this.prescriptions = new ArrayList<>();
		this.vacations = new ArrayList<>();
        this.setIsFirstLog(true);
    }
    
    public Nurse(User user)
    {
    	super(user);   
    	this.prescriptions = new ArrayList<>();
    	this.vacations = new ArrayList<>();
        this.setIsFirstLog(true);
    }

	public Nurse(NurseDTO dto)
	{
		super(dto.getUser());
		this.setRole(UserRole.Doctor);
		this.setIsFirstLog(true);
		this.shiftStart = DateUtil.getInstance().getDate(dto.getShiftStart(), "HH:mm");
		this.shiftEnd = DateUtil.getInstance().getDate(dto.getShiftEnd(), "HH:mm");
		this.vacations = new ArrayList<>();
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Prescription> getPrescriptions() {
		return prescriptions;
	}

	public void setPrescriptions(List<Prescription> prescriptions) {
		this.prescriptions = prescriptions;
	}

	public Centre getCentre() {
		return centre;
	}

	public void setCentre(Centre centre) {
		this.centre = centre;
	}


	public static class Builder extends UserBuilder
	{
	    private Date shiftStart;
	    private Date shiftEnd;
	    public Centre centre;
	    public String type;


		protected Builder(String email) {
			super(email);

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

		public Builder withType(String type)
		{
			this.type = type;

			return this;
		}
		
		public Nurse build()
		{
			super.withRole(UserRole.Nurse);
			User user = super.build();
			Nurse n = new Nurse(user);
			n.setCentre(this.centre);
			n.setShiftStart(this.shiftStart);
			n.setShiftEnd(this.shiftEnd);
			n.setType(this.type);
			return n;
		}
	}
   
}
