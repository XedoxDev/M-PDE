package org.xedox.mpde.drawer;

import android.view.MenuItem;
import androidx.activity.result.ActivityResultLauncher;
import android.net.Uri;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.IOException;
import android.database.Cursor;
import android.provider.OpenableColumns;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import androidx.core.content.FileProvider;
import android.content.Intent;
import android.webkit.MimeTypeMap;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTreeView;
import org.xedox.mpde.AppCore;
import org.xedox.mpde.EditorActivity;
import org.xedox.mpde.R;
import org.xedox.mpde.dialogs.NewFileDialog;
import org.xedox.mpde.dialogs.RenameFileDialog;
import org.xedox.utils.Disposable;
import org.xedox.utils.ErrorDialog;
import org.xedox.utils.OverflowMenu;

public abstract class BaseDrawerManager implements Disposable {

    protected EditorActivity context;
    protected TabLayout tabLayout;
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle drawerToggle;
    protected ViewPager2 viewPager;
    protected TabLayoutMediator tabLayoutMediator;
    protected DrawerStateAdapter stateAdapter;
    protected FileTreeView fileTree;
    protected SelectListener mSelectListener;
    protected ActivityResultLauncher<String[]> filePickerLauncher;

    @Override
    public void dispose() {
        tabLayoutMediator.detach();
    }

    protected void importFile(File folder) {
        showFilePicker(
                new SelectListener() {
                    @Override
                    public void onSelect(Object... options) {
                        try {
                            Uri uri = (Uri) options[0];
                            String fileName = (String) options[1];
                            String content = (String) options[2];
                            File destFile = new File(folder, fileName);
                            try (OutputStream out = new FileOutputStream(destFile)) {
                                out.write(content.getBytes());
                            }
                            fileTree.refresh();
                        } catch (Exception e) {
                            ErrorDialog.show(context, "Failed to import file", e);
                        }
                    }
                });
    }

    public void showFilePicker(SelectListener listener) {
        filePickerLauncher.launch(new String[] {"*/*"});
        mSelectListener = listener;
    }

    protected void onFileSelected(Uri uri) {
        if (mSelectListener != null) {
            try {
                String fileName = getFileName(uri);
                String fileContent = readFileContent(uri);
                mSelectListener.onSelect(uri, fileName, fileContent);
            } catch (Exception e) {
                mSelectListener.onSelect();
                ErrorDialog.show(context, "Error reading file", e);
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String readFileContent(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public interface SelectListener {
        void onSelect(Object... options);
    }

    public void openFileInExternalApp(File file) {
        try {
            String authority = context.getPackageName() + ".provider";
            Uri uri = FileProvider.getUriForFile(context, authority, file);

            String mime = getMimeType(file.getPath());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getMimeType(String url) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(url);
        if (ext == null) {
            return "*/*";
        }
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getMimeTypeFromExtension(ext.toLowerCase());
    }
}
