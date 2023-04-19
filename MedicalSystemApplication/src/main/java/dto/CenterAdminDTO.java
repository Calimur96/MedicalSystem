package dto;
import model.CenterAdmin;
import model.User;

public class CenterAdminDTO {

    private UserDTO user;
    private String centreName;


    public CenterAdminDTO()
    {
        super();
    }

    public CenterAdminDTO(UserDTO user, String centreName) {
        super();
        this.user = user;
        this.centreName = centreName;
    }

    public CenterAdminDTO(CenterAdmin ca) {
        UserDTO dto = new UserDTO();
        dto.setUsername(ca.getUsername());
        dto.setFirstname(ca.getFirstname());
        dto.setLastname(ca.getLastname());
        dto.setEmail(ca.getEmail());
        dto.setCity(ca.getCity());
        dto.setPhone(ca.getPhone());
        dto.setState(ca.getState());
        dto.setDate_of_birth(ca.getDate_of_birth());
        dto.setRole(User.UserRole.CentreAdmin);
        this.user = dto;

        if (ca.getCenter() != null) {
            this.centreName = ca.getCenter().getName();
        } else {
            this.centreName = "N/A";
        }
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }



    public String getCentreName() {
        return centreName;
    }

    public void setCentreName(String centreName) {
        this.centreName = centreName;
    }


}
