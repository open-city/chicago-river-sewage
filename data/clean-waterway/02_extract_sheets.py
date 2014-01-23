from mmap import mmap,ACCESS_READ
from xlrd import open_workbook, xldate_as_tuple
import datetime
import csv
import re

# useful for cleaning up the headers; 'pH   ' => 'ph'
def clean_string(value):
    return value.strip().lower().replace(" ", "_").replace("-", "_")

# resolves encoding issues that were occuring when processing the xls files
def process_cell(cell):
    if cell.ctype == 3:
        value = xldate_as_tuple(cell.value, wb.datemode)
        value = str(datetime.datetime(*value))
    else:
        value = unicode(cell.value).encode("utf-8")
        if isinstance(value, str):
            value = clean_string(value)
    return value

# removes the third row (empty row) and merges the second into the first
def process_first_rows(rows):
    rows.pop(2)
    old_second_row = rows.pop(1)
    rows[0] = [ cell_1 + '(' + cell_2 + ')' for cell_1, cell_2 in zip(rows[0], old_second_row) ]
    return rows

# checks last rows to see if they start with something that 'looks like' a date
# removes them if they don't
def process_last_rows(rows):
    while not re.match(r'\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}',rows[-1][0]) or not rows[-1][0]:
        print rows[-1][0]
        rows.pop(-1)
    return rows

def csv_filename_from(sheet, year):
    return str(year) + '/' + sheet.name.replace(" ", "").lower() + '.csv'

def waterways_workbook_for(year):
    return str(year) + '/waterways' + str(year) + '.xls'

def process_location_sheet(rows):
    rows.pop(1)
    rows[0] = [ cell.lower().replace('\n', '_') for cell in rows[0] ]
    rows[0][0] = 'sampling(point)'
    return rows

for year in range(2005,2013):
    wb = open_workbook(waterways_workbook_for(year))
    annual_water_sheets = [sheet for sheet in wb.sheets() if str(year) in sheet.name]
    location_sheet = [sheet for sheet in wb.sheets() if 'location' in sheet.name.lower()]
    for s in location_sheet:
        with open(csv_filename_from(s, year), 'w') as f:
            writer = csv.writer(f)
            sheet = [[process_cell(s.cell(row,col)) for col in range(s.ncols)] for row in range(s.nrows)]
            sheet = process_location_sheet(sheet)
            writer.writerows(sheet)

    for s in annual_water_sheets:
        with open(csv_filename_from(s, year), 'w') as f :
            writer = csv.writer(f)
            sheet = [[process_cell(s.cell(row,col)) for col in range(s.ncols)] for row in range(s.nrows)]
            sheet = process_first_rows(sheet)
            sheet = process_last_rows(sheet)
            writer.writerows(sheet)
