package org.xedox.mpde.editor;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.xedox.utils.BaseFragment;

public class EditorStateAdapter extends FragmentStateAdapter {
    
    private final List<BaseFragment> fragments = new ArrayList<>();
    private OnChangeListener onChangeListener;

    public interface OnChangeListener {
        void onFragmentCountChanged(int count);
    }

    public EditorStateAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public BaseFragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    public void addFragment(@NonNull BaseFragment fragment) {
        fragments.add(fragment);
        notifyItemInserted(fragments.size() - 1);
        notifyChange();
    }

    public void removeFragment(int position) {
        fragments.remove(position);
        notifyItemRemoved(position);
        notifyChange();
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseFragment> T getFragment(int position) {
        return (T) fragments.get(position);
    }

    public void clear() {
        int size = fragments.size();
        fragments.clear();
        notifyItemRangeRemoved(0, size);
        notifyChange();
    }

    public void closeFile(int position) {
        BaseFragment fragment = fragments.get(position);
        if (fragment instanceof FileFragment) {
            FileFragment fileFragment = (FileFragment) fragment;
            if (fileFragment.hasUnsavedChanges()) {
                fileFragment.save();
            }
            removeFragment(position);
        }
    }

    public void closeOtherFiles(int keepPosition) {
        for (int i = getItemCount() - 1; i >= 0; i--) {
            if (i == keepPosition) continue;
            if (getFragment(i) instanceof FileFragment) {
                FileFragment fileFragment = getFragment(i);
                if (fileFragment.hasUnsavedChanges()) {
                    fileFragment.save();
                }
            }
            removeFragment(i);
        }
    }

    public void closeAllFiles() {
        for (BaseFragment fragment : fragments) {
            if (fragment instanceof FileFragment) {
                FileFragment fileFragment = (FileFragment) fragment;
                if (fileFragment.hasUnsavedChanges()) {
                    fileFragment.save();
                }
            }
        }
        int size = fragments.size();
        fragments.clear();
        notifyItemRangeRemoved(0, size);
        notifyChange();
    }

    public void saveAll() {
        for (int i = 0; i < getItemCount(); i++) {
            if (getFragment(i) instanceof FileFragment) {
                FileFragment fileFragment = getFragment(i);
                if (fileFragment.hasUnsavedChanges()) {
                    fileFragment.save();
                }
            }
        }
    }

    public int indexOf(BaseFragment fragment) {
        return fragments.indexOf(fragment);
    }

    public boolean containsFile(@NonNull File file) {
        for (BaseFragment fragment : fragments) {
            if (fragment instanceof FileFragment) {
                FileFragment fileFragment = (FileFragment) fragment;
                if (file.equals(fileFragment.getFile())) {
                    return true;
                }
            }
        }
        return false;
    }

    public int findFragmentPosition(@NonNull File file) {
        for (int i = 0; i < fragments.size(); i++) {
            BaseFragment fragment = fragments.get(i);
            if (fragment instanceof FileFragment) {
                FileFragment fileFragment = (FileFragment) fragment;
                if (file.equals(fileFragment.getFile())) {
                    return i;
                }
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
