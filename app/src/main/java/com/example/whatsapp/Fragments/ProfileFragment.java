package com.example.whatsapp.Fragments;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsapp.Model.User;
import com.example.whatsapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    private CircleImageView profile_image;
    private TextView username;
    private DatabaseReference reference;
    private StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;  //Request code
    private Uri imageURI;
    private StorageTask uploadTask;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init views
        profile_image = view.findViewById(R.id.user_profile_img);
        username = view.findViewById(R.id.profile_username);

        //current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //firebase database reference
        assert firebaseUser != null;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        //set profile details(name and image) from database
        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                //set name
                assert user != null;
                username.setText(user.getUsername());
                //set profile image
                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.drawable.profile);
                } else {

                    assert getActivity()!=null;
                    Glide.with(getActivity()).load(user.getImageURL()).into(profile_image);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //firebase storage reference
        storageReference = FirebaseStorage.getInstance().getReference("Uploads");

        //upload image when profile_image is clicked
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });

        return view;
    }

    private void openImage() {

        Intent intent = new Intent();
        intent.setType("image/*");  //Should be written
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST); // Activity is started with requestCode 1
    }

    // Call Back method  to get the results form other Activity
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check if the request code is same as what is passed  here it is 1
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageURI = data.getData();
        }
        if (uploadTask != null && uploadTask.isInProgress()) {
            Toast.makeText(getContext(), "Uploading in Progress...", Toast.LENGTH_SHORT).show();
        } else {
            uploadImage();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = Objects.requireNonNull(getContext()).getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        if (imageURI != null) {
            progressDialog.setMessage("Uploading");
            progressDialog.show();
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageURI));
            uploadTask = fileReference.putFile(imageURI);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadURI = task.getResult();
                        assert downloadURI != null;
                        String mURI = downloadURI.toString();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageURL", mURI);
                        reference.updateChildren(hashMap);
                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(getContext(), "No Image Selected!", Toast.LENGTH_SHORT).show();
        }
    }
}
