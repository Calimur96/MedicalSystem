package model;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;

@Data
@Entity
public class Priceslist {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne()
	@LazyCollection(LazyCollectionOption.FALSE)
	private Center center;

	@Column(name = "typeOfExamination", nullable = false)
	private String typeOfExamination;

	@Column(name = "price", nullable = false)
	private float price;
	
	@Column(name = "deleted", nullable = false)
	private Boolean deleted;
	
	
	public Priceslist() {
		super();
		this.deleted = false;
		// TODO Auto-generated constructor stub
	}
	public Priceslist(Center center, String typeOfExamination, float price) {
		super();
		this.center = center;
		this.typeOfExamination = typeOfExamination;
		this.price = price;
		this.deleted = false;
	}
	
	
	public Boolean getDeleted() {
		return deleted;
	}
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Center getCenter() {
		return center;
	}
	public void setCenter(Center center) {
		this.center = center;
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
