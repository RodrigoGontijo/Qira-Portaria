package com.qira.portaria;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class SettingsActivity extends AppCompatActivity {

    private TextView registrationState;
    private TextView wifiState;
    private TextView sipAdress;
    private TextView call;
    private Button backButton;
    public Handler h = new Handler();


    private static final int DELAY = 3000; //milliseconds

    @Override
    public void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.activity_settings);
        setViews();
        checkStates();
    }

    @Override
    public void onResume(){
        super.onResume();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    public void setViews() {
        registrationState = (TextView) findViewById(R.id.registrationState);
        wifiState = (TextView) findViewById(R.id.WifiState);
        sipAdress = (TextView) findViewById(R.id.sipAdress);
        call = (TextView) findViewById(R.id.callState);
        backButton = (Button) findViewById(R.id.back_button);
    }

    public void checkWifiConnection() {
        SharedPreferences sharedPreferencesState = getSharedPreferences("state", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesState.edit();

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            editor.putString("WifiState", "Check your wifi state");
        } else {
            editor.putString("WifiState", "Connected");
        }
        editor.apply();
    }


    private void checkStates() {
        final SharedPreferences sharedPreferencesState = getSharedPreferences("state", MODE_PRIVATE);

        checkWifiConnection();
        h.postDelayed(new Runnable() {
            public void run() {
                registrationState.setText(sharedPreferencesState.getString("registrationState", ""));
                wifiState.setText(sharedPreferencesState.getString("WifiState", ""));
                call.setText(sharedPreferencesState.getString("call", ""));
                sipAdress.setText(sharedPreferencesState.getString("sipAddress", ""));

                h.postDelayed(this, DELAY);
            }
        }, DELAY);
    }


}
