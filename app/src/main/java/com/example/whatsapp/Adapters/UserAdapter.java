package com.example.whatsapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.Activities.Message;
import com.example.whatsapp.Model.Chat;
import com.example.whatsapp.Model.User;
import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<User> users;
    private boolean isChat;
    private String thelastMessage;

    public UserAdapter(List<User> users, Context context, boolean isChat) {
        this.users = users;
        this.context = context;
        this.isChat = isChat;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        /*java.lang.IllegalStateException: ViewHolder views must not be attached when created.
        Ensure that you are not passing 'true' to the attachToRoot parameter of LayoutInflater.inflate(..., boolean attachToRoot)*/
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final User user = users.get(position);

        //get and set data

        //username
        holder.username.setText(user.getUsername());

        //user image
        if (user.getImageURL().equals("default")) {
            holder.user_profile.setImageResource(R.drawable.profile);
        } else {
            Glide.with(context).load(user.getImageURL()).into(holder.user_profile);
        }

        /*to know if it is chat or user fragment, if chat fragment make lastMessage visible,
        if user fragment  make lastMessage Un-visible*/
        if (isChat) {
            lastMessage(user.getId(), holder.lastMessage);
        } else {
            holder.lastMessage.setVisibility(View.GONE);
        }

        /*first know if it is chat fragment or no, if chat fragment,
        check if status online or offline, if it is user fragment no check user status*/
        if (isChat) {
            if (user.getStatus().equals("Online")) {
                holder.status_on.setVisibility(View.VISIBLE);
                holder.status_off.setVisibility(View.GONE);
            } else {
                holder.status_on.setVisibility(View.GONE);
                holder.status_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.status_on.setVisibility(View.GONE);
            holder.status_off.setVisibility(View.GONE);
        }

        //handle recycler view clicks
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Message.class);
                intent.putExtra("user_id", user.getId());
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    private void lastMessage(final String userid, final TextView last_message) {
        thelastMessage = "Default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        if (firebaseUser != null) {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        assert chat != null;
                        if (chat.getSender().equals(userid) && chat.getReceiver().equals(firebaseUser.getUid()) || chat.getSender().equals(firebaseUser.getUid()) && chat.getReceiver().equals(userid)) {
                            thelastMessage = chat.getMessage();
                        }
                    }
                    if ("Default".equals(thelastMessage)) {
                        last_message.setText("No Messages");
                    } else {
                        last_message.setText(thelastMessage);
                    }
                    //thelastMessage="Default";
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView username, lastMessage;
        ImageView user_profile;
        CircleImageView status_on;
        CircleImageView status_off;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            user_profile = itemView.findViewById(R.id.profile_image);
            status_on = itemView.findViewById(R.id.status_on);
            status_off = itemView.findViewById(R.id.status_off);
            lastMessage = itemView.findViewById(R.id.last_message);
        }
    }

}
