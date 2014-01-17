import pandas as pd
import calendar

pieces = []

months = [calendar.month_name[i].lower() for i in range(1, 13)]
years = range(2005, 2013)

for year in years:
    for month in months:
        path = '%s/%s%s.csv' % (year, month, year)
        frame = pd.read_csv(path)
        pieces.append(frame)
flat = pd.concat(pieces, ignore_index=True)

# still unfinished
