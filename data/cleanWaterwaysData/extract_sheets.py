from mmap import mmap,ACCESS_READ
from xlrd import open_workbook,xldate_as_tuple
import datetime
import csv

for year in range(2005,2013):
  wb = open_workbook(str(year) + '/waterways' + str(year) + '.xls')
  for s in wb.sheets():
    if str(year) in s.name :	
      print 'Sheet:',s.name
      with open(str(year) + '/' + s.name.replace(" ", "") + '.csv', 'w') as f :
        writer = csv.writer(f)
        for row in range(s.nrows):
          values = []
          for col in range(s.ncols):
            value = ''
            cell = s.cell(row,col)
            if cell.ctype == 3:
              value = xldate_as_tuple(cell.value,wb.datemode)
              value = str(datetime.datetime(*value))
            else:
              value = unicode(cell.value).encode("utf-8")
            values.append(value)
          writer.writerow(values)
