package model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import dto.PatientDTO;
import helpers.UserBuilder;
import lombok.Data;
import model.User.UserRole;

import javax.persistence.*;

@Data
@Entity
public class Patient extends User {

	@JsonIgnore
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "medicalRecord_id", referencedColumnName = "id")
	private MedicalRecord medicalRecord;
	

	public Patient()
	{
		super();
	}
	
	public Patient(String username, String password, String email, String firstname, String lastname, String city, String state, String date_of_birth, String phone) {
		super(username, password, email, firstname, lastname, city, state, date_of_birth, phone, UserRole.Patient);
		medicalRecord = new MedicalRecord();
		this.setIsFirstLog(false);
	}
	public Patient(PatientDTO dto){
		super(dto.getUser());
		medicalRecord = new MedicalRecord();
	}
	
	public Patient(RegistrationRequest request)
	{
		super(request,UserRole.Patient);
		medicalRecord = new MedicalRecord();
		this.setIsFirstLog(false);
	}
	
	public Patient(User user)
	{
		super(user);
		medicalRecord = new MedicalRecord();
		this.setIsFirstLog(false);
	}

	public MedicalRecord getMedicalRecord() {
		return medicalRecord;
	}

	public void setMedicalRecord(MedicalRecord medicalRecord) {
		this.medicalRecord = medicalRecord;
	}

	public static class Builder extends UserBuilder
	{

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

		
		
		public Patient build()
		{
			this.withRole(UserRole.Patient);
			User user = super.build();
			Patient p = new Patient(user);
			return p;
		}
	}

	
}
