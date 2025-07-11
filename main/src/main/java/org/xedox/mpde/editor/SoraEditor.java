package org.xedox.mpde.editor;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.component.EditorTextActionWindow;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import java.util.Set;
import java.util.HashSet;
import org.xedox.mpde.R;

public class SoraEditor extends CodeEditor {

    public SoraEditor(Context context) {
        super(context);
        init();
    }

    public SoraEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SoraEditor(Context context, AttributeSet attrs, int defstyle) {
        super(context, attrs, defstyle);
        init();
    }

    protected void init() {
        Typeface typeface =
                Typeface.createFromAsset(getContext().getAssets(), "JetBrainsMono-Bold.ttf");
        setTypefaceText(typeface);
        setTypefaceLineNumber(typeface);
        EditorTextActionWindow actionWindow = getComponent(EditorTextActionWindow.class);
        if (actionWindow != null) {
            ViewGroup view = actionWindow.getView();
            if (view != null) {
                view.setBackground(
                        getContext().getDrawable(R.drawable.sora_editor_action_window_background));
            }
        }
    }

    public void append(CharSequence txt) {
        if (txt == null || txt.length() == 0) return;
        Content content = getText();
        content.insert(
                getText().getLineCount() - 1,
                getText().getColumnCount(getText().getLineCount() - 1),
                txt);
    }

    public String getStringText() {
        return getText().toString();
    }
}
