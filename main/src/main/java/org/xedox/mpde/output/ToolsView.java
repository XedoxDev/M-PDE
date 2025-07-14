package org.xedox.mpde.output;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import org.xedox.mpde.R;
import org.xedox.utils.ErrorDialog;

public class ToolsView extends FrameLayout {
    private View content;
    private Header header;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ToolWindowAdapter adapter;
    public String buildLogs;

    public ToolsView(Context context) {
        this(context, null);
    }

    public ToolsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToolsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        inflate(context, R.layout.console_layout, this);
        buildLogs = context.getString(R.string.build_logs);
        if (!(context instanceof FragmentActivity)) {
            throw new IllegalArgumentException("OutputView requires FragmentActivity context");
        }

        header = new Header(findViewById(R.id.header));
        content = findViewById(R.id.content);
        viewPager = findViewById(R.id.tabs_content);
        tabLayout = findViewById(R.id.tab_layout);
        header.setParentView(this);
        viewPager.setUserInputEnabled(false);
        adapter = new ToolWindowAdapter((FragmentActivity) context);
        viewPager.setAdapter(adapter);

        adapter.add(BuildOutputFragment.newInstance(buildLogs));

        new TabLayoutMediator(
                        tabLayout,
                        viewPager,
                        (tab, pos) -> {
                            try {
                            	tab.setText(adapter.get(pos).getTitle());
                            } catch(Exception err) {
                            	ErrorDialog.show(getContext(), "Failed to setup tab in tool window", err);
                            }
                        })
                .attach();

        post(this::setupHeader);
    }
    
    public BuildOutputFragment getBuildOutput() {
        return adapter.getFragmentByName(buildLogs);
    }

    private void setupHeader() {
        header.setOnMoveListener(y -> content.setTranslationY(y + header.getHeight()));
        header.setY(getHeight() - header.getHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return header.onTouchEvent(event) || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return header.isMoving() ? header.onTouchEvent(event) : super.onTouchEvent(event);
    }
}
