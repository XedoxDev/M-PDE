package org.xedox.mpde.project;

import java.util.Properties;
/*import org.xedox.apkbuilder.ApkBuilder;
import org.xedox.apkbuilder.util.ApkbuilderProperties;*/
import org.xedox.utils.io.FileX;
import org.xedox.utils.io.IFile;

public class Project {

    public IFile path;
    public IFile buildConfigPath;

    public Project(IFile path) {
        this.path = path;
        buildConfigPath = new FileX(path, "build.properties");
    }

    /*public ApkBuilder.BuildConfig getBuildConfig() {
        return ApkbuilderProperties.loadFromFile(buildConfigPath.toFile());
    }*/
}
