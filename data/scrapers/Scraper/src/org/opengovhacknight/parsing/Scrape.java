package org.opengovhacknight.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.opengovhacknight.db.DbConnection;


public class Scrape {

    private static DbConnection dbConn;

    public static String mwrdWebPrefix = "http://apps.mwrd.org/CSO/CSOEventSynopsisReport.aspx?passdate=";

    public Scrape(String dbLocation) {
        dbConn = new DbConnection(dbLocation);
    }

    public static DbConnection getDbConn() {
        return dbConn;
    }

    public void doScrape(LocalDate startDate, LocalDate endDate) {
		LocalDate date = startDate;

        while(!date.equals(endDate)) {

			Map<String, List<CsoEvent>> csoEvents;

			try {
				csoEvents = grabEventFromSite(date);
				List<String> sqlCommands = convertEventsToSql(csoEvents, dbConn);
				for (String sqlCommand : sqlCommands) {
                    dbConn.executeUpdate(sqlCommand);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
                // TODO: Do something more noticable here
			}

			date = date.plusDays(1);
		}
	}
	
	private static List<String> convertEventsToSql(Map<String, List<CsoEvent>> csoEvents, DbConnection dbConnection) {
		
		List<String> retVal = new ArrayList<String>();

		for (String key : csoEvents.keySet()) {
			String [] keySplit = key.split(CsoEvent.seperator);
			String date = keySplit[0];
			String outfallLocation = keySplit[1];
			
			List<CsoEvent> csoEventsFromDb;
			try {
				csoEventsFromDb = dbConnection.getCsosAtDatePlace(date, outfallLocation);
			}
			catch (Exception e) {
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
							String sqlCmd = dbEvent.getSqlRemove();
							retVal.add(sqlCmd);
							
						}
					}
					
					if (!sameEventFound)
						retVal.add(scrapedEvent.getSqlInsertSqlite());

				}
			}
		}
		
		return retVal;
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

    /* TODO: Delete this

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
*/


}