package com.qira.portaria;


import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class SplashScreenActivity extends AppCompatActivity{

    private PulsatorLayout pulsator;
    private RelativeLayout relativeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setViews();
        setClickListernet();
    }


    private void setViews() {
        pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        assert pulsator != null;
        pulsator.start();

        relativeLayout = (RelativeLayout) findViewById(R.id.splash_screen_activity);

    }

    private void setClickListernet() {
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivity = new Intent(getBaseContext(),
                        WalkieTalkieActivity.class);
                startActivity(mainActivity);
            }
        });
    }

}
