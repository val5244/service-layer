package com.morbank.services.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.morbank.services.model.smev.FNSPriostanSch;
import com.morbank.services.model.spark.Alive;
import com.morbank.services.model.spark.CompanyShortReport;

@RestController
public class ServiceController {
	private static final Logger log = LoggerFactory.getLogger(ServiceController.class);
	  
    // ФНС  FNS001  ИС "Автоматизированная информационная система "ФЦОД" ФНС"
	// Предоставление сведений о наличии действующих решений о приостановлении операций по счетам налогоплательщика
    // VS00626v002-FNS001
    @RequestMapping("/smev/fns-bankostsch")
    public FNSPriostanSch getBankOstschResult(
    		@RequestParam(value="smevid", required=true, defaultValue="") String smevid,
			@RequestParam(value="bik", required=true, defaultValue="") String bik,
			@RequestParam(value="innfl", defaultValue="") String innfl,
			@RequestParam(value="innul", defaultValue="") String innul,
			@RequestParam(value="kio", defaultValue="") String kio
   		) {
    	log.info("SMEV. VS00626v002-FNS001 service entered");			
        return new FNSPriostanSch(smevid, bik, innfl, innul, kio);
    }

    @RequestMapping("/spark/Alive")
    public Alive getSparkAlive() {
    	log.info("SPARK.Time service entered");			
        return new Alive();
    }
    
    @RequestMapping("/spark/GetCompanyShortReport")
    public CompanyShortReport getSparkCompanyShortReport(
	    		@RequestParam(value="sparkid", defaultValue="") String sparkid,
				@RequestParam(value="inn", defaultValue="") String inn,
				@RequestParam(value="ogrn", defaultValue="") String ogrn
    	) {
    	log.info("SPARK.GetCompanyShortReport service entered");			
        return new CompanyShortReport(sparkid,inn,ogrn);
    }
}
