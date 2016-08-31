from __future__ import absolute_import, division, print_function, unicode_literals

import csv
from datetime import datetime, timedelta
import requests
from bs4 import BeautifulSoup
import os
import sqlite3

data_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
processed_data_dir = os.path.join(data_dir, 'processed_data')

mwrd_url = 'http://apps.mwrd.org/csoreports/CSO_Synopisis_Report'
headers = {
    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36',
    'Connection': 'keep-alive',
    'Accept-Encoding': 'gzip, deflate'
}
drop_keys = [
    'bttSearchDay',
    'bttResetSearch2',
    'bttSearch',
    'bttResetSearch',
    'ReportViewer1$ToggleParam$img'
]
payload = dict()

# Mapping of segment names to segment integer IDs
UPDATED_SEGMENTS = {
    "NSC Upper (NSWRP)": 1,
    "NSC Lower (NSWRP)": 2,
    "NBCR Lower (NSC Confluence)": 3,
    "NBCR Upper (NSC Confluence)": 4,
    "Chicago R": 5,
    "SB Chicago R": 6,
    "SF SB Chicago R": 7,
    "CSSC Upper (SWRP)": 8,
    "CSSC Lower (SWRP)": 9,
    # Default for CSSC Lower (SWRP) is 9, rules checking for other values (10, 11)
    "Weller Cr": 12,
    "DesPlaines Upper": 13,
    "DesPlaines Middle": 14,
    "DesPlaines Lower": 15,
    "Salt Cr": 16,
    "Cal R": 17,
    "Grand Cal R": 18,
    "Little Cal R (North)": 19,
    "Little Cal R (South)": 20,
    "Cal Sag Ch": 21,
    "Cal Union drainage Ditch": 22,
    "Addison Cr": 23
    # 30, 31, and 32 follow different rules, not sure about 31 overall
    # Haven't seen any examples for 32 O'Brien
}

def scrape_table(start_date, end_date):
    s = requests.Session()
    mwrd_response = s.get(mwrd_url)
    mwrd_soup = BeautifulSoup(mwrd_response.content, 'html.parser')
    all_inputs = mwrd_soup.find_all('input')

    # Get all inputs, set values based off of values on page, or empty string
    for input_val in all_inputs:
        if input_val.get('value'):
            payload[input_val['name']] = input_val['value']
        else:
            payload[input_val['name']] = ''

    # Set mandatory payload values
    payload['__EVENTTARGET'] = 'bttSearch'
    payload['txtStartDateSearch'] = start_date
    payload['txtEndDateSearch'] = end_date
    payload['ReportViewer1$ctl11'] = 'standards'

    # Some input fields (listed above) cause scraping to fail, remove them
    for key in drop_keys:
        if payload.get(key):
            payload.pop(key)

    mwrd_post_response = requests.post(mwrd_url,
                                       data=payload,
                                       headers=headers,
                                       cookies=s.cookies)
    mwrd_post_soup = BeautifulSoup(mwrd_post_response.content, 'html.parser')

    # MWRD markup is not well formatted, there are many nested tables, so this
    # is the best CSS selector so far
    cso_table = mwrd_post_soup.select("table[cols=9]")
    # Getting all rows except placeholder row which is blank and does not share
    # the valign top attribute
    table_rows = cso_table[0].select("tr[valign='top']")
    table_list = []

    for row in table_rows:
        row_cells = row.select('div')
        row_list = []
        for cell in row_cells:
            row_list.append(cell.text.strip())
        table_list.append(row_list)

    mod_filename = date_today.replace('/', '-')
    mwrd_filepath = os.path.join
    with open('data/mwrd_scraper_{}.csv'.format(mod_filename), 'w') as mwrd_file:
        mwrd_writer = csv.writer(mwrd_file, delimiter=',', lineterminator='\n')
        for row in table_list:
            mwrd_writer.writerow(row)

def write_to_db(updated_table):
    con = sqlite3.connect(os.path.join(processed_data_dir, 'cso-data.db'))
    cur = con.cursor()
    insert_records = list()
    check_records = [(i['location'],
                      i['segment'],
                      i['date'],
                      i['starttime'],
                      i['endtime'],
                      i['duration']) for i in updated_table]

    cur.execute("SELECT location, segment, date, starttime, endtime, duration FROM CSOs;")
    results = cur.fetchall()
    for r in check_records:
        if r not in results:
            insert_records.append(r)

    cur.executemany("INSERT INTO CSOs (location, segment, date, starttime, endtime, duration) VALUES (?, ?, ?, ?, ?, ?);",
                    insert_records)
    con.commit()
    con.close()

# Updates CSV format to be compatible with existing database
def reformat_compatible(mwrd_file):
    with open(mwrd_file, 'r') as mwrd_csv:
        mwrd_reader = csv.DictReader(mwrd_csv)
        mwrd_table = list(mwrd_reader)
    updated_mwrd_table = list()
    for row in mwrd_table:
        updated_row = dict()
        updated_row['location'] = row['Outfall Structure']

        if row['Outfall Location'] == 'Sheridan Rd (Wilmette P.S.) (W)':
            updated_row['segment'] = 31
        elif row['Outfall Location'] == 'Wilmette Gate':
            updated_row['segment'] = 30
        elif 'Lemont' in row['Outfall Location']:
            updated_row['segment'] = 11
        else:
            try:
                updated_row['segment'] = UPDATED_SEGMENTS[row['Waterway Reach']]
            except:
                continue

        start_datetime = datetime.strptime(row['Open date/time'], "%m/%d/%Y %H:%M:%S %p")
        end_datetime = datetime.strptime(row['Close date/time'], "%m/%d/%Y %H:%M:%S %p")
        duration = datetime.strptime(row['Gate Open Period'], ':%H:%M:%S')
        duration_min = timedelta(hours=duration.hour,
                                 minutes=duration.minute,
                                 seconds=duration.second)

        updated_row['date'] = start_datetime.strftime("%Y-%m-%d")
        updated_row['starttime'] = start_datetime.strftime("%H:%M")
        updated_row['endtime'] = end_datetime.strftime("%H:%M")
        updated_row['duration'] = int(duration_min.total_seconds() / 60)
        updated_mwrd_table.append(updated_row)

    updated_mwrd_table.sort(key=lambda x: (x['date'], x['starttime']))
    mod_filename = date_today.replace('/', '-')
    with open('data/mwrd_scraper_compatible_{}.csv'.format(mod_filename), 'w') as mwrd_clean:
        mwrd_writer = csv.DictWriter(mwrd_clean,
                                     fieldnames=['location','segment','date','starttime','endtime','duration'],
                                     delimiter=',',
                                     lineterminator='\n')
        mwrd_writer.writeheader()
        mwrd_writer.writerows(updated_mwrd_table)

    write_to_db(updated_mwrd_table)

if __name__ == '__main__':
    date_today = datetime.now().strftime("%m/%d/%Y")
    # Start from first day after data collection stopped
    scrape_table('05/15/2016', date_today)
    file_suffix = date_today.replace('/', '-')
    reformat_compatible('data/mwrd_scraper_{}.csv'.format(file_suffix))
