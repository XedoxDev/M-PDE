package org.xedox.mpde.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import org.xedox.filetree.utils.Node;
import org.xedox.filetree.widget.FileTreeView;
import org.xedox.mpde.R;
import org.xedox.utils.DialogBuilder;
import org.xedox.utils.ErrorDialog;

public class RenameFileDialog {
    public static void show(Context context, FileTreeView fileTree, Node node) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.rename_file_folder);
        builder.setView(R.layout.dialog_input_layout);
        builder.setCancelable(false);

        TextInputEditText input = builder.findViewById(R.id.input);
        TextView error = builder.findViewById(R.id.error);

        input.setHint(R.string.file_name);
        input.requestFocus();
        File file = new File(node.fullPath);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(
                R.string.rename,
                (dialog, which) -> {
                    String text = input.getText().toString();
                    if (text.isBlank()) {
                        showError(error, R.string.name_cannot_be_empty);
                        return;
                    }
                    File newFile = new File(file.getParent(), text);
                    if (newFile.exists()) {
                        showError(
                                error,
                                node.isFile
                                        ? R.string.file_already_exists
                                        : R.string.folder_already_exists);
                        return;
                    }
                    try {
                        file.renameTo(newFile);
                        fileTree.renameNode(node, newFile.getName());
                        dialog.dismiss();
                    } catch (Exception err) {
                        err.printStackTrace();
                        String errTemp = "Failed to rename file (%s -> %s)";
                        String errText = String.format(errTemp, file.getName(), newFile.getName());
                        ErrorDialog.show(context, errText, err);
                    }
                });
        builder.create().show();
    }

    private static void showError(TextView view, int textId) {
        view.setVisibility(View.VISIBLE);
        view.setText(textId);
    }
}
