package model;

import javax.persistence.*;
import dto.UserDTO;
import lombok.Data;

@Data
@Entity(name = "users")
@Table
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class User   {


	public enum UserRole {Patient, Doctor, Nurse, CentreAdmin}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name="deleted", nullable = true)
	private Boolean deleted;
	
	@Column(name = "isFirstLog")
	private Boolean isFirstLog;

	@Column(name = "username")
	private String username;
	
	@Column(name = "password", nullable = false)
	private String password;
	
	@Column(name = "email", nullable = false)
	private String email;
	
	@Column(name = "firstname")
	private String firstname;
	
	@Column(name = "lastname")
	private String lastname;
	
	@Column(name = "city")
	private String city;
	
	@Column(name = "state")
	private String state;

	@Column(name = "date_of_birth")
	private String date_of_birth;
	
	@Column(name = "phone")
	private String phone;

	@Column(name = "role")
	private UserRole role;

	@Column(name = "verified")
	private Boolean verified;


	public User()
	{
		super();
		this.deleted = false;
		this.verified = false;
	}

	public User(String username, String password, String email, String firstname, String lastname, String city, String state, String date_of_birth, String phone, UserRole role) {
		super();
		this.username = username;
		this.password = password;
		this.email = email;
		this.firstname = firstname;
		this.lastname = lastname;
		this.city = city;
		this.state = state;
		this.date_of_birth = date_of_birth;
		this.phone = phone;
		this.deleted = false;
		this.verified = false;
	}
	
	public User(RegistrationRequest request, UserRole role)
	{
		super();
		this.password = request.getPassword();
		this.email = request.getEmail();
		this.role = role;
		this.deleted = false;
		this.verified = false;
	}


	public User(RegistrationRequest request)
	{
		super();
		this.password = request.getPassword();
		this.email = request.getEmail();
		this.deleted = false;
		this.verified = false;
	}

	public User(User user) {
		this.username = user.getUsername();
		this.password = user.getPassword();
		this.email = user.getEmail();
		this.firstname = user.getFirstname();
		this.lastname = user.getLastname();
		this.city = user.getCity();
		this.state = user.getState();
		this.date_of_birth = user.getDate_of_birth();
		this.phone = user.getPhone();
		this.role = user.getRole();
		this.deleted = false;
		this.verified = false;
	}


	public User(UserDTO user) {
		// TODO Auto-generated constructor stub
		this.username = user.getUsername();
		this.password = user.getPassword();
		this.email = user.getEmail();
		this.firstname = user.getFirstname();
		this.lastname = user.getLastname();
		this.city = user.getCity();
		this.state = user.getState();
		this.date_of_birth = user.getDate_of_birth();
		this.phone = user.getPhone();
		this.role = user.getRole();
		this.deleted = false;
		this.verified = false;
	}



}
