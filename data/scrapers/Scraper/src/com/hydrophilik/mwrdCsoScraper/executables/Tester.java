package com.hydrophilik.mwrdCsoScraper.executables;


import org.joda.time.DateTime;

import com.hydrophilik.mwrdCsoScraper.Configures;
import com.hydrophilik.mwrdCsoScraper.db.DbConnection;
import com.hydrophilik.mwrdCsoScraper.parsing.CsoEvent;
import com.hydrophilik.mwrdCsoScraper.utils.DateTimeUtils;

public class Tester {

	public static void main(String[] args) throws Exception {
		
		DateTime startTime = new DateTime(DateTimeUtils.chiTimeZone).minusDays(1);
		DateTime endTime = new DateTime(DateTimeUtils.chiTimeZone);
		
		CsoEvent csoEvent = new CsoEvent(startTime, endTime, "Area 51", 69);
		
		Configures configuration = new Configures("/Users/scottbeslow/Downloads/mwrdScraperWorking");
		
		DbConnection dbConnect = new DbConnection(configuration);
		
		String stmt = csoEvent.getSqlInsert();
		
		dbConnect.executeUpdate(stmt);
		
		



	}

}
