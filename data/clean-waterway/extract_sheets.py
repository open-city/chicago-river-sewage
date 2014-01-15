from mmap import mmap,ACCESS_READ
from xlrd import open_workbook, xldate_as_tuple
import datetime
import csv

def clean_string(value):
    return value.strip().lower().replace(" ", "_").replace("-", "_")

def process(cell):
    if cell.ctype == 3:
        value = xldate_as_tuple(cell.value, wb.datemode)
        value = str(datetime.datetime(*value))
    else:
        value = unicode(cell.value).encode("utf-8")
        if isinstance(value, str):
            value = clean_string(value)
    return value

def csv_filename_from(sheet, year):
    return str(year) + '/' + sheet.name.replace(" ", "").lower() + '.csv'

def waterways_workbook_for(year):
    return str(year) + '/waterways' + str(year) + '.xls'

for year in range(2005,2013):
    wb = open_workbook(waterways_workbook_for(year))
    annual_water_sheets = [sheet for sheet in wb.sheets() if str(year) in sheet.name]
    for s in annual_water_sheets:
        with open(csv_filename_from(s, year), 'w') as f :
            writer = csv.writer(f)
            rows = [[process(s.cell(row,col)) for col in range(s.ncols)] for row in range(s.nrows)]
            writer.writerows(rows)
