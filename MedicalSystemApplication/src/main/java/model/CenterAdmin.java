
package model;

import javax.persistence.*;

import helpers.UserBuilder;
import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class CenterAdmin extends User {

    @ManyToOne(fetch = FetchType.EAGER)
    private Center center;

	@OneToMany()
	@LazyCollection(LazyCollectionOption.FALSE)
    private List<AppointmentRequest> appointmentRequests;

	@OneToMany()
	@LazyCollection(LazyCollectionOption.FALSE)
    private  List<VacationRequest> vacationRequests;

    public CenterAdmin(){
        super();
        setRole(UserRole.CentreAdmin);
    	this.setIsFirstLog(true);
    }

    public CenterAdmin(String username, String password, String email, String firstname, String lastname, String city, String state, String date_of_birth, String phone, Center center) {
        super(username, password, email, firstname, lastname, city, state, date_of_birth, phone, UserRole.CentreAdmin);
        this.center = center;
        this.appointmentRequests = new ArrayList<>();
        this.vacationRequests = new ArrayList<>();
        this.setIsFirstLog(true);
    }

    public CenterAdmin(User user)
    {
    	super(user);
    	this.appointmentRequests = new ArrayList<>();
        this.vacationRequests = new ArrayList<>();
        this.setIsFirstLog(true);
    }

    public Center getCenter() {
        return center;
    }

    public void setCenter(Center center) {
        this.center = center;
    }

    public List<AppointmentRequest> getAppointmentRequests() {
        return appointmentRequests;
    }

    public void setAppointmentRequests(List<AppointmentRequest> appointmentRequests) {
        this.appointmentRequests = appointmentRequests;
    }

    public List<VacationRequest> getVacationRequests() {
        return vacationRequests;
    }

    public void setVacationRequests(List<VacationRequest> vacationRequests) {
        this.vacationRequests = vacationRequests;
    }
    
    public static class Builder extends UserBuilder
    {
    	public Center center;

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

		public Builder withDate_of_birth(String date_of_birth) {
			super.withDate_of_birth(date_of_birth);
			return this;
		}
		
		public Builder withPhone(String phone)
		{
			super.withPhone(phone);
			
			return this;
		}
		
		public Builder withCentre(Center center)
		{
			this.center = center;
			
			return this;
		}
		
		public CenterAdmin build()
		{
			super.withRole(UserRole.CentreAdmin);
			User user = super.build();
			CenterAdmin a = new CenterAdmin(user);
			a.setCenter(this.center);
			return a;
		}
    }
}

