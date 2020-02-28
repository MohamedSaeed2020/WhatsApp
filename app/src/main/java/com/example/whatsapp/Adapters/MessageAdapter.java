package com.example.whatsapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.Model.Chat;
import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int MSG_TYPE_RIGHT = 1;
    private static final int MSG_TYPE_LEFT = 0;
    private List<Chat> chats;
    private Context context;
    private String image_url;

    public MessageAdapter(List<Chat> chats, Context context, String image_url) {
        this.chats = chats;
        this.context = context;
        this.image_url = image_url;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        final Chat chat = chats.get(position);

        //set message
        holder.show_message.setText(chat.getMessage());

        //set image for receiver
       /* if (image_url.equals("default")) {
            holder.user_profile.setImageResource(R.drawable.profile);
        } else {
            Glide.with(context).load(image_url).into(holder.user_profile);

        }*/

        //set image for receiver
        try {
            //image received, set it to imageView in toolbar
            Picasso.get().load(image_url).placeholder(R.drawable.profile).into(holder.user_profile);

        } catch (Exception ex) {
            //there exception getting picture,set default picture
            Picasso.get().load(R.drawable.profile).into(holder.user_profile);
        }


       /* //check last message
        if (position==chats.size()-1){
            if(chat.getSeen()){
                holder.messageSeen.setText("Seen");
            }
            else {
                holder.messageSeen.setText("Delivered");
            }
        }
        else {
            holder.messageSeen.setVisibility(View.GONE);
        }*/


    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        if (chats.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView show_message;
        ImageView user_profile;

        //TextView messageSeen;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            user_profile = itemView.findViewById(R.id.profile_image);
            //messageSeen=itemView.findViewById(R.id.seen_message);

        }
    }


}
