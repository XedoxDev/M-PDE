package org.xedox.mpde.editor;

import android.content.Context;
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
        viewPager.setOffscreenPageLimit(1);
        viewPager.setUserInputEnabled(false);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, this::setupTab);
        tabLayoutMediator.attach();
    }

    private void setupTab(TabLayout.Tab tab, int position) {
        FileFragment fragment = editorAdapter.getFragment(position);
        tab.setText(fragment.getFile().getName());
        fragment.setOnEditorTextChangeListener((e, f) -> {
            tab.setText(fragment.getTitle());
        });
        tab.view.setOnClickListener(v -> showTabContextMenu(v, position));
    }

    private void showTabContextMenu(View anchor, int position) {

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
                closeFile(position);
                return true;
            } else if (id == R.id.close_other) {
                closeOtherFiles(position);
                return true;
            } else if (id == R.id.close_all) {
                closeAllFiles();
                return true;
            } 
        } catch (Exception e) {
            ErrorDialog.show(context, "Error handling tab action", e);
        }

        return false;
    }

    public void openFile(File file) {
        if (!editorAdapter.containsFile(file)) {
            try {
                FileFragment fragment = FileFragment.newInstance(file);
                editorAdapter.addFragment(fragment);
                viewPager.setCurrentItem(editorAdapter.getItemCount() - 1, false);
            } catch (Exception e) {
                ErrorDialog.show(context, "Failed to open file", e);
            }
        } else {
            int position = editorAdapter.findFragmentPosition(file);
            viewPager.setCurrentItem(position, false);
        }
    }

    public void closeFile(int position) {
        FileFragment fragment = editorAdapter.getFragment(position);
        if (fragment.hasUnsavedChanges()) {
            showSaveDialog(position);
        } else {
            editorAdapter.removeFragment(position);
        }
    }

    private void showSaveDialog(int position) {
        new DialogBuilder(context)
                .setTitle(R.string.unsaved_changes)
                .setMessage(
                        context.getString(R.string.do_you_want_to_save_changes_to)
                                + editorAdapter.getFragment(position).getFile().getName()
                                + "?")
                .setPositiveButton(
                        R.string.save,
                        (d, w) -> {
                            editorAdapter.getFragment(position).save();
                            editorAdapter.removeFragment(position);
                            d.dismiss();
                        })
                .setNegativeButton(
                        R.string.dont_save,
                        (d, w) -> {
                            editorAdapter.removeFragment(position);
                            d.dismiss();
                        })
                .setNeutralButton(R.string.cancel, (d, w) -> d.dismiss())
                .show();
    }

    public void closeOtherFiles(int currentPosition) {
        for (int i = editorAdapter.getItemCount() - 1; i >= 0; i--) {
            if (i != currentPosition) {
                closeFile(i);
            }
        }
    }

    public void closeAllFiles() {
        for (int i = editorAdapter.getItemCount() - 1; i >= 0; i--) {
            closeFile(i);
        }
    }

    public void saveFile(int position) {
        editorAdapter.getFragment(position).save();
    }

    public void saveAllFiles() {
        editorAdapter.saveAll();
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
            if (editorAdapter.getFragment(i).hasUnsavedChanges()) {
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
                            saveAllFiles();
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
