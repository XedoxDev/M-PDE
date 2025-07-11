package org.xedox.mpde.editor;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.xedox.utils.ErrorDialog;
import org.xedox.utils.BaseFragment;

public class EditorStateAdapter extends FragmentStateAdapter {
    private final List<FileFragment> fragments = new ArrayList<>();
    private OnChangeListener onChangeListener;

    public interface OnChangeListener {
        void onFragmentCountChanged(int count);
    }

    public EditorStateAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    public void addFragment(FileFragment fragment) {
        fragments.add(fragment);
        notifyItemInserted(fragments.size() - 1);
        notifyChange();
    }

    public void removeFragment(int position) {
        if (position >= 0 && position < fragments.size()) {
            fragments.remove(position);
            notifyItemRemoved(position);
            notifyDataSetChanged();
            notifyChange();
        }
    }

    public FileFragment getFragment(int position) {
        return fragments.get(position);
    }

    public void clear() {
        fragments.clear();
        notifyDataSetChanged();
        notifyChange();
    }

    public void saveAll() {
        for (FileFragment fragment : fragments) {
            if (fragment.hasUnsavedChanges()) {
                fragment.save();
            }
        }
    }

    public boolean containsFile(File file) {
        for (FileFragment fragment : fragments) {
            if (fragment.getFile().equals(file)) {
                return true;
            }
        }
        return false;
    }

    public int findFragmentPosition(File file) {
        for (int i = 0; i < fragments.size(); i++) {
            if (fragments.get(i).getFile().equals(file)) {
                return i;
            }
        }
        return -1;
    }

    public void setOnChangeListener(OnChangeListener listener) {
        this.onChangeListener = listener;
    }

    private void notifyChange() {
        if (onChangeListener != null) {
            onChangeListener.onFragmentCountChanged(fragments.size());
        }
    }
}