package org.xedox.mpde.build;

import android.content.Context;
import java.io.PrintStream;
//import org.xedox.apkbuilder.ApkBuilder;
import org.xedox.mpde.project.Project;

public class BuildTask {

    //private ApkBuilder apkBuilder;
    private Context context;
    private PrintStream out;

    public BuildTask(Context context, PrintStream out) {
        this.context = context;
        this.out = out;
        //apkBuilder = new ApkBuilder(context, out);
    }
//
//    public void startBuild(Project project) {
//        new Thread(() -> build(project)).start();
//    }
//
//    private final void build(Project project) {
//        apkBuilder.build(project.getBuildConfig());
//    }
//
//    public void stopBuild() {
//        apkBuilder.stopBuild();
//    }
}
