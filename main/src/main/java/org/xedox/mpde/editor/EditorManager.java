package org.xedox.mpde.editor;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.View;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.io.File;
import org.xedox.mpde.EditorActivity;
import org.xedox.mpde.R;
import org.xedox.mpde.drawer.DrawerManager;
import org.xedox.utils.BaseFragment;
import org.xedox.utils.DialogBuilder;
import org.xedox.utils.ErrorDialog;
import org.xedox.utils.OverflowMenu;

public class EditorManager {
    
    private final EditorActivity context;
    private final TabLayout tabLayout;
    private final ViewPager2 viewPager;
    private final EditorStateAdapter editorAdapter;
    private final TabLayoutMediator tabLayoutMediator;
    private final View emptyEditorView;

    public EditorManager(EditorActivity context) {
        this.context = context;
        this.tabLayout = context.getEditorTabLayout();
        this.viewPager = context.getEditorPager();
        this.emptyEditorView = context.getEmptyEditorView();

        editorAdapter = new EditorStateAdapter(context);
        editorAdapter.setOnChangeListener(count -> updateUIState(count > 0));
        viewPager.setAdapter(editorAdapter);
        viewPager.setUserInputEnabled(false);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, this::setupTab);
        tabLayoutMediator.attach();
    }

    private void setupTab(TabLayout.Tab tab, int position) {
        FileFragment fragment = editorAdapter.getFragment(position);
        tab.setText(fragment.getFile().getName());
        tab.setIcon(fragment.getIcon());
        fragment.setOnEditorTextChangeListener((e, f) -> {
            tab.setText(fragment.getTitle());
        });
        tab.view.setOnClickListener(v -> showTabContextMenu(v, position));
    }

    private void showTabContextMenu(View anchor, int position) {
        if(viewPager.getCurrentItem() == position) return;
        OverflowMenu.show(
                context,
                anchor,
                R.menu.tab,
                (item) -> {
                    handleTabMenuItemClick(item, position);
                });
    }

    private boolean handleTabMenuItemClick(MenuItem item, int position) {
        int id = item.getItemId();

        try {
            if (id == R.id.close_it) {
                editorAdapter.closeFile(position);
                return true;
            } else if (id == R.id.close_other) {
                editorAdapter.closeOtherFiles(position);
                return true;
            } else if (id == R.id.close_all) {
                editorAdapter.closeAllFiles();
                return true;
            } 
        } catch (Exception e) {
            ErrorDialog.show(context, "Error handling tab action", e);
        }

        return false;
    }

    public void openFile(File file) {
        openFile(file, context.getDrawable(org.xedox.filetree.R.drawable.file));
    }
    
    public void openFile(File file, Drawable icon) {
        if (!editorAdapter.containsFile(file)) {
            try {
                FileFragment fragment = FileFragment.newInstance(file, icon);
                editorAdapter.addFragment(fragment);
                viewPager.setCurrentItem(editorAdapter.getItemCount() - 1);
            } catch (Exception e) {
                ErrorDialog.show(context, "Failed to open file", e);
            }
        } else {
            int position = editorAdapter.findFragmentPosition(file);
            viewPager.setCurrentItem(position);
        }
    }
   
    private void updateUIState(boolean hasFiles) {
        emptyEditorView.setVisibility(hasFiles ? View.GONE : View.VISIBLE);
        viewPager.setVisibility(hasFiles ? View.VISIBLE : View.GONE);
        tabLayout.setVisibility(hasFiles ? View.VISIBLE : View.GONE);
        context.updateItemVisibility(R.id.save, hasFiles);
    }

    public boolean onBackPressed() {
        if (editorAdapter.getItemCount() > 0) {
            if (hasUnsavedChanges()) {
                showUnsavedChangesDialog();
                return true;
            }
        }
        return false;
    }

    private boolean hasUnsavedChanges() {
        for (int i = 0; i < editorAdapter.getItemCount(); i++) {
            FileFragment fileFragment = (FileFragment) editorAdapter.getFragment(i);
            if (fileFragment.hasUnsavedChanges()) {
                return true;
            }
        }
        return false;
    }

    private void showUnsavedChangesDialog() {
        new DialogBuilder(context)
                .setTitle(R.string.unsaved_changes)
                .setMessage(R.string.unsaved_changes_message)
                .setPositiveButton(
                        R.string.save,
                        (d, w) -> {
                            editorAdapter.saveAll();
                            context.finish();
                            d.dismiss();
                        })
                .setNegativeButton(
                        R.string.close_without_save,
                        (d, w) -> {
                            context.finish();
                            d.dismiss();
                        })
                .setNeutralButton(R.string.cancel, (d, w) -> d.dismiss())
                .show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save) {
            editorAdapter.saveAll();
        }
        return false;
    }
}
