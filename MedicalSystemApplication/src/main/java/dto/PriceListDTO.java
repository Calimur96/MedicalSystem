package dto;

import model.Priceslist;

public class PriceListDTO {
	private String centreName;
	private String typeOfExamination;
	private float price;
	
	public  PriceListDTO(Priceslist pr)
	{
		this.centreName = pr.getCenter().getName();
		this.typeOfExamination = pr.getTypeOfExamination();
		this.price = pr.getPrice();
	}
	
	public PriceListDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PriceListDTO(String centreName, String typeOfExamination, float price) {
		super();
		this.centreName = centreName;
		this.typeOfExamination = typeOfExamination;
		this.price = price;
	}
	
	public String getCentreName() {
		return centreName;
	}
	public void setCentreName(String centreName) {
		this.centreName = centreName;
	}
	public String getTypeOfExamination() {
		return typeOfExamination;
	}
	public void setTypeOfExamination(String typeOfExamination) {
		this.typeOfExamination = typeOfExamination;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	
	
}
