/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qira.portaria;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;

/**
 * Handles all calling, receiving calls, and UI interaction in the WalkieTalkie app.
 */
public class WalkieTalkieActivity extends AppCompatActivity implements View.OnTouchListener {

    public String sipAddress = null;

    public SipManager manager = null;
    public SipProfile me = null;
    public SipAudioCall call = null;
    public Toast toast;
    public IncomingCallReceiver callReceiver;
    public Handler h = new Handler();


    public boolean isRegistred;

    private static final int CALL_ADDRESS = 1;
    private static final int SET_AUTH_INFO = 2;
    private static final int UPDATE_SETTINGS_DIALOG = 3;
    private static final int HANG_UP = 4;
    private static final int DELAY = 30000; //milliseconds


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(com.qira.portaria.R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // Set up the intent filter.  This will be used to fire an
        // IncomingCallReceiver when someone calls the SIP address used by this
        // application.
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initializeViews();

        initializeManager();

        checkIsRegistred();

        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user","8200");
        editor.putString("password", "@quaecoh6Ria@");
        editor.putString("domain", "172.16.100.251:5566");
        editor.apply();

    }




    @Override
    public void onStart() {
        super.onStart();
        // When we get back from the preference setting Activity, assume
        // settings have changed, and re-login with new auth info.
        initializeManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.close();
            callReceiver.kill();
        }

        closeLocalProfile();

        if (callReceiver != null) {
            this.unregisterReceiver(callReceiver);

        }
    }

    public void initializeManager() {
        if (manager == null) {
            manager = SipManager.newInstance(this);
        }

        initializeLocalProfile();
    }

    /**
     * Logs you into your SIP provider, registering this device as the location to
     * send SIP calls to for your SIP address.
     */
    public void initializeLocalProfile() {
        if (manager == null) {
            return;
        }

        if (me != null) {
            closeLocalProfile();
        }

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String username = prefs.getString(getResources().getString(com.qira.portaria.R.string.user), "");
        String password = prefs.getString(getResources().getString(com.qira.portaria.R.string.password), "");
        String domain = prefs.getString(getResources().getString(com.qira.portaria.R.string.domain), "");

        if (username.length() == 0 || domain.length() == 0 || password.length() == 0) {
            Toast toast = Toast.makeText(this, "Put the corret info", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        try {


            SipProfile.Builder builder = new SipProfile.Builder(username, domain);
            builder.setPassword(password);
            me = builder.build();

            Intent i = new Intent();
            i.setAction("android.SipDemo.INCOMING_CALL");
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, Intent.FILL_IN_DATA);
            manager.open(me, pi, null);


            // This listener must be added AFTER manager.open is called,
            // Otherwise the methods aren't guaranteed to fire.

            manager.setRegistrationListener(me.getUriString(), new SipRegistrationListener() {

                public void onRegistering(String localProfileUri) {
                    updateStatus("Registering with SIP Server...");
                    isRegistred = false;
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    updateStatus("Ready");
                    isRegistred = true;
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
                    updateStatus("Registration failed.  Please check settings.");
                    isRegistred = false;

                }

            });

        } catch (ParseException pe) {
            updateStatus("Connection Error.");
        } catch (SipException se) {
            updateStatus("Connection error.");
        }
    }

    /**
     * Closes out your local profile, freeing associated objects into memory
     * and unregistering your device from the server.
     */
    public void closeLocalProfile() {
        if (manager == null) {
            return;
        }
        try {
            if (me != null) {
                manager.close(me.getUriString());
            }
        } catch (Exception ee) {
            Log.d("WalkieTalkieActivity/onDestroy", "Failed to close local profile.", ee);
        }
    }

    /**
     * Make an outgoing call.
     */
    public void initiateCall() {

        updateStatus(sipAddress);

        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                // Much of the client's interaction with the SIP Stack will
                // happen via listeners.  Even making an outgoing call, don't
                // forget to set up a listener to set things up once the call is established.
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    call.startAudio();
                    call.setSpeakerMode(true);
                    if(call.isMuted()) {
                        call.toggleMute();
                    }
                    updateStatus(call);
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    updateStatus("Ready.");
                    callReceiver.kill();
                }
            };

            call = manager.makeAudioCall(me.getUriString(), sipAddress, listener, 30);

        } catch (Exception e) {
            Log.i("WalkieTalkieActivity/InitiateCall", "Error when trying to close manager.", e);
            if (me != null) {
                try {
                    manager.close(me.getUriString());
                } catch (Exception ee) {
                    Log.i("WalkieTalkieActivity/InitiateCall",
                            "Error when trying to close manager.", ee);
                    ee.printStackTrace();
                }
            }
            if (call != null) {
                call.close();
            }
        }
    }

    /**
     * Updates the status box at the top of the UI with a messege of your choice.
     *
     * @param status The String to display in the status box.
     */
    public void updateStatus(final String status) {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        this.runOnUiThread(new Runnable() {
            public void run() {
                toast = Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT);
                toast.show();
            }
        });


    }

    /**
     * Updates the status box with the SIP address of the current call.
     *
     * @param call The current, active call.
     */
    public void updateStatus(SipAudioCall call) {
        String useName = call.getPeerProfile().getDisplayName();
        if (useName == null) {
            useName = call.getPeerProfile().getUserName();
        }
        updateStatus(useName + "@" + call.getPeerProfile().getSipDomain());
    }

    /**
     * Updates whether or not the user's voice is muted, depending on whether the button is pressed.
     *
     * @param v     The View where the touch event is being fired.
     * @param event The motion to act on.
     * @return boolean Returns false to indicate that the parent view should handle the touch event
     * as it normally would.
     */
    public boolean onTouch(View v, MotionEvent event) {
        if (call == null) {
            return false;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN && call != null && call.isMuted()) {
            call.toggleMute();
        } else if (event.getAction() == MotionEvent.ACTION_UP && !call.isMuted()) {
            call.toggleMute();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.qira.portaria.R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.qira.portaria.R.id.call:
                // custom dialog
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(com.qira.portaria.R.layout.call_address_dialog);
                dialog.setTitle("Call Someone");

                // set the custom dialog components - text, image and button
                TextView text = (TextView) dialog.findViewById(com.qira.portaria.R.id.calladdress_view);
                text.setText("Insert Contact number!");

                final EditText editText = (EditText) dialog.findViewById(com.qira.portaria.R.id.calladdress_edit);

                ImageView image = (ImageView) dialog.findViewById(com.qira.portaria.R.id.image_sip);
                image.setImageResource(com.qira.portaria.R.drawable.icon);

                Button dialogButton = (Button) dialog.findViewById(com.qira.portaria.R.id.dialogButtonCall);
                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sipAddress = editText.getText().toString();
                        initiateCall();
                        dialog.dismiss();
                    }
                });

                dialog.show();
                break;
            case com.qira.portaria.R.id.edit:
                updatePreferences();
                break;
            case com.qira.portaria.R.id.end_call:
                if (call != null) {
                    try {
                        call.endCall();
                    } catch (SipException se) {
                        Log.d("WalkieTalkieActivity/onOptionsItemSelected",
                                "Error ending call.", se);
                    }
                    callReceiver.kill();
                    call.close();
                }
                break;
        }
        return true;
    }


    public void updatePreferences() {
        Intent settingsActivity = new Intent(getBaseContext(),
                SipSettings.class);
        startActivity(settingsActivity);
    }

    private void initializeViews() {
        RelativeLayout company1 = (RelativeLayout) findViewById(R.id.coletivo);
        RelativeLayout company2 = (RelativeLayout) findViewById(R.id.nauweb);
        RelativeLayout company3 = (RelativeLayout) findViewById(R.id.prover);
        RelativeLayout company4 = (RelativeLayout) findViewById(R.id.coletivo_casa);
        RelativeLayout company5 = (RelativeLayout) findViewById(R.id.peregrino);
        RelativeLayout company6 = (RelativeLayout) findViewById(R.id.vivero);

        assert company1 != null;
        company1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReceiver.kill();
                sipAddress = "8201@172.16.100.251:5566";
                initiateCall();
            }
        });


        assert company2 != null;
        company2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReceiver.kill();
                sipAddress = "8202@172.16.100.251:5566";
                initiateCall();
            }
        });


        assert company3 != null;
        company3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReceiver.kill();
                sipAddress = "8203@172.16.100.251:5566";
                initiateCall();
            }
        });


        assert company4 != null;
        company4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReceiver.kill();
                sipAddress = "8204@172.16.100.251:5566";
                initiateCall();
            }
        });


        assert company5 != null;
        company5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReceiver.kill();
                sipAddress = "8205@172.16.100.251:5566";
                initiateCall();
            }
        });

        assert company6 != null;
        company6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReceiver.kill();
                sipAddress = "8206@172.16.100.251:5566";
                initiateCall();
            }
        });


    }

    public void checkWifiConnection(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            Toast.makeText(this,"Check Wifi connection", Toast.LENGTH_SHORT).show();

        }
    }

    public void checkIsRegistred() {


        h.postDelayed(new Runnable(){
            public void run(){
                if(!isRegistred) {
                    initializeLocalProfile();
                    checkWifiConnection();
                }
                h.postDelayed(this, DELAY);
            }
        }, DELAY);
    }
}
