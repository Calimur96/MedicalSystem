package dto;

import model.Center;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	public float getRating() {
		return rating;
	}

	public void setRating(float rating) {
		this.rating = rating;
	}
    
    
}
