package org.xedox.mpde.editor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.Event;
import io.github.rosemoe.sora.event.SubscriptionReceipt;
import io.github.rosemoe.sora.event.Unsubscribe;
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.text.Content;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.xedox.utils.ErrorDialog;
import org.xedox.utils.io.FileX;
import org.xedox.utils.io.IFile;
import org.xedox.utils.BaseFragment;

public class FileFragment extends BaseFragment {
    
    private static final Map<String, String> EXTENSION_MAPPING = new HashMap<>();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private File file;
    private SoraEditor editor;
    private String originalText;
    private String title;
    private SubscriptionReceipt<ContentChangeEvent> contentChangeSubscriber;
    private boolean isModified = false;
    private OnEditorTextChangeListener onEditorTextChangeListener;

    public static interface OnEditorTextChangeListener {
        void onChange(SoraEditor editor, File file);
    }

    static {
        String[] scopes = SoraEditorManager.getLangSourcesList();
        for (String item : scopes) {
            String extension = item.substring(item.lastIndexOf("."));
            EXTENSION_MAPPING.put(extension, "source" + extension);
        }
    }

    public static FileFragment newInstance(File file) {
        FileFragment fragment = new FileFragment();
        fragment.setFile(file);
        return fragment;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        editor = new SoraEditor(requireActivity());
        loadFileContent();

        String extension = FileX.getExtension(file);
        String scope = EXTENSION_MAPPING.getOrDefault(extension, "source.txt");
        editor.setEditorLanguage(new TML(requireActivity(), scope));

        try {
            editor.setColorScheme(TextMateColorScheme.create(ThemeRegistry.getInstance()));
        } catch (Exception err) {
            ErrorDialog.show(requireActivity(), "Failed to load color scheme", err);
        }

        contentChangeSubscriber =
                editor.subscribeEvent(
                        ContentChangeEvent.class,
                        (event, unsubscribe) -> {
                            isModified = !originalText.equals(editor.getText().toString());
                            updateTabTitle();
                            if (onEditorTextChangeListener != null)
                                onEditorTextChangeListener.onChange(editor, file);
                        });

        return editor;
    }

    private void loadFileContent() {
        try {
            IFile ifile = new FileX(file);
            originalText = ifile.read();
            editor.setText(originalText);
            isModified = false;
            updateTabTitle();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load file: " + file.getName(), e);
        }
    }

    public void save() {
        if (editor == null) {
            if (originalText != null && file != null) {
                saveToFile(originalText);
            }
            return;
        }

        String newText = editor.getText().toString();
        saveToFile(newText);
        originalText = newText;
        isModified = false;
        updateTabTitle();
    }

    private void saveToFile(String content) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException err) {
            throw new RuntimeException("Failed to save file: " + file.getName(), err);
        }
    }

    public void updateTabTitle() {
        title = file.getName() + (isModified ? "*" : "");
        TabLayout.Tab tab = getTab();
        if (tab != null) {
            tab.setText(title);
        }
    }

    public boolean hasUnsavedChanges() {
        return isModified;
    }

    @Override
    public void onDestroyView() {
        if (contentChangeSubscriber != null) {
            contentChangeSubscriber.unsubscribe();
        }
        if (editor != null) {
            editor.release();
        }
        super.onDestroyView();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        this.title = file.getName();
    }

    public void setText(CharSequence text) {
        handler.post(
                () -> {
                    if (editor != null) {
                        editor.setText(text);
                        isModified = !originalText.equals(text.toString());
                        updateTabTitle();
                    }
                });
    }

    public String getText() {
        return editor != null
                ? editor.getText().toString()
                : file != null ? new FileX(file).read() : "";
    }

    public OnEditorTextChangeListener getOnEditorTextChangeListener() {
        return this.onEditorTextChangeListener;
    }

    public void setOnEditorTextChangeListener(
            OnEditorTextChangeListener onEditorTextChangeListener) {
        this.onEditorTextChangeListener = onEditorTextChangeListener;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
