/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.support.multidex;

import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import dalvik.system.BaseDexClassLoader;

/**
 * A class loader that loads classes from any .dex file in a particular directory on the SD card.
 * <p>
 * <p>Used to implement incremental deployment to Android phones.
 */
public class IncrementalClassLoader extends ClassLoader {

    private final DelegateClassLoader delegateClassLoader;

    private BaseDexClassLoader originalClassLoader;

    public IncrementalClassLoader(
            ClassLoader original, String nativeLibraryPath, String codeCacheDir, List<String> dexes) {
        super(original.getParent());
        // TODO(bazel-team): For some mysterious reason, we need to use two class loaders so that
        // everything works correctly. Investigate why that is the case so that the code can be
        // simplified.
        delegateClassLoader = createDelegateClassLoader(nativeLibraryPath, codeCacheDir, dexes, original);
        if(original instanceof BaseDexClassLoader){
            originalClassLoader = (BaseDexClassLoader)original;
        }
    }

    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        return delegateClassLoader.findClass(className);
    }

    /**
     * A class loader whose only purpose is to make {@code findClass()} public.
     */
    private static class DelegateClassLoader extends BaseDexClassLoader {
        private DelegateClassLoader(String dexPath, File optimizedDirectory, String libraryPath, ClassLoader parent) {
            super(dexPath, optimizedDirectory, libraryPath, parent);
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }
    }

    private static DelegateClassLoader createDelegateClassLoader(String nativeLibraryPath, String codeCacheDir, List<String> dexes, ClassLoader original) {
        String pathBuilder = createDexPath(dexes);
        return new DelegateClassLoader(pathBuilder, new File(codeCacheDir), nativeLibraryPath, original);
    }

    private static String createDexPath(List<String> dexes) {
        StringBuilder pathBuilder = new StringBuilder();
        boolean first = true;
        for (String dex : dexes) {
            if (first) {
                first = false;
            } else {
                pathBuilder.append(File.pathSeparator);
            }
            pathBuilder.append(dex);
        }
        return pathBuilder.toString();
    }

    private static void setParent(ClassLoader classLoader, ClassLoader newParent) throws NoSuchFieldException, IllegalAccessException {
        Field parent = ClassLoader.class.getDeclaredField("parent");
        parent.setAccessible(true);
        parent.set(classLoader, newParent);

    }

    public static ClassLoader inject(ClassLoader classLoader, String nativeLibraryPath, String codeCacheDir, List<String> dexes) throws NoSuchFieldException, IllegalAccessException{
        IncrementalClassLoader incrementalClassLoader = new IncrementalClassLoader(classLoader, nativeLibraryPath, codeCacheDir, dexes);
        setParent(classLoader, incrementalClassLoader);
        // This works as follows:
        // We're given the current class loader that's used to load the bootstrap application.
        // We have a new class loader which reads patches/overrides from the data directory
        // instead. We want *that* class loader to have the bootstrap class loader's parent
        // as its parent, and then we make the bootstrap class loader parented by our
        // class loader.
        //
        // In other words, we have this:
        //      BootstrapApplication.classLoader = ClassLoader1, parent=ClassLoader2
        // We create ClassLoader3 from the .dex files in the data directory, and arrange for
        // the hierarchy to be like this:
        //      BootstrapApplication.classLoader = ClassLoader1, parent=ClassLoader3, parent=ClassLoader2
        // With this approach, a class find (which should always look at the parents first) should
        // find anything from ClassLoader3 before they get them from ClassLoader1.
        // (Note that ClassLoader2 in the above is generally the BootClassLoader, not containing
        // any classes we care about.)
        return incrementalClassLoader;
    }

    @Override
    protected String findLibrary(String libName) {
        try {
            return super.findLibrary(libName);
        } catch (Throwable e) {
            Log.e("IncrementalClassLoader","findLibrary error", e);
            if(originalClassLoader != null){
                originalClassLoader.findLibrary(libName);
            }
        }
        return "";
    }
}