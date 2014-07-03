package com.hydrophilik.mwrdCsoScraper.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

import com.hydrophilik.mwrdCsoScraper.db.DbConnection;
import com.hydrophilik.mwrdCsoScraper.parsing.CsoEvent;
import com.hydrophilik.mwrdCsoScraper.scrapeSite.Scraper;
import com.hydrophilik.mwrdCsoScraper.utils.DateTimeUtils;
import com.hydrophilik.mwrdCsoScraper.utils.LogLogger;

public class Scrape {
	
	public static DbConnection dbConn = null;
	
	public Scrape(String configFile) throws Exception {
		dbConn = new DbConnection(configFile);
	}
	
	public void doScrape(LocalDate startDate, LocalDate endDate) {
		LocalDate date = startDate;
		
		while (false == DateTimeUtils.isSameDay(date, endDate)) {
			
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
					retVal.add(scrapedEvent.getSqlInsertSqlite());
				}
			}
			else {
				for (CsoEvent scrapedEvent : scrapedEvents) {
					
					boolean sameEventFound = false;
					
					for (CsoEvent dbEvent : csoEventsFromDb) {
						if (scrapedEvent.sameEvent(dbEvent)) {
							sameEventFound = true;
							continue;
						}
						if (scrapedEvent.overlap(dbEvent)) {
							LogLogger.log("The website updated event: " + scrapedEvent.getKey());
							String sqlCmd = dbEvent.getSqlRemove();
							retVal.add(sqlCmd);
							
						}
					}
					
					if (false == sameEventFound)
						retVal.add(scrapedEvent.getSqlInsertSqlite());

				}
			}
		}
		
		return retVal;
	}
}