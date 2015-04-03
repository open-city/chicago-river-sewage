package org.opengovhacknight;

import org.joda.time.LocalDate;
import org.opengovhacknight.parsing.Scrape;
import org.opengovhacknight.utils.DateTimeHelpers;

public class ScrapeMwrd {

    // arg[0] - Start of scrape MM/DD/YYYY
    // arg[1] - End of scrape MM/DD/YYYY
    // arg[2] - Path to the database (including 
    public static void main(String[] args) {

        String startDateStr;
        String endDateStr;
        String pathToDb;

        LocalDate startDate;
        LocalDate endDate;
        
        Scrape scrape = null;

        try {
            startDateStr = args[0];
            endDateStr = args[1];
            pathToDb = args[2];
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Must include 3 arguments, start date, end date, and path to repo");
            return;
        }

        try {
            startDate = DateTimeHelpers.convertDateString(startDateStr);
            endDate = DateTimeHelpers.convertDateString(endDateStr).plusDays(1); // Add one day because we want to include this date
            scrape = new Scrape(pathToDb);

        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        scrape.doScrape(startDate, endDate);

    }

}
