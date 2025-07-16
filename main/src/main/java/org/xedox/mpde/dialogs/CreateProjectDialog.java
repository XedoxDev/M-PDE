package org.xedox.mpde.dialogs;

import android.content.Context;
import org.xedox.filetree.widget.FileTreeView;
import org.xedox.filetree.utils.Node;
import android.widget.TextView;
import org.xedox.mpde.project.Project;
import org.xedox.utils.DialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import org.xedox.utils.ErrorDialog;
import android.view.View;
import org.xedox.mpde.R;

public class CreateProjectDialog {
    public static void show(Context context, FileTreeView fileTree, Node node) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.create_project);
        builder.setView(R.layout.dialog_input_layout);
        builder.setCancelable(false);

        TextInputEditText input = builder.findViewById(R.id.input);
        TextView error = builder.findViewById(R.id.error);

        input.setHint(R.string.file_name);
        input.requestFocus();
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(
                R.string.rename,
                (dialog, which) -> {
                    String text = input.getText().toString();
                    if (text.isBlank()) {
                        showError(error, R.string.name_cannot_be_empty);
                        return;
                    }
                    File newFile = new File(node.fullPath, text);
                    if (newFile.exists()) {
                        showError(
                                error,
                                        R.string.folder_already_exists);
                        return;
                    }
                    try {
                        Project.create(context, node.fullPath, text);
                        fileTree.addNode(node, new Node(newFile.getAbsolutePath()));
                        dialog.dismiss();
                    } catch (Exception err) {
                        err.printStackTrace();
                        String errText = "Failed to create project";
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
