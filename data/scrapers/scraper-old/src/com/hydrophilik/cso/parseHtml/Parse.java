package com.hydrophilik.cso.parseHtml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.hydrophilik.cso.Configuration;

/*
 * This class takes in a directory of html files that were scraped from the MWRD CSO site, parses them,
 * and creates a series of CSV files with the data.
 */
public class Parse {

	public static void main(String[] args) {

		if (false == Configuration.setup()) {
			System.out.println("Failed to set up configuration.  Bailing...");
			return;
		}

		File eventsFile = new File(Configuration.writeDirectory + "events" + Configuration.eventsSuffix + ".csv");

		// If the output file already exists, it will be deleted and re-written
		if (eventsFile.exists()) {
			eventsFile.delete();
		}
		
		FileWriter fwEv = null;
		
		try {
			eventsFile.createNewFile();
			fwEv = new FileWriter(eventsFile.getAbsoluteFile());
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}

		BufferedWriter bwEv = new BufferedWriter(fwEv);
		
		// Loop through all of the files, and parse the data into structures.
		File dir = new File(Configuration.readDirectory);
		
		for (File input : dir.listFiles()) {
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
					bwEv.write(event.parseToString() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}		
		}
		
		try {
			bwEv.close();
			fwEv.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Parsing done");
	}
}
