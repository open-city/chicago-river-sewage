package com.hydrophilik.mwrdCsoScraper.executables;

import org.joda.time.LocalDate;

import org.opengovhacknight.parsing.Scrape;
import com.hydrophilik.mwrdCsoScraper.utils.DateTimeUtils;

public class SpecificScrape {

	public static void main(String[] args) {
		String startDateStr = null;
		String endDateStr = null;
		Scrape scrape = null;

		try {
			startDateStr = args[0];
			endDateStr = args[1];
			String databasePath = args[2];
			scrape = new Scrape(databasePath);
			
		}
		catch (Exception e) {
			System.out.println("Start date and/or end date not passed in");
			return;
		}
		
		LocalDate startDate = DateTimeUtils.translateDate(startDateStr);
		LocalDate endDate = DateTimeUtils.translateDate(endDateStr);
		if (null == startDate) {
			System.out.println("Unable to translate end date");
		}

		if (null == endDate)
			endDate = new LocalDate(DateTimeUtils.chiTimeZone);
		
		scrape.doScrape(startDate, endDate);
	}

}
