package dto;

import lombok.Data;
import model.User;
import model.User.UserRole;

@Data
public class SessionUserDTO {
    private Long id;
    private Boolean isFirstLog;
    private Boolean verifiedEmail;
    private Boolean verifiedPhone;
    private String username;
    private String email;
    private String firstname;
    private String lastname;
    private String city;
    private String state;
    private String date_of_birth;
    private String phone;
    private UserRole role;

    public SessionUserDTO() {
        super();
    }

    public SessionUserDTO(Long id, String username, String email, String firstname, String lastname, String city, String state, String date_of_birth, String phone, UserRole role) {
        super();
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.city = city;
        this.state = state;
        this.date_of_birth = date_of_birth;
        this.phone = phone;
        this.role = role;
    }

    public SessionUserDTO(User user) {
        super();
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.city = user.getCity();
        this.state = user.getState();
        this.date_of_birth = user.getDate_of_birth();
        this.phone = user.getPhone();
        this.role = user.getRole();
        this.isFirstLog = user.getIsFirstLog();
        this.verifiedEmail = user.getVerifiedEmail();
        this.verifiedEmail = user.getVerifiedPhone();
    }

}
