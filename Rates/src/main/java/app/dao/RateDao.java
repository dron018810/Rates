package app.dao;

import java.time.LocalDate;
import java.util.List;

import app.model.RateObject;

public interface RateDao {
	
	void save(RateObject rate);
	
	List<RateObject> listRates();
	
	List<RateObject> listRates(LocalDate startDate, LocalDate endDate);

}
