package il.androidcourse.infonow;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
//            FirebaseAuth auth = FirebaseAuth.getInstance();
//            if (auth.getCurrentUser() != null) {
//                startActivity(new Intent(SplashActivity.this, MainActivity.class));
//            } else {
//                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
//            }
            finish();
        }, 3000); // Splash screen duration
    }
}
