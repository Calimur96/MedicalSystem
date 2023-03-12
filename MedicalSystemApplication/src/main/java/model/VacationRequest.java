package model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import lombok.Data;
import org.springframework.data.annotation.Version;

@Data
@Entity
public class VacationRequest
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    @Column(name = "startDate", nullable = false)
	private Date startDate;
    
    @Column(name = "endDate", nullable = false)
	private Date endDate;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private Centre centre;
    
    @Version
    private int version;

	public VacationRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	public VacationRequest(Date startDate, Date endDate, Centre centre, User vacationUser) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.user = vacationUser;
		this.centre = centre;
	}

	
	public int getVersion() {
		return version;
	}


	public void setVersion(int version) {
		this.version = version;
	}


	public Centre getCentre() {
		return centre;
	}


	public void setCentre(Centre centre) {
		this.centre = centre;
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date date) {
		this.startDate = date;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date end) {
		this.endDate = end;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User vacationUser) {
		this.user = vacationUser;
	}

}
