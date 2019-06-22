package app.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import app.model.RateObject;
import app.service.RateService;

@Controller
@RequestMapping("/")
public class RateController {
 
	@Autowired
	private RateService service;
	
	@GetMapping
	public String index(Model model) {
		List<RateObject> rates = service.listRates();
		model.addAttribute("rates", rates);
		return "index";
	}
	
	@PostMapping("downloadExchangeRatesAction")
	@ResponseBody
	public List<RateObject> downloadExchangeRatesAction(HttpServletRequest request, Model model) {
		Set<RateObject> result = service.getObjectsToSave(request.getParameter("daterange"));
		service.save(result);
		return service.listRates(request.getParameter("daterange"));
	}
	
	@RequestMapping(value="applyDaterangepickerAction", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<RateObject> applyDaterangepickerAction(HttpServletRequest request, Model model) {		
		return service.listRates(request.getParameter("daterange"));
	}
	
	@PostMapping("downloadExcelRatesAction")
	@ResponseBody
	public String downloadExcelRatesAction(HttpServletRequest request) {
		String[] dates = request.getParameter("dates").replace("[", "").replace("]", "").replace("\"", "").split(",");
		String[] rates = request.getParameter("rates").replace("[", "").replace("]", "").replace("\"", "").split(",");
		request.getSession().getId();
		String fileName = service.saveExcel(request, dates, rates);		
		return fileName;
	}
	
	@RequestMapping(value = "downloadFile/{path:.+}", method = RequestMethod.GET)
	public void downloadFile(@PathVariable String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String realPath = request.getServletContext().getRealPath("/");
		File file = new File (realPath, path);
		InputStream is = new FileInputStream(file);

		response.setContentType("application/vnd.ms-excel");
	    response.setHeader("Content-Disposition", "attachment; filename=Rates.xls");
	    FileCopyUtils.copy(is, response.getOutputStream());

	    file.delete();
	    response.flushBuffer();
	}
}
