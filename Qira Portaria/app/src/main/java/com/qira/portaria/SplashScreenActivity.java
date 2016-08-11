package com.qira.portaria;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class SplashScreenActivity extends AppCompatActivity {

    private PulsatorLayout pulsator;
    private RelativeLayout relativeLayout;
    private ImageView logo_splash;
    private AnimatorSet mAnimationSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onResume() {
        super.onResume();
        setViews();
        setClickListerner();
        if (!mAnimationSet.isRunning())
            mAnimationSet.start();
    }


    private void setViews() {
        pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        assert pulsator != null;
        pulsator.start();

        relativeLayout = (RelativeLayout) findViewById(R.id.splash_screen_activity);

        logo_splash = (ImageView) findViewById(R.id.logo_splash);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(logo_splash, "alpha", .1f, 1f);
        fadeIn.setDuration(3500);
        mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn);
        mAnimationSet.start();
    }

    private void setClickListerner() {

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAnimationSet.end();
                pulsator.stop();
                Intent mainActivity = new Intent(getBaseContext(),
                        WalkieTalkieActivity.class);
                startActivity(mainActivity);
            }
        });
    }

}
