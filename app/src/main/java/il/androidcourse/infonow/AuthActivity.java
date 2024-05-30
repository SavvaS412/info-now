package il.androidcourse.infonow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        auth = FirebaseAuth.getInstance();
        showFragment(new LoginFragment());
    }

    // Method to replace fragment in AuthActivity
    public void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // Method to navigate from AuthActivity to MainActivity
    public void navigateToMainActivity() {
        startActivity(new Intent(AuthActivity.this, MainActivity.class));
        finish(); // Finish AuthActivity to prevent going back to it
    }
}