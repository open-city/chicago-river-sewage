package com.hydrophilik.mwrdCsoScraper.scrapeSite;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.hydrophilik.mwrdCsoScraper.parsing.CsoEvent;
import com.hydrophilik.mwrdCsoScraper.parsing.MwrdCsoSynopsisParser;

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
	
	public static Map<String, List<CsoEvent>> grabEventFromSite(LocalDate date) throws Exception {
		
		String urlStr = mwrdWebPrefix + date.toString();
		Document document = Jsoup.connect(urlStr).get();
		
		MwrdCsoSynopsisParser mwrdParser = new MwrdCsoSynopsisParser(document);
		List<CsoEvent> thisDaysEvents = mwrdParser.parseEvents();
		
		Map<String, List<CsoEvent>> retVal = new HashMap<String, List<CsoEvent>>();
		
		if (null == thisDaysEvents)
			return retVal;
		
		for (CsoEvent event : thisDaysEvents) {
			List<CsoEvent> csoList = retVal.get(event.getKey());
			if (null == csoList) {
				csoList = new ArrayList<CsoEvent>();
			}
			csoList.add(event);
			retVal.put(event.getKey(), csoList);
		}
		
		return retVal;
	}

}
