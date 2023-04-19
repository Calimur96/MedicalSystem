package dto;

import helpers.DateUtil;
import model.Doctor;
import model.User.UserRole;

public class DoctorDTO {

	private UserDTO user;
	private String type;
    private String shiftStart;
    private String shiftEnd;
    private String centreName;
    private float averageRating;
    
    public DoctorDTO()
    {
    	super();
    }
    
	public DoctorDTO(UserDTO user, String type, String shiftStart, String shiftEnd, String centreName, float averageRating) {
		super();
		this.user = user;
		this.type = type;
		this.shiftStart = shiftStart;
		this.shiftEnd = shiftEnd;
		this.centreName = centreName;
		this.averageRating = averageRating;
	}
	
	public DoctorDTO(Doctor d)
	{
		UserDTO dto = new UserDTO();
		dto.setUsername(d.getUsername());
		dto.setFirstname(d.getFirstname());
		dto.setLastname(d.getLastname());
		dto.setEmail(d.getEmail());
		dto.setCity(d.getCity());
		dto.setPhone(d.getPhone());
		dto.setState(d.getState());
		dto.setDate_of_birth(d.getDate_of_birth());
		dto.setRole(UserRole.Doctor);
		this.user = dto;
		
		if(d.getCenter() != null)
		{
			this.centreName = d.getCenter().getName();
		}
		else
		{
			this.centreName = "N/A";
		}
		this.averageRating = d.getAverageRating();
		this.type = d.getType();
		this.shiftStart = DateUtil.getInstance().getString(d.getShiftStart(),"HH:mm");
		this.shiftEnd = DateUtil.getInstance().getString(d.getShiftEnd(),"HH:mm");
	}

	public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.user = user;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


	public String getShiftStart() {
		return shiftStart;
	}

	public void setShiftStart(String shiftStart) {
		this.shiftStart = shiftStart;
	}

	public String getShiftEnd() {
		return shiftEnd;
	}

	public void setShiftEnd(String shiftEnd) {
		this.shiftEnd = shiftEnd;
	}

	public String getCentreName() {
		return centreName;
	}

	public void setCentreName(String centreName) {
		this.centreName = centreName;
	}

	public float getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(float averageRating) {
		this.averageRating = averageRating;
	}
    
    
	
}
