package model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
public class RegistrationRequest{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "password", nullable = false)
	private String password;
	
	@Column(name = "email", nullable = false)
	private String email;



    public RegistrationRequest() {
    	super();
    }

    public RegistrationRequest(String password, String email) {
		this.password = password;
		this.email = email;
    }

	public RegistrationRequest(RegistrationRequest req)
	{
		this.password = req.getPassword();
		this.email = req.getEmail();
	}

    


}
