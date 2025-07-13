package org.xedox.mpde.output;

import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import org.xedox.mpde.R;

public class Header {
    private View view;
    private TextView title, subtitle;
    private float lastTouchX;
    private float lastTouchY;
    private boolean isMoving = false;
    private OnMoveListener onMoveListener;
    private View parentView;

    public Header(View view) {
        this.view = view;
        title = view.findViewById(R.id.title);
        subtitle = view.findViewById(R.id.subtitle);
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN && isInsideView(x, y)) {
            lastTouchX = x;
            lastTouchY = y;
            isMoving = true;
            return true;
        } else if (action == MotionEvent.ACTION_MOVE && isMoving) {
            float dy = y - lastTouchY;
            setY(getY() + dy);
            lastTouchX = x;
            lastTouchY = y;
            return true;
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            isMoving = false;
            return true;
        }
        return false;
    }

    private boolean isInsideView(float rawX, float rawY) {
        float viewX = view.getX();
        float viewY = view.getY();
        return rawX >= viewX
                && rawX <= viewX + view.getWidth()
                && rawY >= viewY
                && rawY <= viewY + view.getHeight();
    }

    public void setY(float y) {
        float finalY = y;
        if (finalY < 0) {
            finalY = 0;
        }

        if (parentView != null) {
            int parentHeight = parentView.getHeight();
            int viewHeight = getHeight();

            if (viewHeight > 0 && parentHeight > 0 && finalY > parentHeight - viewHeight) {
                finalY = parentHeight - viewHeight;
            }
        }

        view.setTranslationY(finalY);

        if (onMoveListener != null) {
            onMoveListener.onMove(finalY);
        }
    }

    public int getHeight() {
        return view.getHeight();
    }

    public float getY() {
        return view.getTranslationY();
    }

    public void setVisibility(int visibility) {
        view.setVisibility(visibility);
    }

    public int getVisibility() {
        return view.getVisibility();
    }

    public View getView() {
        return view;
    }

    public void setTitle(String text) {
        if (title != null) title.setText(text);
    }

    public String getTitle() {
        return title != null ? title.getText().toString() : "";
    }

    public void setSubtitle(String text) {
        if (subtitle != null) subtitle.setText(text);
    }

    public String getSubtitle() {
        return subtitle != null ? subtitle.getText().toString() : "";
    }

    public void setTitleVisible(boolean visible) {
        if (title != null) title.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setSubtitleVisible(boolean visible) {
        if (subtitle != null) subtitle.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public boolean isMoving() {
        return isMoving;
    }

    public static interface OnMoveListener {
        void onMove(float y);
    }

    public OnMoveListener getOnMoveListener() {
        return this.onMoveListener;
    }

    public void setOnMoveListener(OnMoveListener onMoveListener) {
        this.onMoveListener = onMoveListener;
    }

    public View getParentView() {
        return this.parentView;
    }

    public void setParentView(View parentView) {
        this.parentView = parentView;
    }
}
