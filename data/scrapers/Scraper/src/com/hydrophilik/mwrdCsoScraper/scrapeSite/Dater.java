/* TODO: REMOVE THIS FILE
package com.hydrophilik.mwrdCsoScraper.scrapeSite;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.orangewall.bezutils.beztime.BezDate;

public class Dater {
	Calendar calendar = null;
	
	public Dater(BezDate startDate, BezDate endDate) {
		 calendar = new GregorianCalendar(2013,11,1); //YYYY MM DD

		 String date = formatDate();
		 
		 System.out.println(date);
	}
	
	public String formatDate() {
		 return Integer.toString(calendar.get(Calendar.MONTH) + 1) + "/" +
				 Integer.toString(calendar.get(Calendar.DATE)) + "/" +
				 Integer.toString(calendar.get(Calendar.YEAR));
	}

	public boolean notEnd() {
		if (formatDate().equals("12/17/2013"))
			return false;
		
		return true;
	}
	
	public boolean nextDay() {
		if (formatDate().equals("12/31/2013"))
			return false;
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		return true;
	}
}
*/