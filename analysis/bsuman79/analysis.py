import pandas as pd
import numpy as np
import os


def mwrd(directory='../data/', file='mwrd_rain_measurements.csv'):
   data= pd.read_csv(directory+file, na_values='na')
   print data.shape[0]
   location= list(set(data.ix[0:,3]))

   lat=['nan',41.824857, 42.079825, 41.821587, 41.734139, 41.659136, 41.894294, 41.912744, 41.721979, 42.075623, 41.970287, 42.019087, 41.735587]    
   lng=['nan',-87.655586, -87.821792, -87.773094, -87.782139, -87.612606, -87.625152, -87.723944, -87.548275, -87.685299, -87.701213, -87.716341, -87.682604]

   latitude,longitude={},{}
   for key, val in zip(location, lat):
       latitude[key]=val
   for key, val in zip(location,lng):
       longitude[key]= val
   print len(latitude), len(longitude) 

   coord=np.zeros(shape=(data.shape[0],2))
   ct=0
   for loc in data.ix[:,3]:
      coord[ct,0]=latitude[loc]
      coord[ct,1]=longitude[loc]
      ct+=1

   data_new=np.hstack((data.ix[:,:],coord))
   
   print data_new.shape
   np.savetxt(file, data_new,fmt='%s', delimiter=",")

def clean_water(directory='../data/',file='clean-waterway-measurements.csv'):
    data=pd.read_csv(directory+file, na_values='na')
    np.savetxt(file, data,fmt='%s', delimiter=",")

def cso(directory='../data/', file='cso_events_timestamped.csv', file1='ssmma_mwrd_merge_cleanedup.csv'):
    readdata= pd.read_csv(directory+file1, na_values='na')    
    loc=list(readdata['TARP Connection'])
    lat=list(readdata['LAT_DEC'])
    lng=list(readdata['LON_CONV'])
    latitude,longitude={},{}
    for key, val in zip(loc, lat):
       latitude[key]=val
    for key, val in zip(loc,lng):
       longitude[key]= val
    #print len(latitude), len(longitude) 
    ct=0
    fw=open('jnk.csv','w')
    fw1=open('nocoord.csv','w')
    with open(directory+file,"r") as f:
      for line in f:
         lsplit=line.split(",")
         if len(lsplit)==4 and "(" not in lsplit[0]:
            try:
              print>>fw, '%s,%s,%s,%s,%s'%(lsplit[0], lsplit[-2],lsplit[-1], latitude[lsplit[0]],longitude[lsplit[0]])
              ct+=1
            except KeyError:
              print>>fw1, '%s,%s,%s,%s,%s'%(lsplit[0], lsplit[-2],lsplit[-1], 'nan','nan') 
              ct+=1   
         if len(lsplit)!=4 and "(" in lsplit[0]: 
            tmp=line.split("),")[0].split("(")[1].split(",")[0] 
            try: 
              print>>fw, '%s,%s,%s,%s,%s'%(tmp,lsplit[-2],lsplit[-1],latitude[tmp],longitude[tmp])
              ct+=1
            except KeyError:
              print>>fw1, '%s,%s,%s,%s,%s'%(tmp,lsplit[-2],lsplit[-1],'nan','nan')
              ct+=1     
         if len(lsplit)!=4 and "(" not in lsplit[0]:
             try:
               print>>fw, '%s,%s,%s,%s,%s'%(lsplit[0], lsplit[-2],lsplit[-1], latitude[lsplit[0]],longitude[lsplit[0]])
               ct+=1
             except KeyError:
                 print>>fw1, '%s,%s,%s,%s,%s'%(lsplit[0], lsplit[-2],lsplit[-1], 'nan','nan')
                 ct+=1                
    print ct                  
    fw.close()
    os.system('mv jnk.csv '+ file)

   

          

if __name__=="__main__":
    mwrd()
    cso()
    clean_water()
