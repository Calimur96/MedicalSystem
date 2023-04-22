package dto;

import lombok.Data;
import model.Center;

@Data
public class CenterDTO {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String description;
    private float rating;

    public CenterDTO() {
        super();
    }

    public CenterDTO(Center center) {
        this.id = center.getId();
        this.name = center.getName();
        this.address = center.getAddress();
        this.city = center.getCity();
        this.state = center.getState();
        this.description = center.getDescription();
        this.rating = center.calculateRating();
    }

}
