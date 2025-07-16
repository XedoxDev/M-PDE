package org.xedox.mpde.build;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.xedox.apkbuilder.ApkBuilder;
import org.xedox.mpde.AppCore;
import org.xedox.mpde.project.Project;
import org.xedox.utils.io.Assets;
import org.xedox.utils.io.FileX;

public class BuildTask {
    private static final String[] REQUIRED_LIBS = {
        "android.jar", 
        "appcompat-1.7.1.aar", 
        "processing-core.jar"
    };

    private final ApkBuilder apkBuilder;
    private final Context context;
    private final PrintStream out;
    private final Preprocessor preprocessor;
    private final AtomicBoolean isBuilding = new AtomicBoolean(false);

    public BuildTask(Context context, PrintStream out) {
        this.context = Objects.requireNonNull(context, "Context cannot be null");
        this.out = Objects.requireNonNull(out, "PrintStream cannot be null");
        this.apkBuilder = new ApkBuilder(context, out);
        this.preprocessor = new Preprocessor();
        copyRequiredResources();
    }

    private void copyRequiredResources() {
        Assets ass = Assets.from(context);
        File homeDir = AppCore.homeDir().toFile();
        
        for (String lib : REQUIRED_LIBS) {
            try {
                FileX destFile = new FileX(homeDir, lib);
                if (!destFile.exists()) {
                    out.println("Copying " + lib);
                    ass.copyFile("build/" + lib, destFile);
                }
            } catch (Exception err) {
                out.println("Failed to copy " + lib + ": " + err.getMessage());
                err.printStackTrace(out);
            }
        }
    }

    public void startBuild(Project project) {
        if (!isBuilding.compareAndSet(false, true)) {
            out.println("Build already in progress");
            return;
        }

        new Thread(() -> {
            try {
                build(project);
            } catch (Exception err) {
                err.printStackTrace(out);
                out.println("Build failed: " + err.getMessage());
            } finally {
                isBuilding.set(false);
            }
        }, "BuildThread").start();
    }

    private void build(Project project) throws IOException {
        Objects.requireNonNull(project, "Project cannot be null");
        out.println("Starting build for project: " + project.path.getName());

        prepareBuildDir(project);
        processManifest(project);
        processSources(project);

        out.println("Building APK...");
        apkBuilder.build(project.getBuildConfig());
        out.println("Build completed successfully");
    }

    private void prepareBuildDir(Project project) throws IOException {
        FileX buildDir = project.buildDir;
        if (!buildDir.exists() && !buildDir.mkdirs()) {
            throw new IOException("Failed to create build directory: " + buildDir);
        }

        deleteDirectory(new FileX(buildDir, "src"));
        deleteDirectory(new FileX(buildDir, "res"));
    }

    private void deleteDirectory(FileX directory) throws IOException {
        if (directory.exists()) {
            Files.walk(directory.toFile().toPath())
                .sorted(java.util.Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    private void processManifest(Project project) throws IOException {
        ApkBuilder.BuildConfig config = project.getBuildConfig();
        FileX manifest = new FileX(project.buildDir, "AndroidManifest.xml");

        String tempManifest = Assets.from(context)
            .readText("build/temp.AndroidManifest.xml")
            .replace("%permissions%", getPermissionsBlock(project))
            .replace("%app_name%", project.getProperty("appName", "MPDE App"))
            .replace("%package%", config.appPackage)
            .replace("%version_code%", project.getProperty("versionCode", "1"))
            .replace("%version_name%", project.getProperty("versionName", "1.0"))
            .replace("%min_sdk%", project.getProperty("minSdk", "21"))
            .replace("%target_sdk%", project.getProperty("targetSdk", "33"))
            .replace("%max_sdk%", project.getProperty("targetSdk", "33"));

        manifest.write(tempManifest);
        config.manifestPath = manifest.getAbsolutePath();
    }

    private String getPermissionsBlock(Project project) {
        return "";
    }

    private void processSources(Project project) throws IOException {
        FileX buildSrcDir = new FileX(project.buildDir, "src");
        if (!buildSrcDir.exists() && !buildSrcDir.mkdirs()) {
            throw new IOException("Failed to create source directory: " + buildSrcDir);
        }

        FileX mainActivity = new FileX(buildSrcDir, "MainActivity.java");
        String tempActivity = Assets.from(context)
            .readText("build/MainActivity.java")
            .replace("%package%", project.getBuildConfig().appPackage);
        mainActivity.write(tempActivity);

        FileX srcDir = new FileX(project.path, "src");
        if (srcDir.exists()) {
            List<FileX> sourceFiles = new ArrayList<>();
            try (Stream<Path> walk = Files.walk(srcDir.toFile().toPath())) {
                walk.filter(Files::isRegularFile)
                    .forEach(path -> {
                        File file = path.toFile();
                        if (file.getName().endsWith(".pde") || file.getName().endsWith(".java")) {
                            sourceFiles.add(new FileX(file));
                        }
                    });
            }

            for (FileX sourceFile : sourceFiles) {
                String relativePath = srcDir.getRelativePath(sourceFile);
                String destPath = sourceFile.getName().endsWith(".pde") 
                    ? relativePath.replace(".pde", ".java") 
                    : relativePath;
                
                FileX destFile = new FileX(buildSrcDir, destPath);
                destFile.getParentFile().mkdirs();

                if (sourceFile.getName().endsWith(".pde")) {
                    String javaCode = preprocessor.preprocess(sourceFile);
                    destFile.write(javaCode);
                    out.println("Processed PDE: " + relativePath + " -> " + destPath);
                } else {
                    sourceFile.copyTo(destFile);
                    out.println("Copied Java: " + relativePath);
                }
            }
        }
    }

    public void stopBuild() {
        if (isBuilding.get()) {
            apkBuilder.stopBuild();
            isBuilding.set(false);
            out.println("Build stopped by user");
        }
    }

    public boolean isBuilding() {
        return isBuilding.get();
    }
}