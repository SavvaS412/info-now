package il.androidcourse.infonow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SettingsFragment extends Fragment {
    private FirebaseAuth auth;
    private FirebaseUser user;

    private Button btnSignout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        //if (user != null)

        btnSignout = (Button) view.findViewById(R.id.profileSignOut);
        btnSignout.setOnClickListener(v -> signOut());

        // Inflate the layout for this fragment
        return view;
    }

    private void signOut() {
        auth.signOut();
        ((MainActivity) requireActivity()).navigateToAuthentication();
    }
}