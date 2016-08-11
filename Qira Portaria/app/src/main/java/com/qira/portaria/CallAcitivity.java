package com.qira.portaria;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


public class CallAcitivity extends AppCompatActivity{

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
}
