package com.kirtan.audionotepro.AudioNotePro;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by Kirtan on 7/26/16.
 */
public class AudioNotePro extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
