package com.qira.portaria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;


public class CallAcitivity extends AppCompatActivity{


    public static final String ACTION_CLOSE = "com.qira.portaria.ACTION_CLOSE";
    public static final int DELAY_1 = 2000;
    public static final int DELAY_2 = 3000;
    private TextView roomName;
    private TextView roomNumber;
    private ImageView callIconOne;
    private ImageView callIconTwo;
    private ImageView callIconThree;
    public Handler h = new Handler();
    public Handler h1 = new Handler();


    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_call);
        initiateViews();
    }

    @Override
    public void onResume(){
        super.onResume();
        startAnimationButtonOne();

        h.postDelayed(new Runnable() {
            public void run() {
                startAnimationButtonTwo();
                h.postDelayed(this, DELAY_1);
            }
        }, DELAY_1);


        h1.postDelayed(new Runnable() {
            public void run() {
                startAnimationButtonThree();
                h1.postDelayed(this, DELAY_2);
            }
        }, DELAY_2);






    }

    private void startAnimationButtonThree() {
        callIconOne.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.blink, null));
    }

    private void startAnimationButtonTwo() {
        callIconTwo.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.blink, null));
    }

    private void startAnimationButtonOne() {
        callIconThree.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.blink, null));
    }

    private void initiateViews() {
        roomName = (TextView) findViewById(R.id.room_name);
        roomNumber = (TextView) findViewById(R.id.room_number);
        callIconOne = (ImageView) findViewById(R.id.call_icon_1);
        callIconTwo = (ImageView) findViewById(R.id.call_icon_2);
        callIconThree = (ImageView) findViewById(R.id.call_icon_3);
    }

    class FirstReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("FirstReceiver", "FirstReceiver");
            if (intent.getAction().equals(ACTION_CLOSE)) {
                CallAcitivity.this.finish();
            }
        }
    }
}
