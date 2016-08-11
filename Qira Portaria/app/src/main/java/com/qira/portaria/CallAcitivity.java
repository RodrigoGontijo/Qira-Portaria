package com.qira.portaria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;


public class CallAcitivity extends AppCompatActivity{


    public static final String ACTION_CLOSE = "com.qira.portaria.ACTION_CLOSE";
    private TextView roomName;
    private TextView roomNumber;


    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_call);
        initiateViews();
    }

    private void initiateViews() {
        roomName = (TextView) findViewById(R.id.room_name);
        roomNumber = (TextView) findViewById(R.id.room_number);
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
