package app.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.dao.RateDao;
import app.model.RateObject;

@Service
public class RateServiceImpl implements RateService {
	
	private final static Logger logger = LogManager.getLogger(RateServiceImpl.class);
	
	@Autowired
	private RateDao rateDao;
	
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final int DATE_RANGE_LIMIT = 93;
	private static final LocalDate MIN_DATE = LocalDate.parse("2002-01-01");
	private static final LocalDate MAX_DATE = LocalDate.now();
	
	public List<RateObject> getXmlData(String dateRange) {
		long startTime = System.nanoTime();
		LocalDate realStartDate = LocalDate.parse(dateRange.substring(0, 10), FORMATTER);
		LocalDate endDate = LocalDate.parse(dateRange.substring(13, 23), FORMATTER);
		
		
		if(Duration.between(endDate.atStartOfDay(), MAX_DATE.atStartOfDay()).toDays() < 0) {
			endDate = MAX_DATE;
		}
		if(Duration.between(realStartDate.atStartOfDay(), MIN_DATE.atStartOfDay()).toDays() > 0) {
			realStartDate = MIN_DATE;
		}
		
		LocalDate startDate = realStartDate;
		int countFakeDays = 0;
		
		if(realStartDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
			startDate = startDate.minusDays(1);	
			countFakeDays++;
		} else if(realStartDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
			startDate = startDate.minusDays(2);
			countFakeDays += 2;
		}
		
		long days = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays();				
		long requests = days / DATE_RANGE_LIMIT;
		if(days % DATE_RANGE_LIMIT > 0) {
			requests++;
		}
		
		List<RateObject> result = new ArrayList<RateObject>();
		
		for(int i = 1; i <= requests; i++) {
			LocalDate sdate = i == 1 ? startDate : startDate.plusDays(DATE_RANGE_LIMIT * (i - 1) + i - 1);
			LocalDate edate;
			if(i == requests) {
				edate = endDate;
			} else {
				if(Duration.between(startDate.plusDays(DATE_RANGE_LIMIT * i + (i == 1 ? 0 : i - 1)).atStartOfDay(), endDate.atStartOfDay()).toDays() < 0) {
					edate = endDate;
					requests--;
				} else {
					edate = startDate.plusDays(DATE_RANGE_LIMIT * i + (i == 1 ? 0 : i - 1));
				}
			}
			result.addAll(getXmlData(sdate, edate));
		}
		
		for(int i = 0; i < countFakeDays;) {
			result.remove(i);
			countFakeDays--;
		}
		
		long estimatedTime  = System.nanoTime() - startTime;
		logger.info("TIME TO PROCESS THE API RESPONSE = " + TimeUnit.SECONDS.convert(estimatedTime, TimeUnit.NANOSECONDS) + " SECONDS");
		return result;
		
	}
	
	private List<RateObject> getXmlData(LocalDate startDate, LocalDate endDate) {
		String url = "http://api.nbp.pl/api/exchangerates/tables/A/" + startDate.toString() + "/" + endDate.toString() + "/?format=xml";
		HttpURLConnection connection = null;
		
		try {
			URL urlObj = new URL(url);
			connection = (HttpURLConnection)urlObj.openConnection();
			connection.setRequestMethod("GET");

			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(connection.getInputStream());
						

			logger.info("NBP API CALL WITH DATE RANGE startDate = " + startDate.toString() + " endDate = " + endDate.toString());

			List<RateObject> result = new ArrayList<RateObject>();
			String date = "";
			while(reader.hasNext()) {
	            reader.next(); 
	            if(reader.getEventType() == XMLStreamReader.START_ELEMENT) {
	            	String localName = reader.getLocalName();
	            	if("EffectiveDate".equals(localName)) {
	            		date = reader.getElementText();
	            	} else if("Code".equals(localName)) {
	            		String elementText = reader.getElementText();
	            		if("USD".equals(elementText)) {
	            			reader.next();
	            			elementText = reader.getElementText();
	            			long startDatelossDatesCount = 0;	            			
	            			LocalDate localDate = LocalDate.parse(date, FORMATTER);
	            			if(result.size() > 0) {
		            			RateObject prevResult = result.get(result.size() - 1);
		            			startDatelossDatesCount = Duration.between(prevResult.getDate().atStartOfDay(), localDate.atStartOfDay()).toDays() - 1;
		            			for(int i = 1; i <= startDatelossDatesCount; i++) {
			            			result.add(new RateObject(prevResult.getDate().plusDays(i), prevResult.getRate()));
			            		} 
	            			}	            			
	            			result.add(new RateObject(LocalDate.parse(date, FORMATTER), Double.parseDouble(elementText)));
	            		}
	            	}
	            }
	        }

			long endDatelossDatesCount = 0;
			RateObject lastResult = result.get(result.size() - 1);
			endDatelossDatesCount = Duration.between(lastResult.getDate().atStartOfDay(), endDate.atStartOfDay()).toDays();
			for(int i = 1; i <= endDatelossDatesCount; i++) {
    			result.add(new RateObject(lastResult.getDate().plusDays(i), lastResult.getRate()));
    		}
			
			return result;
			
		} catch (IOException e) {
			e.printStackTrace();			
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	@Override
	public void save(Set<RateObject> rates) {
		for (RateObject rate : rates) {
			save(rate);
		}
		logger.info(rates.size() + " VALUES WERE INSERTED TO THE DATABASE");
	}

	@Override
	public synchronized Set<RateObject> getObjectsToSave(String dateRange) {	
		Set<RateObject> result = new TreeSet<RateObject>(new Comparator<RateObject>() {
			@Override
	        public int compare(RateObject s1, RateObject s2) {
	            return s1.getDate().compareTo(s2.getDate());
	        }        
	    });
		
		List<RateObject> xmlData = getXmlData(dateRange);
		List<RateObject> rates = listRates(dateRange);	
		result.addAll(xmlData);
		result.addAll(rates);
		
		rates.forEach(r -> result.removeIf(e -> e.getDate().equals(r.getDate())));
		
		return result;
	}
	
	@Override
	public String saveExcel(String realPath, String[] dates, String[] rates) {
		Workbook book = new HSSFWorkbook();
		Sheet sheet = book.createSheet("Rates");
		Row row = sheet.createRow(0);
		
		Cell date = row.createCell(0);
		date.setCellValue("Date");
		
		DataFormat format = book.createDataFormat();
        CellStyle dateStyle = book.createCellStyle();
        dateStyle.setDataFormat(format.getFormat("yyyy-mm-dd"));
        
        date.setCellStyle(dateStyle);
		
		Cell rate = row.createCell(1);
		rate.setCellValue("Exchange rate");
		
		for(int i = 1; i <= dates.length; i++) {
			row = sheet.createRow(i);
			date = row.createCell(0);
			date = row.createCell(0);
			date.setCellStyle(dateStyle);
			date.setCellValue(dates[i-1]);
			rate = row.createCell(1);
			rate.setCellValue(rates[i-1]);
		}
		
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		
		String filename = "rates.xls";
        File file = new File(realPath, filename);
		
		try {			
			FileOutputStream fileOut = new FileOutputStream(file);
			book.write(fileOut);
			fileOut.close();
			fileOut.flush();
			book.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return filename;
	}

	@Transactional
	public void save(RateObject rate) {
		rateDao.save(rate);	
	}

	@Transactional(readOnly = true)
	public List<RateObject> listRates() {
		return rateDao.listRates();
	}

	@Transactional(readOnly = true)
	public List<RateObject> listRates(String dateRange) {
		LocalDate startDate = LocalDate.parse(dateRange.substring(0, 10), FORMATTER);
		LocalDate endDate = LocalDate.parse(dateRange.substring(13, 23), FORMATTER);
		return rateDao.listRates(startDate, endDate);
	}

}
