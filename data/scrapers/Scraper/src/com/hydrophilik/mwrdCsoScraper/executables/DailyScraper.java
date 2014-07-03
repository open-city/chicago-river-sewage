package com.hydrophilik.mwrdCsoScraper.executables;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.LocalDate;

import com.hydrophilik.mwrdCsoScraper.parsing.Scrape;
import com.hydrophilik.mwrdCsoScraper.utils.DateTimeUtils;
import com.hydrophilik.mwrdCsoScraper.utils.LogLogger;

public class DailyScraper {

	public static void main(String[] args) {
		Scrape scrape = null;

		try {
			String configFile = args[0];
			scrape = new Scrape(configFile);
		}
		catch (Exception e) {
			LogLogger.logError(ExceptionUtils.getStackTrace(e));
			return;
		}
		
		LocalDate endDate = new LocalDate(DateTimeUtils.chiTimeZone);
		LocalDate startDate = endDate.minusDays(30);
		
		scrape.doScrape(startDate, endDate);

	}
}