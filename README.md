# Multidex 
Fork from Google [MultiDex](https://android.googlesource.com/platform/frameworks/multidex)  
A library for speeding up app launch at the first time when you used google multi dex for compacting some devices which system version code were below 5.0 .   
Performance can be increased by ```30%-65%```.

### Problem  
The default implement of google MultiDex dex was serialization work.  
1. Extract every classesN.dex from apk  
2. Fork sub process for doing dex opt for every classesN.dex file
<img src="https://raw.githubusercontent.com/cantalou/multidex/master/doc/SerialExtractDexopt.jpg" width = "1000" height = "500" div align=left />  

### Feature
We can split task and work parallel in different thread to optimize ```MultiDex.install()```.   
<img src="https://raw.githubusercontent.com/cantalou/multidex/master/doc/ParallelExtractDexopt.jpg" width = "1000" height = "500" div align=left />  


### How to use  
Add below code in build.gradle of your app project and nothing to do with java code.
```
    dependencies {
       compile 'com.cantalou:multidex:1.0.1' 
    }
    
    configurations.all {
        exclude group: 'com.android.support', module: 'multidex'
    }
```   

### Performance  
There three dex in test apk. Every dex contains about 40000+ methods.
| Device               | Version |   Before(s)  |  After(s) |
|---|---|---|---|
| LenovoA3800          |  4.4.2  |    14.495    |   7.455   |  
| Honor H30            |  4.2.2  |    9.173     |   3.221   |
| Xiaomi HM NOTE 1S    |  4.4.4  |    11.716    |   5.171   |
| Genymotion(run in pc)|  4.1.1  |    2.288     |   0.512   |
