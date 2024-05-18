package il.androidcourse.infonow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;

public class SplashActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startProgressBar();

        new Handler().postDelayed(() -> {
            //FirebaseAuth auth = FirebaseAuth.getInstance();
            //if (auth.getCurrentUser() != null) {
            if (null == null) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            }
            finish();
        }, 3000); // Splash screen duration
    }

    private void startProgressBar(){
        progressBar = findViewById(R.id.progressBar);
        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        animator.setDuration(3000); // Animation duration: 3 second
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }
}
