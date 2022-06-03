package com.example.walter.nursecaller_final;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
private static int SPLASH_SCREEN_TIMEOUT=2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  getWindow().setFlags();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);


      new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
              Intent intent=new Intent(com.example.walter.nursecaller_final.SplashScreen.this, com.example.walter.nursecaller_final.MainActivity.class);
                      startActivity(intent);
                      finish();
          }
      },SPLASH_SCREEN_TIMEOUT);


    }
}