package app.service;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import app.model.RateObject;

public interface RateService {

	void save(RateObject user);
	 
	List<RateObject> listRates();
	
	List<RateObject> listRates(String dateRange);
	
	List<RateObject> getXmlData(String dateRange);
	
	void save(Set<RateObject> rates);	
	
	Set<RateObject> getObjectsToSave(String dateRange);
	
	String saveExcel(HttpServletRequest request, String[] dates, String[] rates);
	
}
