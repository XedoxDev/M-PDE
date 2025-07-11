package org.xedox.mpde;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.xedox.filetree.widget.FileTreeView;
import org.xedox.mpde.drawer.DrawerManager;
import org.xedox.mpde.editor.EditorManager;
import org.xedox.mpde.project.Project;
import org.xedox.utils.io.FileX;

public class EditorActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private TabLayout editorTabLayout, drawerTabLayout;
    private ViewPager2 editorPager, drawerPager;
    private FileTreeView fileTree;
    private View emptyEditorView;
    private NavigationView navigationView;

    private DrawerManager drawerManager;
    private EditorManager editorManager;

    private final Map<Integer, Boolean> menuItemsVisibility = new HashMap<>();
    private final Map<Integer, Drawable> menuItemsIcon = new HashMap<>();
    
    private Project project = null;
    private String buildType = "Preview";
    private int itemBuild = R.id.preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        AppCore.initFromActivity(this);
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        editorTabLayout = findViewById(R.id.editor_tab_layout);
        drawerTabLayout = findViewById(R.id.drawer_tab_layout);
        editorPager = findViewById(R.id.editor_pager);
        drawerPager = findViewById(R.id.drawer_pager);
        emptyEditorView = findViewById(R.id.editor_no_files);
        navigationView = findViewById(R.id.navigation_view);
        setSupportActionBar(toolbar);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        editorManager = new EditorManager(this);
        drawerManager = new DrawerManager(this);

        openProject(null);
    }
    
    public void loadFileTree(FileTreeView treeView) {
    	this.fileTree = treeView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor, menu);
        for (Map.Entry<Integer, Boolean> entry : menuItemsVisibility.entrySet()) {
            MenuItem item = menu.findItem(entry.getKey());
            if (item != null) {
                item.setVisible(entry.getValue());
            }
        }
        for (Map.Entry<Integer, Drawable> entry : menuItemsIcon.entrySet()) {
            MenuItem item = menu.findItem(entry.getKey());
            if (item != null) {
                item.setIcon(entry.getValue());
            }
        }
        MenuItem item = menu.findItem(itemBuild);
        item.setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (editorManager.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        
        if(id == R.id.preview) {
            buildType = "Preview";
            itemBuild = R.id.preview;
            updateItemIcon(R.id.run_type, item.getIcon());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null && menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                Method m =
                        menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(menu, true);
            } catch (Exception e) {
                Log.e("OverflowMenu", "Error forcing menu icons to show", e);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public void updateItemVisibility(int itemId, boolean visible) {
        menuItemsVisibility.put(itemId, visible);
        invalidateOptionsMenu();
    }
    
    public void updateItemIcon(int itemId, Drawable drawable) {
        menuItemsIcon.put(itemId, drawable);
        invalidateOptionsMenu();
    }

    public DrawerLayout getDrawerLayout() {
        return this.drawerLayout;
    }

    public TabLayout getEditorTabLayout() {
        return this.editorTabLayout;
    }

    public TabLayout getDrawerTabLayout() {
        return this.drawerTabLayout;
    }

    public ViewPager2 getEditorPager() {
        return this.editorPager;
    }

    public ViewPager2 getDrawerPager() {
        return this.drawerPager;
    }

    public DrawerManager getDrawerManager() {
        return this.drawerManager;
    }

    public EditorManager getEditorManager() {
        return this.editorManager;
    }

    public View getEmptyEditorView() {
        return this.emptyEditorView;
    }

    public NavigationView getNavigationView() {
        return navigationView;
    }

    public ActionBarDrawerToggle getDrawerToggle() {
        return this.drawerToggle;
    }

    public void openProject(String path) {
        boolean nonNull = path != null;
        updateItemVisibility(R.id.run_type, nonNull);
        updateItemVisibility(R.id.run, nonNull);
        if (path == null) return;
        project = new Project(new FileX(path));
        fileTree.loadPath(path);
        getSupportActionBar().setSubtitle(project.path.getName());
        updateItemVisibility(R.id.run_type, true);
    }
}
