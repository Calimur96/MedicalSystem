package dto;

import lombok.Data;
import model.Center;
@Data
public class CenterDTO {

    private String name;
    private String address;
    private String city;
    private String state;
    private String description;
    private float rating;

    public CenterDTO()
    {
    	super();
    }
    
    public CenterDTO(String name, String address, String city, String state, String description, float rating) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.description = description;
        this.rating = rating;
    }
    
    public CenterDTO(Center center)
    {
    	this.name = center.getName();
        this.address = center.getAddress();
        this.city = center.getCity();
        this.state = center.getState();
        this.description = center.getDescription();
        
        this.rating = center.calculateRating();
    }


    
    
}
