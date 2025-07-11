package org.xedox.mpde.drawer;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.ArrayList;
import java.util.List;
import org.xedox.mpde.AppCore;
import org.xedox.utils.BaseFragment;

public class DrawerStateAdapter extends FragmentStateAdapter {

    private List<BaseFragment> fragments;

    public DrawerStateAdapter(FragmentActivity activity) {
        super(activity);
        fragments = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    public void clear() {
        fragments.clear();
        notifyDataSetChanged();
    }

    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    public void add(BaseFragment fragment) {
        fragments.add(fragment);
        notifyItemInserted(fragments.size() - 1);
    }

    public BaseFragment get(int position) {
        return fragments.get(position);
    }

    public FileTreeFragment getFileTreeFragment() {
        for (BaseFragment fragment : fragments) {
            if (fragment instanceof FileTreeFragment) return (FileTreeFragment) fragment;
        }
        return null;
    }
}
