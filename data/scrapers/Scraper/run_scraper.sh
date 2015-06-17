START_DATE=`/bin/date +%m/%d/%Y -d "1 month ago"`
END_DATE=`/bin/date +%m/%d/%Y -d "1 day ago"`

java -jar data/scrapers/Scraper/classes/artifacts/Scraper_jar/Scraper.jar ${START_DATE} ${END_DATE} data/processed_data/cso-data.db
git add .
git commit -m "scraped data for ${END_DATE}"
git push origin master
git push heroku master