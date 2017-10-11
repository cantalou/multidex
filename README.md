# multidex
Fork from https://android.googlesource.com/platform/frameworks/multidex  
1. Fix few unexpected bugs with special rom.  
2. Add an alternative approach for loading dex since sometime MultiDex.install() execute successed but ClassNotFoundException throwed.
3. Use multi thread to extract and dexopt dex file parallel. The classesN dex file's size is the main factor affecting the time of opt, so you will get the best performance if you average all the file size of classesN dex file .
