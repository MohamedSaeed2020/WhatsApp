package com.example.whatsapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsapp.Adapters.MessageAdapter;
import com.example.whatsapp.Notifications.APIService;
import com.example.whatsapp.Model.Chat;
import com.example.whatsapp.Model.User;
import com.example.whatsapp.Notifications.Client;
import com.example.whatsapp.Notifications.Data;
import com.example.whatsapp.Notifications.MyResponse;
import com.example.whatsapp.Notifications.Sender;
import com.example.whatsapp.Notifications.Token;
import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Message extends AppCompatActivity {


    Toolbar toolbar;
    APIService apiService;
    RecyclerView recyclerView;
    CircleImageView profile_image;
    TextView username;
    ImageButton send_btn;
    String hisImage;
    boolean notify = false;
    EditText msg;
    Intent intent;
    String userID;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    //ValueEventListener seenListner;
    List<Chat> chats;
    MessageAdapter messageAdapter;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_layout);

        //init toolbar
        toolbar = findViewById(R.id.toolbar_id);
        //Note should be imported from androidx.appcompat.widget NOT androidx.widget
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                startActivity(new Intent(Message.this, Home.class));
                //.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        //init api service
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        //init recycler view
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //init views
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        send_btn = findViewById(R.id.btn_send);
        msg = findViewById(R.id.txt_send);
        intent = getIntent();
        //get user id that is sent when some user click on any another user to chat with from user fragment
        userID = intent.getStringExtra("user_id");
        //user that is now logging to the system
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        //set the data to the user you chatting with, such name and image
        assert userID != null;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;

                //get user image
                hisImage = user.getImageURL();
                //set user name
                username.setText(user.getUsername());

                //set user profile image
               /* if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.drawable.profile);
                } else {
                    Glide.with(getApplicationContext()).load(hisImage).into(profile_image);

                }*/

                //set user profile image
                try {
                    //image received, set it to imageView in toolbar
                    Picasso.get().load(hisImage).placeholder(R.drawable.profile).into(profile_image);

                } catch (Exception ex) {
                    //there exception getting picture,set default picture
                    Picasso.get().load(R.drawable.profile).into(profile_image);
                }

                readMessage(user.getImageURL());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //handle send message button clicks
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String message = msg.getText().toString().trim();
                if (!message.equals("")) {
                    sendMessage(firebaseUser.getUid(), userID, message);
                } else {
                    Toast.makeText(getApplicationContext(), "You can't send empty message!", Toast.LENGTH_SHORT).show();
                }
                msg.setText("");
            }
        });

        //show user message
        //readMessage();

        // seenMessage();
    }

    /*private void seenMessage(){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListner= reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userID)) {
                        HashMap<String, Object> hashMap=new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);  //important
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    private void sendMessage(String sender, final String receiver, final String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        //hashMap.put("isseen",false);

        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Chatlists")
                .child(firebaseUser.getUid())
                .child(userID);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    myRef.child("id").setValue(userID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                if (notify) {
                    sendNotification(receiver, user.getUsername(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void sendNotification(String receiver, final String username, final String message) {

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = reference.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), username + ": " + message, "New Message", userID, R.drawable.icon);
                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        assert response.body() != null;
                                        if (response.body().success != 1) {
                                            Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }


                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void readMessage(final String image_url)  {

        chats = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getSender().equals(userID) && chat.getReceiver().equals(firebaseUser.getUid()) ||
                            chat.getSender().equals(firebaseUser.getUid()) && chat.getReceiver().equals(userID)) {
                        chats.add(chat);
                    }
                    messageAdapter = new MessageAdapter(chats, getApplicationContext(), image_url);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void currentUser(String userid) {

        SharedPreferences.Editor editor = getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit();
        editor.putString("currentUser", userid);
        editor.apply();
    }

   /* public void showStatus(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }*/

    public void showStatus(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //showStatus("Online");
        currentUser(userID);
        showStatus("Online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        //showStatus("Offline");
        //reference.removeEventListener(seenListner);
        currentUser("none");
        showStatus("Offline");

    }
}
