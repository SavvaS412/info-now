package il.androidcourse.infonow;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class SettingsFragment extends Fragment {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private StorageReference storageReference;

    private TextView profileName;
    private TextView profileEmail;
    private ImageView profileImage;
    private Button btnLogin;
    private Button btnSignOut;

    Uri imageUri;

    public static final String PREFS_NAME = "user";
    Switch syncSettings;
    Switch israelHayom;
    Switch mako;
    Switch haaretz;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        imageUri = result.getData().getData();
                        uploadImage();
                    }
                    else Toast.makeText(getContext(), "Failed uploading image", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        profileName = (TextView) view.findViewById(R.id.profileName);
        profileEmail = (TextView) view.findViewById(R.id.profileEmail);
        profileImage = (ImageView) view.findViewById(R.id.profileImage);

        btnLogin = (Button) view.findViewById(R.id.profileLogin);
        btnLogin.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navigateToAuthentication();
        });

        btnSignOut = (Button) view.findViewById(R.id.profileSignOut);
        btnSignOut.setOnClickListener(v -> signOut());

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        syncSettings = (Switch) view.findViewById(R.id.syncSettingsSwitch);
        israelHayom = (Switch) view.findViewById(R.id.israel_hayom_switch);
        mako = (Switch) view.findViewById(R.id.mako_switch);
        haaretz = (Switch) view.findViewById(R.id.haaretz_switch);

        Context context = getContext();
        SharedPreferences userPreferences = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = userPreferences.edit();
        boolean isSyncSettingsEnabled = userPreferences.getBoolean("syncSettings", true);

        if (isSyncSettingsEnabled)
        {
            // TODO: load from db
        }
        else
        {
            loadSettingsSharedPreferences(userPreferences);
        }

        syncSettings.setChecked(isSyncSettingsEnabled);

        syncSettings.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // TODO: load from db
            } else {
            }
        });

        if (user == null || user.isAnonymous())
            viewGuest(view);
        else
            viewUser(view);

        // Inflate the layout for this fragment
        return view;
    }

    private void loadSettingsSharedPreferences(SharedPreferences userPreferences) {
        boolean isIsraelHayomEnabled = userPreferences.getBoolean("israelHayom", true);
        boolean isMakoEnabled = userPreferences.getBoolean("mako", true);
        boolean isHaaretzEnabled = userPreferences.getBoolean("haaretz", true);
        israelHayom.setChecked(isIsraelHayomEnabled);
        mako.setChecked(isMakoEnabled);
        haaretz.setChecked(isHaaretzEnabled);
    }

    @Override
    public void onStop() {
        super.onStop();
        Context context = getContext();
        SharedPreferences userPreferences = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = userPreferences.edit();
        editor.putBoolean("syncSettings", syncSettings.isChecked());
        if (user == null || user.isAnonymous() || !syncSettings.isChecked())    // save to SharedPreferences
        {
            editor.putBoolean("israelHayom", israelHayom.isChecked());
            editor.putBoolean("mako", mako.isChecked());
            editor.putBoolean("haaretz", haaretz.isChecked());
        }
        else    // save to Firestore
        {
            // TODO: save to Firestore
        }
        editor.commit();
    }

    private void viewGuest(View view) {
        btnSignOut.setVisibility(View.GONE);
        profileEmail.setVisibility(View.GONE);

        btnLogin.setVisibility(View.VISIBLE);
        profileName.setText("Guest");
        profileImage.setImageResource(R.drawable.guest);
        profileImage.setOnClickListener(v -> {});
    }

    private void viewUser(View view) {
        btnLogin.setVisibility(View.GONE);

        profileEmail.setVisibility(View.VISIBLE);
        btnSignOut.setVisibility(View.VISIBLE);
        profileName.setText(user.getDisplayName());
        profileEmail.setText(user.getEmail());

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });

        if (user.getPhotoUrl() == null)
            profileImage.setImageResource(R.drawable.guest);
        else {
            Glide.with(getContext())
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.loading) // Add a placeholder image if you have one
                    .error(R.drawable.error) // Add an error image if you have one
                    .into(profileImage);
        }
    }

    private void signOut() {
        auth.signOut();
        ((MainActivity) requireActivity()).navigateToAuthentication();
    }

    private void uploadImage() {
        StorageReference ref = storageReference.child("images/" + user.getUid());
        ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Glide.with(getContext()).load(imageUri).into((profileImage));
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(uri).build());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getContext(), "Failed retrieving image url", Toast.LENGTH_SHORT).show();
                    }
                });

                Toast.makeText(getContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Failed uploading image", Toast.LENGTH_SHORT).show();
                profileImage.setImageResource(R.drawable.error);
            }
        });
}}