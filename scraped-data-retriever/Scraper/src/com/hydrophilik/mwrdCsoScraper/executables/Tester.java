package com.hydrophilik.mwrdCsoScraper.executables;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.hydrophilik.mwrdCsoScraper.parsing.CsoEvent;
import com.hydrophilik.mwrdCsoScraper.parsing.MwrdCsoSynopsisParser;

public class Tester {

	public static void main(String[] args) {
		Document doc;
		
		File input = new File("/Users/scottbeslow/Downloads/mwrd/scrapedSites/20070425.txt");

		try {
			doc = Jsoup.parse(input, "UTF-8", "");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		
		MwrdCsoSynopsisParser mwrdParser = new MwrdCsoSynopsisParser(doc);

		List<CsoEvent> thisDaysEvents = mwrdParser.parseEvents();

	}

}
