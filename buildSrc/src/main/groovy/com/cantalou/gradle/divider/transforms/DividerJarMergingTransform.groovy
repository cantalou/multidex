package com.cantalou.gradle.divider.transforms

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.transforms.JarMergingTransform
import org.gradle.api.Project;

import java.io.IOException;
import java.util.Set;

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
 * @date 2017-09-02 22:27
 */

public class DividerJarMergingTransform extends JarMergingTransform {

    private Project project

    public DividerJarMergingTransform(Project project, JarMergingTransform transform) {
        super(transform.getScopes());
        this.project = project
    }

    @Override
    public void transform(TransformInvocation invocation) throws TransformException, IOException {
        super.transform(invocation);
    }
}
