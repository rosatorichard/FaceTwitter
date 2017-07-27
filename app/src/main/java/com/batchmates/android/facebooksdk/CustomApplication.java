package com.batchmates.android.facebooksdk;

import android.app.Application;

import com.twitter.sdk.android.core.Twitter;

/**
 * Created by Android on 7/27/2017.
 */

public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Twitter.initialize(this);
    }
}
