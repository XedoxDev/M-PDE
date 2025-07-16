package org.xedox.mpde.project;

import android.content.Context;
import java.io.FileInputStream;
import org.xedox.apkbuilder.ApkBuilder;
import org.xedox.apkbuilder.util.ApkbuilderProperties;
import org.xedox.utils.ErrorDialog;
import org.xedox.utils.io.Assets;
import org.xedox.utils.io.FileX;
import org.xedox.utils.io.FileX;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import static org.xedox.apkbuilder.ApkBuilder.BuildConfig;

public class Project {
    private static final String ANDROID_JAR =
            "/storage/emulated/0/Android/data/org.xedox.mpde/files/android.jar";

    public final FileX path, buildConfigPath, dataDir, srcDir, buildDir;

    public Project(FileX path) {
        this.path = path;
        this.buildConfigPath = new FileX(path, "build.properties");
        this.dataDir = new FileX(path, "data");
        this.srcDir = new FileX(path, "src");
        this.buildDir = new FileX(path, "build");
    }

    public BuildConfig getBuildConfig() {
        return ApkbuilderProperties.loadFromFile(buildConfigPath.toFile());
    }

    public static BuildConfig create(Context ctx, String path, String name) {
        try {
            File dir = new File(path, name);
            if (!dir.mkdirs()) throw new IOException("Can't create project dir");

            createDirs(dir, "data", "src", "res", "build");
            createMainFile(ctx, new File(dir, "src"));
            extractRes(ctx, new File(dir, "res"));

            BuildConfig cfg = new BuildConfig();
            cfg.buildPath = new File(dir, "build/output").getAbsolutePath();
            cfg.resDir = new File(dir, "res").getAbsolutePath();

            saveProps(new File(dir, "build.properties"), cfg);
            return cfg;
        } catch (Exception e) {
            ErrorDialog.show(ctx, "Project creation failed", e);
            return null;
        }
    }

    private static void createDirs(File parent, String... dirs) throws IOException {
        for (String dir : dirs) {
            if (!new File(parent, dir).mkdir()) {
                throw new IOException("Can't create " + dir);
            }
        }
    }

    private static void createMainFile(Context ctx, File srcDir) throws IOException {
        FileX main = new FileX(srcDir, "main.pde");
        if (main.mkfile()) main.write(Assets.from(ctx).readText("build/main.pde"));
    }

    private static void extractRes(Context ctx, File resDir) {
        try {
            Assets.unzipFromAssets(ctx, "build/base-res.zip", resDir.getAbsolutePath());
        } catch (Exception e) {
            ErrorDialog.show(ctx, "Resource extraction failed", e);
        }
    }
    
    public String getProperty(String key) throws IOException {
    	Properties props = new Properties();
        props.load(new FileInputStream(buildConfigPath.toFile()));
        return props.getProperty(key);
    }
    
    public String getProperty(String key, String def) throws IOException {
    	Properties props = new Properties();
        props.load(new FileInputStream(buildConfigPath.toFile()));
        return props.getProperty(key, def);
    }

    private static void saveProps(File file, BuildConfig cfg) throws IOException {
        Properties p = new Properties();
        p.setProperty("appName", "My_processing_app");
        p.setProperty("androidJarPath", ANDROID_JAR);
        p.setProperty("appPackage", cfg.appPackage);
        p.setProperty("packageId", cfg.packageId);
        p.setProperty("versionName", cfg.versionName);
        p.setProperty("versionCode", cfg.versionCode);
        p.setProperty("minSdk", cfg.minSdk);
        p.setProperty("targetSdk", cfg.targetSdk);
        p.setProperty("javaVersion", cfg.javaVersion);
        p.setProperty("buildPath", cfg.buildPath);
        p.setProperty("resDir", cfg.resDir);
        p.setProperty("debugMode", String.valueOf(cfg.debugMode));
        p.setProperty("r8enabled", String.valueOf(cfg.r8enabled));
        p.setProperty("apkAlignEnable", String.valueOf(cfg.apkAlignEnable));
        p.setProperty("apkSignEnable", String.valueOf(cfg.apkSignEnable));
        p.setProperty("aapt2OptimizeEnable", String.valueOf(cfg.aapt2OptimizeEnable));

        try (OutputStream out = new FileOutputStream(file)) {
            p.store(out, "Build config");
        }
    }
}
