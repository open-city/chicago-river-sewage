package com.hydrophilik.mwrdCsoScraper.executables;

import com.hydrophilik.mwrdCsoScraper.parsing.MwrdParser;
import com.hydrophilik.mwrdCsoScraper.scrapeSite.Scraper;

public class RunScraper {

	public static void main(String[] args) {

		String rawScrapingsDir;
		String csvDataDir;
		// arg0 => directory where the raw scraped html code is placed.
		try {
			rawScrapingsDir = args[0];
			csvDataDir = args[1];
		}
		catch (Exception e) {
			System.out.println("You must specify directories for input");
			return;
		}
		
		System.out.println("Scraping...");
		
		Scraper.scrapeWebsite(rawScrapingsDir);
		
		System.out.println("Parsing");
		
		MwrdParser parser = new MwrdParser(rawScrapingsDir);
		
		parser.parseToCSV(csvDataDir);

	}
}
