package com.hydrophilik.mwrdCsoScraper.scrapeSite;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.joda.time.LocalDate;

public abstract class Scraper {

	public static String mwrdWebPrefix = "http://apps.mwrd.org/CSO/CSOEventSynopsisReport.aspx?passdate=";
	
	public static void scrapeWebsite(String rawScrapingsDir) {
		
		LocalDate theDate;
		LocalDate endDate;
		
		try {
			theDate = new LocalDate(2007, 1, 1);
			endDate = new LocalDate(); //today

		}
		catch (Exception e) {
			System.out.println("Unable to parse dates");
			return;
		}
		
		System.out.println("Scraper started with start date: " + theDate.toString() +
				" and end date: " + endDate.toString());

		while (false == theDate.toString().equals(endDate.toString())) {
			
			File file = new File(rawScrapingsDir + "/" + theDate.toString() + ".txt");
			
			if (file.exists()) {
				// Date has already been scraped.  Skip it.
				theDate = theDate.plusDays(1);
				continue;
			}

			String date = theDate.toString();
			
			BufferedWriter bw = null;
			FileWriter fw = null;
			
			try {
			
				file.createNewFile();
			
				String inputLine;
				String urlStr = mwrdWebPrefix + date;
				URL url = new URL(urlStr);
				URLConnection yc = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(
	                                    yc.getInputStream()));
	
				fw = new FileWriter(file.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				while ((inputLine = in.readLine()) != null)
					bw.write(inputLine + "\n");
			}
			catch (Exception e) {
				e.printStackTrace();
				return;
			}
			finally {
				try {
					bw.close();
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		
			theDate = theDate.plusDays(1);
		}
	}

}
