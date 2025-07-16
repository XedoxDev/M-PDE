package org.xedox.mpde.drawer;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.activity.result.ActivityResultLauncher;
import android.net.Uri;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayoutMediator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.xedox.filetree.adapter.FileTreeAdapter;
import org.xedox.mpde.AppCore;
import org.xedox.mpde.dialogs.CreateProjectDialog;
import org.xedox.mpde.editor.EditorManager;
import org.xedox.mpde.project.Project;
import org.xedox.utils.Disposable;
import org.xedox.utils.ErrorDialog;
import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTreeView;
import org.xedox.mpde.EditorActivity;
import org.xedox.mpde.R;
import org.xedox.mpde.dialogs.NewFileDialog;
import org.xedox.mpde.dialogs.RenameFileDialog;
import org.xedox.utils.OverflowMenu;
import org.xedox.utils.io.FileX;

public class DrawerManager extends BaseDrawerManager {

    protected Handler handler = new Handler(Looper.getMainLooper());

    protected EditorActivity context;
    protected TabLayout tabLayout;
    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle drawerToggle;
    protected ViewPager2 viewPager;
    protected TabLayoutMediator tabLayoutMediator;
    protected DrawerStateAdapter stateAdapter;
    protected FileTreeView fileTree;
    protected SelectListener mSelectListener;
    protected EditorManager editorManager;
    protected NavigationView navigation;

    public DrawerManager(EditorActivity context) {
        this.context = context;
        super.filePickerLauncher =
                context.registerForActivityResult(
                        new ActivityResultContracts.OpenDocument(),
                        uri -> {
                            if (uri != null) {
                                onFileSelected(uri);
                            }
                        });
        try {
            initialize();
        } catch (Throwable err) {
            err.printStackTrace();
            ErrorDialog.show(context, "Failed to init drawer manager", err);
        }
        try {
            initializeFileTree();
        } catch (Throwable err) {
            err.printStackTrace();
            ErrorDialog.show(context, "Failed to init file tree", err);
        }
    }

    private void initialize() {
        stateAdapter = new DrawerStateAdapter(context);
        tabLayout = context.getDrawerTabLayout();
        drawerLayout = context.getDrawerLayout();
        drawerToggle = context.getDrawerToggle();
        viewPager = context.getDrawerPager();
        navigation = context.getNavigationView();
        editorManager = context.getEditorManager();
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        viewPager.setAdapter(stateAdapter);
        tabLayoutMediator =
                new TabLayoutMediator(
                        tabLayout,
                        viewPager,
                        (tab, pos) -> {
                            tab.setText(stateAdapter.get(pos).getTitle());
                        });
        tabLayoutMediator.attach();
        viewPager.setUserInputEnabled(false);
        drawerLayout.setOnTouchListener(
                (v, event) -> {
                    float x = event.getRawX();
                    float y = event.getRawY();

                    if (isTouchView(viewPager, x, y) && drawerLayout.isOpen()) {
                        MotionEvent transformedEvent = transformToViewCoords(viewPager, event);
                        viewPager.dispatchTouchEvent(transformedEvent);
                        transformedEvent.recycle();
                        return true;
                    }
                    return false;
                });
    }

    private boolean isTouchView(View view, float x, float y) {
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        return x >= loc[0]
                && x <= loc[0] + view.getWidth()
                && y >= loc[1]
                && y <= loc[1] + view.getHeight();
    }

    private MotionEvent transformToViewCoords(View view, MotionEvent event) {
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        float viewX = event.getRawX() - loc[0];
        float viewY = event.getRawY() - loc[1];

        MotionEvent transformedEvent = MotionEvent.obtain(event);
        transformedEvent.setLocation(viewX, viewY);
        return transformedEvent;
    }

    private void initializeFileTree() {
        FileTreeFragment treeFragment = FileTreeFragment.newInstance();
        stateAdapter.add(treeFragment);
        handler.post(
                () -> {
                    fileTree = treeFragment.getFileTree();
                    fileTree.childrenLines = true;
                    fileTree.turnOnLines = true;
                    fileTree.setOnFileLongClickListener(
                            (node, file, view) -> {
                                if (!node.isFile && file.getName().equals("projects")) {
                                    OverflowMenu.show(
                                            context,
                                            view,
                                            R.menu.projects_folder,
                                            (item) -> {
                                                int id = item.getItemId();
                                                if (id == R.id.create_project) {
                                                    CreateProjectDialog.show(
                                                            context, fileTree, node);
                                                }
                                                if (id == R.id.clear) {
                                                    FileX.deleteDirectory(AppCore.projectsDir());
                                                    AppCore.projectsDir().mkdirs();
                                                }
                                            });
                                    return;
                                }
                                OverflowMenu.show(
                                        context,
                                        view,
                                        node.isFile ? R.menu.file : R.menu.folder,
                                        (item) -> handleOnFileLongClickMenu(item, node, file));
                            });
                    fileTree.setOnFileClickListener(
                            (node, file, view) -> editorManager.openFile(file, node.icon));
                    initFileTreeIcons();
                    context.loadFileTree(fileTree);
                });
    }

    private void initFileTreeIcons() {
        fileTree.setIcon(".json", R.drawable.json);
        fileTree.setIcon(".java", R.drawable.java);
        fileTree.setIcon(".pde", R.drawable.processing);
        fileTree.setIcon(".properties", R.drawable.file_config);
    }

    protected void handleOnFileLongClickMenu(MenuItem item, Node node, File file) {
        try {
            int id = item.getItemId();
            if (id == R.id.delete) {
                if (node.isFile) {
                    Files.delete(file.toPath());
                } else {
                    try (Stream<Path> walk = Files.walk(file.toPath())) {
                        walk.sorted(Comparator.reverseOrder())
                                .forEach(
                                        path -> {
                                            try {
                                                Files.delete(path);
                                            } catch (IOException e) {
                                                throw new RuntimeException(
                                                        "Failed to delete: " + path, e);
                                            }
                                        });
                    }
                }
                fileTree.deleteNode(node);

            } else if (id == R.id.rename) {
                RenameFileDialog.show(context, fileTree, node);
            }

            if (node.isFile) {
                if (id == R.id.open_in_other_app) {
                    openFileInExternalApp(file);
                }
            } else {
                if (id == R.id.new_file_folder) {
                    NewFileDialog.show(context, fileTree, node);
                } else if (id == R.id.import_file) {
                    importFile(file);
                } else if (id == R.id.open_as_project) {
                    context.openProject(node.fullPath);
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
            ErrorDialog.show(context, "Failed to handle file operation", err);
        }
    }
}
