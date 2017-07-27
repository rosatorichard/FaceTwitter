package com.batchmates.android.facebooksdk;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.share.model.ShareLinkContent;
import com.google.gson.Gson;

public class FaceBookLogin extends AppCompatActivity {

    private static final String TAG = "Second Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_book_login);

        String token=getIntent().getStringExtra("TOKEN");
        Log.d(TAG, "onCreate: "+token);

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("https://developers.facebook.com"))
                .build();


    }
}
