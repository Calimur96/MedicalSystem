package dto;

import model.Hall;

public class HallDTO {

	private String centreName;
	private int number;
	private String name;
	private String date;
		
	public HallDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public HallDTO(String centreName, int number,String name) {
		super();
		this.centreName = centreName;
		this.number = number;
		this.name = name;
		this.date = "";
	} 	
	
	public HallDTO(Hall hall)
	{
		this.centreName = hall.getCenter().getName();
		this.number = hall.getNumber();
		this.name = hall.getName();
		this.date = "";
	}

	
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCentreName() {
		return centreName;
	}

	public void setCentreName(String centreName) {
		this.centreName = centreName;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	
	
}
