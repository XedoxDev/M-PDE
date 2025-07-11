package org.xedox.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public final class ErrorDialog {

    private ErrorDialog() {}

    public static void show(Context context, String message, Throwable error) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle("Runtime error");
        builder.setView(R.layout.dialog_error_layout);
        builder.setPositiveButton("OK", (d, w) -> d.dismiss());
        builder.setCancelable(false);

        View dialogView = builder.getView();
        if (dialogView == null) {
            throw new IllegalStateException("Error dialog layout not found");
        }

        TextView tvMessage = dialogView.findViewById(R.id.error_message);
        TextView tvStackTrace = dialogView.findViewById(R.id.stack_trace);
        builder.setNegativeButton(
                "Copy",
                (d, w) -> {
                    ClipboardManager clipboard =
                            (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData data = ClipData.newPlainText("Runtime error", tvStackTrace.getText());
                    clipboard.setPrimaryClip(data);
                });
        if (tvMessage == null || tvStackTrace == null) {
            throw new IllegalStateException("Error dialog views not found");
        }

        tvMessage.setText(getErrorMessage(message, error));
        tvStackTrace.setText(getStackTrace(error));
        builder.show();
    }

    public static void show(Context context, String message) {
        show(context, message, null);
    }

    public static void show(Context context, Throwable error) {
        show(context, null, error);
    }

    private static String getErrorMessage(String message, Throwable error) {
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }
        if (error != null && error.getMessage() != null) {
            return error.getMessage();
        }
        return "Unknown error";
    }

    private static String getStackTrace(Throwable error) {
        if (error == null) {
            return "StackTrace not available";
        }

        StringBuilder sb = new StringBuilder(512); // Initial capacity for stack traces
        sb.append(error.getClass().getSimpleName());

        if (error.getMessage() != null) {
            sb.append(": ").append(error.getMessage());
        }
        sb.append("\n\n");

        for (StackTraceElement element : error.getStackTrace()) {
            sb.append("at ").append(element.toString()).append("\n");
        }

        // Add cause if exists
        Throwable cause = error.getCause();
        if (cause != null) {
            sb.append("\nCaused by: ");
            sb.append(getStackTrace(cause));
        }

        return sb.toString();
    }
}
