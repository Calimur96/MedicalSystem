package dto;

import helpers.DateUtil;
import model.Nurse;
import model.User;

public class NurseDTO {

	private UserDTO user;
	private String type;
	private String shiftStart;
	private String shiftEnd;
	private String centreName;

	
	public NurseDTO()
	{
		super();
	}

	public NurseDTO(UserDTO user, String type, String shiftStart, String shiftEnd, String centreName) {
		this.user = user;
		this.type = type;
		this.shiftStart = shiftStart;
		this.shiftEnd = shiftEnd;
		this.centreName = centreName;
	}

	public NurseDTO(Nurse n)
	{
		UserDTO dto = new UserDTO();
		dto.setUsername(n.getUsername());
		dto.setFirstname(n.getFirstname());
		dto.setLastname(n.getLastname());
		dto.setEmail(n.getEmail());
		dto.setCity(n.getCity());
		dto.setPhone(n.getPhone());
		dto.setState(n.getState());
		dto.setDate_of_birth(n.getDate_of_birth());
		dto.setRole(User.UserRole.Nurse);
		this.user = dto;

		if(n.getCenter() != null)
		{
			this.centreName = n.getCenter().getName();
		}
		else
		{
			this.centreName = "N/A";
		}
		this.type = n.getType();
		this.shiftStart = DateUtil.getInstance().getString(n.getShiftStart(),"HH:mm");
		this.shiftEnd = DateUtil.getInstance().getString(n.getShiftEnd(),"HH:mm");
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
}
