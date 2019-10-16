# Multidex 
Fork from https://android.googlesource.com/platform/frameworks/multidex  
A library for speeding up app launch at the first time when you used google multi dex for compacting some devices which system version code were below 5.0 . 

### Problem  
The default implement of google MultiDex dex was serialization work.  
1. Extract every classesN.dex from apk  
2. Fork sub process for doing dex opt for every classesN.dex file
<img src="https://raw.githubusercontent.com/cantalou/multidex/master/doc/SerialExtractDexopt.jpg" width = "1000" height = "500" div align=left />  

### Feature
 
We can split task and work parallel in different thread to optimize ```MultiDex.install()```.   
<img src="https://raw.githubusercontent.com/cantalou/multidex/master/doc/ParallelExtractDexopt.jpg" width = "1000" height = "500" div align=left />  
   
