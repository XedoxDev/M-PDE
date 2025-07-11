package org.xedox.mpde.dialogs;

import android.content.Context;
import com.google.android.material.button.MaterialButtonToggleGroup;
import org.xedox.filetree.widget.FileTreeView;
import org.xedox.filetree.utils.Node;
import android.widget.TextView;
import org.xedox.utils.DialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import org.xedox.utils.ErrorDialog;
import android.view.View;
import org.xedox.mpde.R;

public class NewFileDialog {
    public static void show(Context context, FileTreeView fileTree, Node node) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.create_new_file_folder);
        builder.setView(R.layout.dialog_create_new_file_layout);
        builder.setCancelable(false);

        TextInputEditText input = builder.findViewById(R.id.input);
        TextView error = builder.findViewById(R.id.error);
        MaterialButtonToggleGroup buttonToggleGroup = builder.findViewById(R.id.type);
        input.setHint(R.string.file_name);

        input.requestFocus();
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(
                R.string.create,
                (dialog, which) -> {
                    CreateType createType = getType(buttonToggleGroup);
                    String text = input.getText().toString();
                    if (text.isBlank()) {
                        showError(error, R.string.name_cannot_be_empty);
                        return;
                    }
                    File file = new File(node.fullPath, text);
                    if (file.exists()) {
                        showError(
                                error,
                                createType == CreateType.FILE
                                        ? R.string.file_already_exists
                                        : R.string.folder_already_exists);
                        return;
                    }
                    try {
                        if (createType == CreateType.FILE) {
                            file.createNewFile();
                        } else {
                            file.mkdir();
                        }
                        if(node.isOpen)
                            fileTree.addNode(node, new Node(file.getAbsolutePath()));
                        dialog.dismiss();
                    } catch (Exception err) {
                        err.printStackTrace();
                        String errTemp = "Failed to create %s %s";
                        String errText =
                                String.format(
                                        errTemp,
                                        createType == CreateType.FILE
                                                ? context.getString(R.string.file)
                                                : context.getString(R.string.folder),
                                        file.getName());
                        ErrorDialog.show(context, errText, err);
                    }
                });
                builder.create().show();
    }

    private static CreateType getType(MaterialButtonToggleGroup buttonToggleGroup) {
        if (buttonToggleGroup.getCheckedButtonId() == R.id.file_type) {
            return CreateType.FILE;
        } else if (buttonToggleGroup.getCheckedButtonId() == R.id.folder_type) {
            return CreateType.FOLDER;
        }
        return CreateType.FILE;
    }

    private static void showError(TextView view, int textId) {
        view.setVisibility(View.VISIBLE);
        view.setText(textId);
    }

    private enum CreateType {
        FOLDER,
        FILE
    }
}
