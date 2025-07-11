package org.xedox.mpde;

import android.app.Application;
import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.xedox.mpde.editor.SoraEditorManager;
import org.xedox.utils.DialogBuilder;
import org.xedox.utils.ErrorDialog;
import org.xedox.utils.io.IFile;
import org.xedox.utils.io.FileX;

public class AppCore extends Application {
    
    private static IFile HOME;
    private static IFile TEXTMATE;
    private static IFile PROJECTS;
    private static AppCore appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        HOME = new FileX(getExternalFilesDir(null));
        TEXTMATE = new FileX(HOME, "textmate");
        PROJECTS = new FileX(HOME, "projects");
        TEXTMATE.mkdirs();
        PROJECTS.mkdirs();
        DialogBuilder.builderType = MaterialAlertDialogBuilder.class;
    }
    
    public static void initFromActivity(EditorActivity activity) {
        try {
        	SoraEditorManager.init(activity);
        } catch(Exception err) {
        	err.printStackTrace();
            ErrorDialog.show(activity, "Failed to init SoraEditor", err);
        }
    }

    public static IFile homeDir() {
        return HOME;
    }
    
    public static IFile textmateDir() {
        return TEXTMATE;
    }
    
    public static IFile projectsDir() {
        return PROJECTS;
    }
    
    public static IFile pluginsDir() {
    	return PLUGINS;
    }

    public static String string(int res) {
        return appContext.getString(res);
    }

    public static boolean isDarkMode(Context context) {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
    }
}
