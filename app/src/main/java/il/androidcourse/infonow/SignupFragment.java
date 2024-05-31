package il.androidcourse.infonow;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupFragment extends Fragment {

    private EditText editTextEmail, editTextUsername, editTextPassword;
    private Button buttonSignup;
    private TextView textViewLogin;

    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextUsername = view.findViewById(R.id.editTextUsername);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        buttonSignup = view.findViewById(R.id.buttonSignup);
        textViewLogin = view.findViewById(R.id.textViewLogin);

        auth = FirebaseAuth.getInstance();

        buttonSignup.setOnClickListener(v -> signupUser());

        textViewLogin.setOnClickListener(v -> {
            // Navigate to LoginFragment
            ((AuthActivity) requireActivity()).showFragment(new LoginFragment());
        });

        return view;
    }

    private void signupUser() {
        String email = editTextEmail.getText().toString().trim();
        String name = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        UserProfileChangeRequest userProfile = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name).build();

                        FirebaseUser user = auth.getCurrentUser();
                        user.updateProfile(userProfile);

                        // Sign up success, navigate to MainActivity
                        ((AuthActivity) requireActivity()).navigateToMainActivity();
                    } else {
                        // If sign up fails, display a message to the user.
                        Toast.makeText(getActivity(), "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

