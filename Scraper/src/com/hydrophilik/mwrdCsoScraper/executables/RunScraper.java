package com.hydrophilik.mwrdCsoScraper.executables;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

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
			theDate = new BezDate("1/1/2007");
			endDate = new BezDate(); // today
		}
		catch (Exception e) {
			System.out.println("Unable to parse dates");
			return;
		}
		
		System.out.println("Scraper started with start date: " + theDate.convertToString() +
				" and end date: " + endDate.convertToString());
		
		scrapeWebsite(theDate, endDate, rawScrapingsDir);

	}
	
	private static void scrapeWebsite(BezDate theDate, BezDate endDate, String rawScrapingsDir) {	
		
		while (false == theDate.isSameDayAs(endDate)) {
			
			File file = new File(rawScrapingsDir + "/" + theDate.dateYearFirst() + ".txt");
			
			if (file.exists()) {
				// Date has already been scraped.  Skip it.
				return;
			}
			
			String date = theDate.convertToString();
			
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
		
			theDate.incrementDays(1);
		}
	}

}
