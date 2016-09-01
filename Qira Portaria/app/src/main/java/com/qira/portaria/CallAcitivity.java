package com.qira.portaria;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.sip.SipException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class CallAcitivity extends AppCompatActivity {


    public static final String ACTION_CLOSE = "com.qira.portaria.ACTION_CLOSE";
    public static final int DELAY_1 = 1000;


    private TextView roomName;
    private TextView roomNumber;
    private TextView actionText;
    private ImageView callIconOne;
    private ImageView callIconTwo;
    private ImageView callIconThree;
    private ImageView endCall;
    private int animationState;
    public Handler h = new Handler();
    private Animation animation;
    private Bundle bundle;


    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_call);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initiateViews();
        animationState = 1;

        roomNumber.setText(getIntent().getExtras().getString("RoomNumber", "Error"));
        roomName.setText(getIntent().getExtras().getString("RoomName", "Error"));
    }

    @Override
    public void onResume() {
        super.onResume();


        animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(500); //You can manage the blinking time with this parameter
        animation.setStartOffset(20);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(Animation.INFINITE);

        h.postDelayed(new Runnable() {
            public void run() {


                switch (animationState) {
                    case 1:
                        startAnimationButtonThree();
                        animationState = 2;
                        break;

                    case 2:
                        startAnimationButtonTwo();
                        animationState = 3;
                        break;

                    case 3:
                        startAnimationButtonOne();
                        animationState = 1;
                        break;
                }

                h.postDelayed(this, DELAY_1);
            }
        }, DELAY_1);


        BroadcastReceiver broadcast_receiver_finish_activity = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish_activity")) {
                    finish();
                }
            }
        };
        registerReceiver(broadcast_receiver_finish_activity, new IntentFilter("finish_activity"));

        BroadcastReceiver broadcast_receiver_finish_call = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("call_established")) {
                    callIconOne.setVisibility(View.INVISIBLE);
                    callIconTwo.setVisibility(View.INVISIBLE);
                    callIconThree.setVisibility(View.INVISIBLE);
                    actionText.setText("CHAMADA EM CURSO");
                }
            }
        };
        registerReceiver(broadcast_receiver_finish_call, new IntentFilter("call_established"));


    }

    private void startAnimationButtonThree() {
        callIconTwo.clearAnimation();
        callIconOne.startAnimation(animation);
        callIconThree.clearAnimation();
    }

    private void startAnimationButtonTwo() {
        callIconOne.clearAnimation();
        callIconTwo.startAnimation(animation);
        callIconThree.clearAnimation();
    }

    private void startAnimationButtonOne() {
        callIconOne.clearAnimation();
        callIconThree.startAnimation(animation);
        callIconTwo.clearAnimation();
    }

    private void initiateViews() {
        roomName = (TextView) findViewById(R.id.room_name);
        roomNumber = (TextView) findViewById(R.id.room_number);
        actionText = (TextView) findViewById(R.id.calling);
        callIconOne = (ImageView) findViewById(R.id.call_icon_1);
        callIconTwo = (ImageView) findViewById(R.id.call_icon_2);
        callIconThree = (ImageView) findViewById(R.id.call_icon_3);
        endCall = (ImageView) findViewById(R.id.end_call);


        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("finish_call");
                sendBroadcast(intent);
                finish();
            }
        });


    }


}
