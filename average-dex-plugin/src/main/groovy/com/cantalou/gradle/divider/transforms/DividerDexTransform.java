package com.cantalou.gradle.divider.transforms;

import com.android.build.gradle.internal.incremental.InstantRunBuildContext;
import com.android.build.gradle.internal.transforms.DexTransform;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.DexOptions;
import com.android.builder.internal.utils.FileCache;

import org.gradle.api.logging.Logger;

import java.io.File;

/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * @author cantalou
 * @date 2017-09-02 22:30
 */
public class DividerDexTransform extends DexTransform {
    public DividerDexTransform(DexOptions dexOptions, boolean debugMode, boolean multiDex, File mainDexListFile, File intermediateFolder, AndroidBuilder androidBuilder, Logger logger, InstantRunBuildContext instantRunBuildContext, FileCache buildCache) {
        super(dexOptions, debugMode, multiDex, mainDexListFile, intermediateFolder, androidBuilder, logger, instantRunBuildContext, buildCache);
    }
}
