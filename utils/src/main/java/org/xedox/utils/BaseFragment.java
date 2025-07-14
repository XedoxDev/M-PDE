package org.xedox.utils;

import androidx.fragment.app.Fragment;
import com.google.android.material.tabs.TabLayout;

public abstract class BaseFragment extends Fragment {
    
    protected TabLayout.Tab tab;

    public TabLayout.Tab getTab() {
        return this.tab;
    }

    public void setTab(TabLayout.Tab tab) {
        this.tab = tab;
    }

    public String getTitle() {
        return "NULL TITLE";
    }
}
