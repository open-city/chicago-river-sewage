package com.hydrophilik.mwrdCsoScraper.executables;

import java.io.File;

import com.orangewall.bezutils.beztime.BezDate;

public class RunScraper {
	
	public static String mwrdWebPrefix = "http://apps.mwrd.org/CSO/CSOEventSynopsisReport.aspx?passdate=";

	public static void main(String[] args) {
		
		String rawScrapingsDir;
		// arg0 => directory where the raw scraped html code is placed.
		try {
			rawScrapingsDir = args[0];
		}
		catch (Exception e) {
			System.out.println("You must specify a directory to place scraped html");
			return;
		}
		
		BezDate theDate;
		BezDate endDate;
		
		try {
			theDate = new BezDate("11/1/2012");
			endDate = new BezDate(); // today
		}
		catch (Exception e) {
			System.out.println("Unable to parse dates");
			return;
		}
		
		System.out.println("Scraper started with start date: " + theDate.convertToString() +
				" and end date: " + endDate.convertToString());
		
		while (false == theDate.isSameDayAs(endDate)) {
			
			File file = new File(rawScrapingsDir + "/" + theDate.dateYearFirst() + ".txt");
			
			if (file.exists()) {
				// Date has already been scraped.  Skip it.
				return;
			}
			
			System.out.println("Processing: " + file.getAbsolutePath());			
			theDate.incrementDays(1);
		}
	}

}
