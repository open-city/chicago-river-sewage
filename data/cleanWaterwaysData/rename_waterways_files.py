import os
from os.path import join

for root, dirs, files in os.walk('./'):
    for name in files:
        if 'aterways' in name:
            os.rename(join(root,name),join(root,cleaner_(name)))

def: cleaner_(name)
    return name.replace("_", "").replace(" ", "").lower()
