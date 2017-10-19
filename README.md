# multidex
Fork from https://android.googlesource.com/platform/frameworks/multidex

## Introduction
1. Fix few unexpected bugs with special rom.  
2. Add an alternative approach for loading dex since sometime MultiDex.install() execute successed but ClassNotFoundException throwed.
3. Use multi thread to extract and dexopt dex file parallel. The classesN dex file's size(method count) is the main factor affecting the time of opt, so we will get the best performance if we average all the file size of classesN dex file .

## Performance  
 ![image](https://github.com/cantalou/multidex/blob/master/app/PerformanceImproveInfo.png)

## How to use
1. add miltidex lib dependency
   ```

   ```
