package org.xedox.mpde.drawer;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.xedox.filetree.widget.FileTreeView;
import org.xedox.mpde.AppCore;
import org.xedox.mpde.R;
import org.xedox.utils.BaseFragment;

public class FileTreeFragment extends BaseFragment {

    private FileTreeView fileTree;

    public static FileTreeFragment newInstance(@Nullable String path) {
        FileTreeFragment fragment = new FileTreeFragment();
        Bundle args = new Bundle();
        args.putString("path", path != null ? path : AppCore.homeDir().getFullPath());
        fragment.setArguments(args);
        return fragment;
    }

    public static FileTreeFragment newInstance() {
        return newInstance(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceState) {
        if (getActivity() == null) return null;
        View view = inflater.inflate(R.layout.drawer_filetree_layout, vg, false);
        fileTree = view.findViewById(R.id.filetree);
        Bundle args = getArguments();
        String path = args.getString("path");
        fileTree.loadPath(path);

        return view;
    }

    public FileTreeView getFileTree() {
        return fileTree;
    }

    public String getTitle() {
        return AppCore.string(R.string.file_tree);
    }
}
