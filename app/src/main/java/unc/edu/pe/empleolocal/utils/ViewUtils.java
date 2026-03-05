package unc.edu.pe.empleolocal.utils;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import unc.edu.pe.empleolocal.R;

public class ViewUtils {

    public enum MsgType {
        SUCCESS, ERROR, INFO, WARNING
    }

    public static void showSnackbar(Activity activity, String message, MsgType type) {
        if (activity == null) return;
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView == null) return;

        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();

        // Color logic
        int color;
        switch (type) {
            case SUCCESS: color = Color.parseColor("#43A047"); break;
            case ERROR: color = Color.parseColor("#E53935"); break;
            case WARNING: color = Color.parseColor("#FB8C00"); break;
            case INFO:
            default: color = Color.parseColor("#1F89E5"); break;
        }

        // Card Styling
        snackbarView.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_snackbar_card));
        
        // Add Margins to make it look like a floating card
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.setMargins(48, 0, 48, 100);
        params.gravity = Gravity.BOTTOM;
        snackbarView.setLayoutParams(params);

        // Text Styling
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextColor(color); // Text takes the status color
            textView.setTextSize(14);
            textView.setCompoundDrawablesWithIntrinsicBounds(getIcon(type), 0, 0, 0);
            textView.setCompoundDrawablePadding(24);
            textView.setGravity(Gravity.CENTER_VERTICAL);
        }
        
        snackbar.show();
    }

    private static int getIcon(MsgType type) {
        switch (type) {
            case SUCCESS: return R.drawable.ic_check_circle;
            case ERROR: return R.drawable.ic_lock; // Use existing icons or add specific ones
            case WARNING: return R.drawable.ic_security;
            default: return R.drawable.ic_notifications;
        }
    }
}
