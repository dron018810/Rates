package app.service;

import java.util.List;
import java.util.Set;

import app.model.RateObject;

public interface RateService {

	void save(RateObject user);
	 
	List<RateObject> listRates();
	
	List<RateObject> listRates(String dateRange);
	
	List<RateObject> getXmlData(String dateRange);
	
	void save(Set<RateObject> rates);	
	
	Set<RateObject> getObjectsToSave(String dateRange);
	
	String saveExcel(String realPath, String[] dates, String[] rates);
	
}
