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

	
/*
	private static void scrapeLastThirtyDays() {
		
		//int DAYS_AWAY = 1;
		
		//LocalDate today = new LocalDate(2014, 4, 10);
		LocalDate today = new LocalDate(DateTimeUtils.chiTimeZone);
		LocalDate date = today.minusDays(30);
		//LocalDate date = new LocalDate(2007, 1, 1);

		//LocalDate date = today.minusDays(DAYS_AWAY);
		
		while (false == DateTimeUtils.isSameDay(today, date)) {
			
			Map<String, List<CsoEvent>> csoEvents = null;
			try {
				csoEvents = Scraper.grabEventFromSite(date);
				List<String> sqlCommands = convertEventsToSql(csoEvents);
				for (String sqlCommand : sqlCommands) {
					dbConn.executeUpdate(sqlCommand);
				}
			}
			catch (Exception e) {
				LogLogger.logError(e);
			}
			
			date = date.plusDays(1);
		}
	}
	
	private static List<String> convertEventsToSql(Map<String, List<CsoEvent>> csoEvents) {
		
		List<String> retVal = new ArrayList<String>();

		for (String key : csoEvents.keySet()) {
			String [] keySplit = key.split(CsoEvent.seperator);
			String date = keySplit[0];
			String outfallLocation = keySplit[1];
			
			List<CsoEvent> csoEventsFromDb = null;
			try {
				csoEventsFromDb = dbConn.getCsosAtDatePlace(date, outfallLocation);
			}
			catch (Exception e) {
				LogLogger.logError(e);
				continue;
			}
			
			List<CsoEvent> scrapedEvents = csoEvents.get(key);
			if ((null == csoEventsFromDb) || (0 == csoEventsFromDb.size())) {
				for (CsoEvent scrapedEvent : scrapedEvents) {
					retVal.add(scrapedEvent.getSqlInsert());
				}
			}
			else {
				for (CsoEvent scrapedEvent : scrapedEvents) {
					
					boolean sameEventFound = false;
					
					for (CsoEvent dbEvent : csoEventsFromDb) {
						if (sameEvent(scrapedEvent, dbEvent)) {
							sameEventFound = true;
							continue;
						}
						if (overlap(scrapedEvent, dbEvent)) {
							LogLogger.log("The website updated event: " + scrapedEvent.getKey());
							String sqlCmd = dbEvent.getSqlRemove();
							retVal.add(sqlCmd);
							
						}
					}
					
					if (false == sameEventFound)
						retVal.add(scrapedEvent.getSqlInsert());

				}
			}
		}
		
		return retVal;
	}
	
	private static boolean overlap(CsoEvent event1, CsoEvent event2) {
		if (event1.getStartTime().isBefore(event2.getStartTime())) {
			if (false == event1.getEndTime().isBefore(event2.getStartTime())) {
				return true;
			}
		}
		else {
			if (false == event2.getEndTime().isBefore(event1.getStartTime())) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean sameEvent(CsoEvent event1, CsoEvent event2) {
		if (false == event1.getStartTime().toString().equals(event2.getStartTime().toString())) {
			return false;
		}
		
		if (false == event1.getEndTime().toString().equals(event2.getEndTime().toString())) {
			return false;
		}
		
		return true;
	}
	
	private static void convertDbToCsv(List<CsoEvent> csoEvents, String workingDirName) throws Exception {
		
		File csvFile = new File(workingDirName + "/" + csvFileName);
		
		if (csvFile.exists())
			csvFile.delete();
		
		csvFile.createNewFile();
		
		FileWriter fw = null;
		BufferedWriter bw = null;
		
		try {
		
			fw = new FileWriter(csvFile.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			
			bw.write("OutfallLocation;WaterwaySegment;Date;StartTime;EndTime;DurationMins");
			bw.newLine();
			
			for (CsoEvent csoEvent : csoEvents) {
				bw.write(csoEvent.parseToString());
				bw.newLine();
				
			}
		}
		catch (Exception e) {
			throw new Exception(e);
		}
		finally {
			try {
				bw.close();
				fw.close();
			}
			catch (Exception e) {}
		}
}
*/