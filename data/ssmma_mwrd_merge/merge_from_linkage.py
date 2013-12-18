import csv

def csv_to_dict(filename):
  data_d = []
  with open(filename) as f:
      reader = csv.DictReader(f)
      for row in reader:
          data_d.append(row)

  print filename, len(data_d)
  # print data_d[0]
  return data_d

linkage = csv_to_dict('ssmma_mwrd_linkage_1.csv')
linkage_clean = []
for row in linkage:
  if row['Cluster ID'] != 'x':
    linkage_clean.append(row)

print len(linkage_clean)

mwrd = csv_to_dict('mwrd-tarp-connection-database.csv')
ssmma = csv_to_dict('SSMMA_Combined_Sewer_Overflow_locations.csv')

# for link in linkage:
#   for cluster

# with open("ssmma_mwrd_merge.csv", "a") as outfile:
#   with open("../gpx/%s" % gpx_file) as f:
#     for line in f:
#       if not '<?xml' in line and not '<gpx' in line and not '<metadata' in line and not '</gpx>' in line:
#         outfile.write(line)