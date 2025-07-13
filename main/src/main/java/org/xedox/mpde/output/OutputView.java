package org.xedox.mpde.output;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import org.xedox.mpde.R;

public class OutputView extends FrameLayout {

    private View content;
    private Header header;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    public OutputView(Context context) {
        this(context, null);
    }

    public OutputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OutputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.console_layout, this);
        header = new Header(findViewById(R.id.header));
        content = findViewById(R.id.content);
        viewPager = findViewById(R.id.tabs_content);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager.setUserInputEnabled(false);
        header.setParentView(this);
        post(this::postInit);
    }
    
    private void postInit() {
    	header.setOnMoveListener((y) -> {
            content.setTranslationY(y + header.getHeight());
        });
        header.setY(getHeight() - header.getHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (header.onTouchEvent(event)) {
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (header.isMoving()) {
            return header.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }
}