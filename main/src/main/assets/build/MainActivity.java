package %package%;

import android.app.Activity;
import android.os.Bundle;
import main.java; // main.pde
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import processing.android.PFragment;
import processing.core.PApplet;

public class MainActivity extends AppCompatActivity {
    
    private PApplet sketch;
            
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sketch = new main();
        PFragment fragment = new PFragment(sketch);
        FrameLayout frame = new FrameLayout(this);
        getSupportFragmentManager().beginTransaction().add(frame.getId(), fragment).commit();
        setContentView(frame);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sketch != null) {
            sketch.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (sketch != null) {
            sketch.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (sketch != null) {
            sketch.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (sketch != null && !sketch.onBackPressed()) {
            super.onBackPressed();
        }
    }
}