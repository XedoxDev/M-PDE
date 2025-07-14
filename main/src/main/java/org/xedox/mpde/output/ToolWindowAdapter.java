package org.xedox.mpde.output;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.ArrayList;
import java.util.List;
import org.xedox.utils.BaseFragment;

public class ToolWindowAdapter extends FragmentStateAdapter {

    private final List<BaseFragment> fragments = new ArrayList<>();

    public ToolWindowAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public ToolWindowAdapter(
            @NonNull FragmentActivity fragmentActivity, @NonNull List<BaseFragment> fragments) {
        this(fragmentActivity);
        this.fragments.addAll(fragments);
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

    public void add(@NonNull BaseFragment fragment) {
        fragments.add(fragment);
        notifyItemInserted(fragments.size() - 1);
    }

    public void add(int position, @NonNull BaseFragment fragment) {
        fragments.add(position, fragment);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        fragments.remove(position);
        notifyItemRemoved(position);
    }

    public void remove(@NonNull BaseFragment fragment) {
        int position = fragments.indexOf(fragment);
        if (position != -1) {
            fragments.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void set(int position, @NonNull BaseFragment newFragment) {
        fragments.set(position, newFragment);
        notifyItemChanged(position);
    }

    public void clear() {
        int size = fragments.size();
        fragments.clear();
        notifyItemRangeRemoved(0, size);
    }

    @NonNull
    public BaseFragment get(int position) {
        return fragments.get(position);
    }

    public int indexOf(@NonNull BaseFragment fragment) {
        return fragments.indexOf(fragment);
    }

    @NonNull
    public List<BaseFragment> getAll() {
        return new ArrayList<>(fragments);
    }

    public void setAll(@NonNull List<BaseFragment> fragments) {
        this.fragments.clear();
        this.fragments.addAll(fragments);
        notifyDataSetChanged();
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseFragment> T getFragmentByName(String name) {
        for (BaseFragment fragment : fragments) {
            if (fragment.getTitle() == name) {
                return (T) fragment;
            }
        }
        return null;
    }
}
