package org.opengovhacknight.parsing;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opengovhacknight.utils.DateTimeHelpers;

public class MwrdCsoSynopsisParser {
	
	private String startDate = null;
	private Document doc = null;
	
	public MwrdCsoSynopsisParser(Document doc) {
		this.doc = doc;

		startDate = getUValue(doc.getElementById("lbdate"));
	}

	public List<CsoEvent> parseEvents() {
		
		// StartDate cannot be null
		if (null == startDate) {
			System.out.println("ERROR: Found null start date in file\n");
			return null;
		}
		
		List<CsoEvent> events = new ArrayList<CsoEvent>();
		
		// If there are no events, then nothing to do here, move on.
		Element eventTable = doc.getElementById("DG1");
		if (null == eventTable)
			return null;

		// Parse through the table
		Elements eventsTags = eventTable.getElementsByTag("tr");		
		for (Element event : eventsTags) {
			Elements theRow = event.getElementsByTag("td");

			// Each row represents one event.  The event has five rows in it.
			// Create an array of strings, and place the values along the column
			// into it.
			int index = 0;
			String [] aTable = new String[5];			
			for (Element field : theRow) {
				aTable[index] = field.text();
				index++;
			}
			
			// If the value of the first column of this row is "Outfall Location", it
			// is the title row.  Skip it.
			if (!aTable[0].equals("Outfall Location")) {
				
				// A given synopsis page could cover multiple days.
				// Therefore, the build method could create multiple events
				// from just one row.
				// Example: Check out the MWRD Synopsis page for 4/18, which actually
				// covers 4/18-4/22.
				List<CsoEvent> newEvents = parseSynopsisRow(aTable);

				for (CsoEvent anEvent : newEvents)
					events.add(anEvent);
				
			}
		}

		return events;

	}
	
	private String getUValue(Element content) {
		
		if (null == content)
			return null;
		
		Elements links = content.getElementsByTag("u");
		String retVal = null;
		if (null != links) {
			retVal = links.first().text();
		}
		
		return retVal;
	}
	
	private List<CsoEvent> parseSynopsisRow(String [] row) {
		
		List<CsoEvent> retVal = new ArrayList<CsoEvent>();
		
		String outfallLocation = row[0];
		int waterwaySegment = Integer.parseInt(row[1]);

		DateTime start = DateTimeHelpers.createDateTime(startDate, row[2]);
		
		// Use the duration field to determine if the event goes into the next day(s)
		String [] durations = row[4].split(":");
		
		int durationMins;
		try {
			durationMins = (Integer.parseInt(durations[0]) * 60) + Integer.parseInt(durations[1]);
		}
		catch (Exception e) {
			System.out.println("Duration is not an int for outfall " + outfallLocation + " on " + startDate);
			return retVal;
		}
		
		// Calculate the end time based on the duration
		DateTime calculatedEndTime = new DateTime(start.plusMinutes(durationMins));
		
		int daylightSavingsHr = DateTimeHelpers.daylightSavingsDay(start.toLocalDate());
		if (0 != daylightSavingsHr) {
			if (DateTimeHelpers.timeInInterval(start, calculatedEndTime, "1:59")) {
				durationMins = durationMins + (60 * daylightSavingsHr);
				calculatedEndTime = new DateTime(start.plusMinutes(durationMins));
			}
		}

        if (start.toLocalDate().equals(calculatedEndTime.toLocalDate())) {
			CsoEvent csoEvent = new CsoEvent(start, calculatedEndTime,
					outfallLocation, waterwaySegment);
			retVal.add(csoEvent);
			return retVal;
		}
		
		// If we get here, it means that this particular row represents more than one line.
		// So, let's break it up
		
		//System.out.println("Outfall " + outfallLocation + " overflowed across days starting " + startDate);		

        while(!start.toLocalDate().equals(calculatedEndTime.toLocalDate())) {
			DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy");
			DateTime endOfThisDay = DateTimeHelpers.createDateTime(fmt.print(start), "23:59").plusMinutes(1);

			CsoEvent csoEvent = new CsoEvent(start, endOfThisDay, outfallLocation,
					waterwaySegment);
			retVal.add(csoEvent);
			
			start = DateTimeHelpers.createDateTime(fmt.print(start.plusDays(1)), "0:00");

		}
		
		CsoEvent csoEvent = new CsoEvent(start, calculatedEndTime, outfallLocation, waterwaySegment);
		
		retVal.add(csoEvent);
		
		return retVal;
	}
}
