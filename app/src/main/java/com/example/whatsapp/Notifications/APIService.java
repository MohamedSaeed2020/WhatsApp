package com.example.whatsapp.Notifications;

import com.example.whatsapp.Notifications.MyResponse;
import com.example.whatsapp.Notifications.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:Key=AAAAgafYOic:APA91bFtkxei5PZyij3YwAYntBvYLdQ_hKfFMFu5kNG2tjJo4qh-eGWlZonjXGxCwo1UP4ik3sMSy3hOZm2wSEbr2XrkMT7Vf7A0YjrPf8j8r5h0a_RMt7UMi0EhPdUMVvUtDvcEjzm1"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
