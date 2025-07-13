package org.xedox.apkbuilder;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.xedox.apkbuilder.task.AlignTask;
import org.xedox.apkbuilder.task.CleanTask;
import org.xedox.apkbuilder.task.CompileResourcesTask;
import org.xedox.apkbuilder.task.LinkResourcesTask;
import org.xedox.apkbuilder.task.CompileSourcesTask;
import org.xedox.apkbuilder.task.DexingClassesTask;
import org.xedox.apkbuilder.task.OptimizeTask;
import org.xedox.apkbuilder.task.PackageResourcesTask;
import org.xedox.apkbuilder.task.SignTask;
import org.xedox.apkbuilder.util.BinaryUtils;
import org.xedox.apkbuilder.util.TaskManager;
import org.xedox.apkbuilder.util.BuildException;
import org.xedox.javac.JavacOptionsBuilder;

public class ApkBuilder {
    
    public final TaskManager taskManager;
    public  BuildConfig config;
    public boolean debugCommands;
    public File aapt2Binary;
    public File compiledResDir;
    public File genDir;
    public File outputApk;
    public File classesDir;
    public File dexDir;
    public Context context;
    public static final Object keyCopyLock = new Object();
    
    private BuildListener buildListener;
    private volatile boolean stopRequested = false;

    public ApkBuilder(Context context, PrintStream printStream) {
        this.context = context;
        this.taskManager = new TaskManager(Objects.requireNonNull(printStream));
        String nativeDir = context.getApplicationInfo().nativeLibraryDir;
        aapt2Binary = new File(nativeDir, "libaapt2.so");
        debugCommands = config.debugMode;
        taskManager.setVerbose(config.debugMode);

        this.compiledResDir = new File(config.buildPath, "compiled_res");
        this.genDir = new File(config.buildPath, "gen");
        this.outputApk = new File(genDir, "resources.ap_");
        this.classesDir = new File(config.buildPath, "classes");
        this.dexDir = new File(config.buildPath, "dex");
    }

    public interface BuildListener {
        void onBuildStarted();
        void onBuildProgress(String taskName, int progress);
        void onBuildCompleted(boolean success, String message);
        void onBuildError(String error);
    }

    public void setBuildListener(BuildListener listener) {
        this.buildListener = listener;
    }

    public void stopBuild() {
        stopRequested = true;
        if (buildListener != null) {
            buildListener.onBuildError("Build been stopped");
        }
    }

    public boolean isStopRequested() {
        return stopRequested;
    }

    public void build(BuildConfig config) {
        this.config = config;
        if (buildListener != null) {
            buildListener.onBuildStarted();
        }

        try {
            validateBuildEnvironment();
            taskManager.task(
                    "Building APK",
                    () -> {
                        taskManager.start();
                        if (isStopRequested()) return;

                        executeTask("Cleaning build directory...", new CleanTask(this), 10);
                        if (isStopRequested()) return;

                        executeTask("Compiling resources with ECJ...", new CompileResourcesTask(this), 20);
                        if (isStopRequested()) return;

                        executeTask("Linking resources with aapt2...", new LinkResourcesTask(this), 30);
                        if (isStopRequested()) return;

                        executeTask("Compiling sources with aapt2...", new CompileSourcesTask(this), 40);
                        if (isStopRequested()) return;

                        executeTask("Dexing classes with D8...", new DexingClassesTask(this), 50);
                        if (isStopRequested()) return;

                        executeTask("Packing resources...", new PackageResourcesTask(this), 60);
                        if (isStopRequested()) return;

                        executeTask("Optimize apk with aapt2...", new OptimizeTask(this), 70);
                        if (isStopRequested()) return;

                        executeTask("Aligning apk with zip-align...", new AlignTask(this), 80);
                        if (isStopRequested()) return;

                        executeTask("Apk signing...", new SignTask(this), 90);
                        
                        if (!isStopRequested() && buildListener != null) {
                            buildListener.onBuildCompleted(true, "Build completed successfully");
                        }
                    });
        } catch (Exception err) {
            err.printStackTrace(taskManager.getPrintStream());
            if (buildListener != null) {
                buildListener.onBuildError(err.getMessage());
                buildListener.onBuildCompleted(false, "Build failed: " + err.getMessage());
            }
        }

        taskManager.printStatistics();
    }

    private void executeTask(String name, TaskManager.Task task, int progress) throws Exception {
        if (buildListener != null) {
            buildListener.onBuildProgress(name, progress);
        }
        taskManager.task(name, task);
    }

    private void validateBuildEnvironment() throws Exception {
        validateDirectory(config.buildPath, "Build directory");
        validateDirectory(config.resDir, "Resources directory");
        if (config.assetsDir != null) validateDirectory(config.assetsDir, "Assets directory");
        if (config.nativeLibsDir != null)
            validateDirectory(config.nativeLibsDir, "Native libs directory");
        validateFile(new File(config.androidJarPath), "android.jar");
        validateFile(new File(config.manifestPath), "AndroidManifest.xml");

        if (config.javaSources.isEmpty()) {
            throw new BuildException("No Java sources specified");
        }
        for (String sourcePath : config.javaSources) {
            validateDirectory(sourcePath, "Java source directory");
        }

        createDirectory(classesDir, "Classes directory");
        BinaryUtils.setExecutable(aapt2Binary);
    }

    private void validateDirectory(String path, String description) throws Exception {
        if (path == null) throw new BuildException(description + " path is null");
        File dir = new File(path);
        if (!dir.exists()) throw new BuildException(description + " does not exist: " + path);
        if (!dir.isDirectory())
            throw new BuildException(description + " is not a directory: " + path);
    }

    private void validateFile(File file, String description) throws Exception {
        if (!file.exists())
            throw new BuildException(description + " not found at: " + file.getAbsolutePath());
    }

    private void createDirectory(File dir, String description) throws Exception {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BuildException(
                    "Failed to create " + description + ": " + dir.getAbsolutePath());
        }
    }

    public static class BuildConfig {
        public String androidJarPath;
        public String buildPath;
        public String manifestPath;
        public String resDir;
        public String appPackage = "com.example.app";
        public String packageId = "0x7f";

        public String versionName = "1.0";
        public String versionCode = "1";
        public String minSdk = "21";
        public String targetSdk = "33";
        public String javaVersion = "17";

        public String assetsDir;
        public String nativeLibsDir;
        public String desugarJdkLibsPath;
        public String proguardRulesPath;

        public boolean debugMode = true;
        public boolean r8enabled = false;
        public boolean apkAlignEnable = true;
        public boolean apkSignEnable = true;
        public boolean aapt2OptimizeEnable = true;

        public final JavacOptionsBuilder java = JavacOptionsBuilder.create();
        public final List<String> javaSources = new ArrayList<>();
        public final KeyConfig keyConfig = new KeyConfig();

        public static class KeyConfig {
            public boolean useKeystore = false;
            public Keystore keystore;
            public KeyWithCert keyWithCert;

            public KeyConfig() {
                this.keystore = new Keystore();
                this.keyWithCert = new KeyWithCert();
            }

            public static class Keystore {
                public String path;
                public String alias;
                public String storePassword;
                public String keyPassword;
            }

            public static class KeyWithCert {
                public String keyPath;
                public String certPath;
            }
        }
    }
}