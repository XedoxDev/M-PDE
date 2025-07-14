package org.xedox.mpde.output;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.View;
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import java.io.PrintStream;
import org.xedox.mpde.R;
import org.xedox.mpde.editor.SoraEditor;
import org.xedox.mpde.editor.TML;
import org.xedox.utils.BaseFragment;
import org.xedox.utils.ErrorDialog;

public class BuildOutputFragment extends BaseFragment {

    private SoraEditor output;
    private String title = "null";
    private SoraEditor.PrintStream printStream;

    public static BuildOutputFragment newInstance(String title) {
        BuildOutputFragment fragment = new BuildOutputFragment();
        fragment.title = title;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewgroup, Bundle extraArgs) {
        output = new SoraEditor(getActivity());
        printStream = new SoraEditor.PrintStream(output);
        output.setSoftKeyboardEnabled(false);
        try {
            output.setColorScheme(TextMateColorScheme.create(ThemeRegistry.getInstance()));
        } catch (Exception err) {
            ErrorDialog.show(getActivity(), "Failed to load theme to build output fragment", err);
        }
        output.setEditorLanguage(new TML(getActivity(), "source.log"));
        return output;
    }

    @Override
    public String getTitle() {
        return title != null ? title : super.getTitle();
    }

    public PrintStream getPrintStream() {
        return this.printStream;
    }
}
