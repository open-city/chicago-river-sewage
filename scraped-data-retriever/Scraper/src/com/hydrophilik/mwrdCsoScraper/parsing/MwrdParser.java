package com.hydrophilik.mwrdCsoScraper.parsing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.joda.time.LocalDate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MwrdParser {
	
	private String rawScrapingsDir = null;

	public MwrdParser(String rawScrapingsDir) {
		this.rawScrapingsDir = rawScrapingsDir;
	}
	
	public void parseToCSV(String csvDataDir) {

		File csvDir = new File(csvDataDir);
		
		if (false == csvDir.exists())
			csvDir.mkdirs();
		
		LocalDate today = new LocalDate();
		
		String csvFileName = "csoEvents-" + today.toString() + ".csv";
		
		File csvFile = new File(csvDataDir + "/" + csvFileName);
		
		if (csvFile.exists())
			csvFile.delete();
		
		FileWriter fileWriter = null;

		try {
			csvFile.createNewFile();
			fileWriter = new FileWriter(csvFile.getAbsoluteFile());
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}

		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		// Loop through all of the files, and parse the data into structures.
		File scrapedSiteDir = new File(rawScrapingsDir);

		for (File input : scrapedSiteDir.listFiles()) {
			
			Document doc;

			try {
				doc = Jsoup.parse(input, "UTF-8", "");
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}

			MwrdCsoSynopsisParser mwrdParser = new MwrdCsoSynopsisParser(doc);

			List<CsoEvent> thisDaysEvents = mwrdParser.parseEvents();

			if (null == thisDaysEvents)
				continue;

			for (CsoEvent event : thisDaysEvents) {
				try {
					bufferedWriter.write(event.parseToString() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}		
		}

		try {
			bufferedWriter.close();
			fileWriter.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Parsing done");
	}
	
	public void parseToDb() {
	}
}
