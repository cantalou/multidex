package com.cantalou.gradle.divider.transforms

import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.transforms.JarMergingTransform
import org.gradle.api.Project

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
