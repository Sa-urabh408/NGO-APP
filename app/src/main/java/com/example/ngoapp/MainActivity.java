package com.example.ngoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends Activity {

    ImageView welcomeLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeLogo = findViewById(R.id.welcomelogo1);

        // Animation
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fadein_anim);
        welcomeLogo.startAnimation(animation);

        // Splash delay — check login session after splash
        welcomeLogo.postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                Intent intent;
                if (currentUser != null) {
                    // User is already logged in — decide where to send them
                    String email = currentUser.getEmail();
                    if (email != null && email.equalsIgnoreCase("Admin123@gmail.com")) {
                        intent = new Intent(MainActivity.this, adminpanel.class);
                    } else {
                        intent = new Intent(MainActivity.this, Home1.class);
                    }
                } else {
                    // Not logged in — go to login page
                    intent = new Intent(MainActivity.this, loginpage.class);
                }

                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}
