## MWRD Scraper

MWRD updated the page displaying CSO events to a new link which requires a cookie
to submit a form, and has the data in a slightly different format at http://apps.mwrd.org/csoreports/CSO_Synopisis_Report

This scraper pulls all data from the form for the supplied start and end dates,
adds the data in the same format as the page to `data/mwrd_scraper_${DATE}.csv`,
updates the format to match the current SQLite database schema at
`data/mwrd_scraper_compatible_${DATE}.csv`, and then adds the SQLite-friendly
data to the database while not adding any duplicate values.

### Commands

To run the scraper (with both the start and end date specified), run:

`python mwrd_scraper.py MM/DD/YYY MM/DD/YYY`

Both date arguments are optional and default to the current date. If only one date
argument is provided, it is set as the start date, and the end date still defaults
to the current date.

### Replace Overlapping Data

While the existing data goes up until mid-May of 2016, MWRD has data in the updated
format since 4/1/2016. If you want to replace all overlapping data, switch out the
existing function call under `if __name__ == '__main__'` to run `replace_overlap()`.

This will scrape the MWRD site, pull the earliest date it finds, delete all current
rows with dates at or after that one, and then insert all the new rows.
