import os
from os.path import join

for root, dirs, files in os.walk('./'):
  for name in files:
    if 'Waterways' in name:
      os.rename(join(root,name),join(root,name.replace("_","").replace(" ","").lower()))
