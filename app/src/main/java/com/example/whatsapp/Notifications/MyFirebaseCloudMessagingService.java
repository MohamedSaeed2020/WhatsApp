package com.example.whatsapp.Notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.whatsapp.Activities.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseCloudMessagingService extends FirebaseMessagingService {


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKEN", s);
        updateToken(s);
    }


    private void updateToken(String refreshToken) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(refreshToken);
        if (firebaseUser != null) {
            reference.child(firebaseUser.getUid()).setValue(token1);
        } else {
            Log.d("THE_TOKEN", "Will change token when you login for security reason");
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String sent = remoteMessage.getData().get("sent");
        String user = remoteMessage.getData().get("user");
        SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        String currentUser = sharedPreferences.getString("currentUser", "NONE");
        Log.d("currentUser", currentUser);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert sent != null;

        //check if i am are the receiver
        if (firebaseUser != null && sent.equals(firebaseUser.getUid())) {

            /*if we are on message activity ,currentUser will be equal to user so not send notification if on pause message activity,
             currentUser will be equal to none so send notification*/
            if (!currentUser.equals(user)) {
                sendNotification(remoteMessage);
            }
        }


    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        assert user != null;

        //remove all character and spaces
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));  //Request code
        Intent intent = new Intent(this, Message.class);
        Bundle bundle = new Bundle();
        //send it to message fragment
        bundle.putString("user_id", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        assert icon != null;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int i = 0;
        if (j > i) {
            i = j;
        }
        assert notificationManager != null;
        notificationManager.notify(i, builder.build());


    }
}
