cd scrapers/python_mwrd_scraper
python mwrd_scraper.py
git add ../../processed_data/cso-data.db
git commit -m "scraped data for the last 30 days"
git push origin master
git push heroku master