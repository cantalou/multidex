# multidex
Fork from https://android.googlesource.com/platform/frameworks/multidex  
1. Fix few unexpected bugs with special rom.  
2. Add an alternative approach for loading dex since sometime MultiDex.install() execute successed but ClassNotFoundException throwed.
3. Use multi thread to extract and dexopt parallelly for reducing the first time launch.The classesN.dex file size has an effect on the performance. You will get the best performance inproved if you average all the file size of classesN.dex .
