package app.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "rates")
public class RateObject {
	
	@Id
    @GeneratedValue
    @Column(name = "rate_id")
    private Long id;
	
	@Column(name = "date", unique = true)
	private LocalDate date;
	
	@Column(name = "rate")
	private double rate;
	
	public RateObject() {

	}
	
	public RateObject(LocalDate date, double rate) {
		this.date = date;
		this.rate = rate;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public LocalDate getDate() {
		return date;
	}
	
	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	public double getRate() {
		return rate;
	}
	
	public void setRate(double rate) {
		this.rate = rate;
	}

	@Override
	public String toString() {
		return "RateObject [id=" + id + ", date=" + date + ", rate=" + rate + "]";
	}
	
	public String getStringDate() {
		return date.toString();
	}

}
