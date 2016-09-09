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

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.text.ParseException;
import java.util.List;

/**
 * Handles all calling, receiving calls, and UI interaction in the WalkieTalkie app.
 */
public class WalkieTalkieActivity extends AppCompatActivity {

    public String sipAddress = null;
    public static final String ACTION_CLOSE = "com.qira.portaria.ACTION_CLOSE";
    public SipManager manager = null;
    public SipProfile me = null;
    public SipAudioCall call = null;

    public Toast toast;
    public IncomingCallReceiver callReceiver;
    public Handler h = new Handler();
    public int clicksOnLogo = 0;
    public int onresume = 0;
    public FT311UARTInterface uartinterface;

    int baudRate = 9600; /* baud rate */
    byte stopBit = 1; /* 1:1stop bits, 2:2 stop bits */
    byte dataBit= 8; /* 8:8bit, 7: 7bit */
    byte parity = 0; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
    byte flowControl = 0; /* 0:none, 1: flow control(CTS,RTS) */


    public boolean isRegistred;

    private static final String AutoOnOffCmd = "021A000000000000000000000000000000001C00";
    private static final String initializeCmd = "0288000000000000000000000000000000008A00";
    private static final int CALL_ADDRESS = 1;
    private static final int SET_AUTH_INFO = 2;
    private static final int UPDATE_SETTINGS_DIALOG = 3;
    private static final int HANG_UP = 4;
    private static final int DELAY = 3000; //milliseconds


    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(com.qira.portaria.R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Set up the intent filter.  This will be used to fire an
        // IncomingCallReceiver when someone calls the SIP address used by this
        // application.
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);



    }


    @Override
    public void onResume() {
        super.onResume();


        uartinterface = new FT311UARTInterface(getApplicationContext());
        uartinterface.SetConfig(baudRate,dataBit,stopBit,parity,flowControl);
        int result = uartinterface.ResumeAccessory();
        uartinterface.SetConfig(baudRate,dataBit,stopBit,parity,flowControl);


        Toast toast;

        if (result == 0) {
            toast = Toast.makeText(this, "Request Ok", Toast.LENGTH_SHORT);

        }
        else if (result == 1) {
            toast = Toast.makeText(this, "Error", Toast.LENGTH_SHORT);

        }
        else {
            toast = Toast.makeText(this, "Error", Toast.LENGTH_SHORT);

        }
        toast.show();

        onresume++;

        // Necessary verification to avoid multiples registrations and initializations
        if (onresume == 1) {
            initializeViews();

            initializeManager();

            checkIsRegistred();


            SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putString("user", "8200");
//            editor.putString("password", "@quaecoh6Ria@");
//            editor.putString("domain", "172.16.100.251:5566");

            editor.putString("user", "8197");
            editor.putString("password", "*Aabb44cc77!*");
            editor.putString("domain", "192.168.1.200");
            editor.apply();


            BroadcastReceiver broadcast_reciever = new BroadcastReceiver() {

                @Override
                public void onReceive(Context arg0, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals("finish_call")) {
                        try {
                            call.endCall();
                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                        callReceiver.kill();
                    }
                }
            };
            registerReceiver(broadcast_reciever, new IntentFilter("finish_call"));


        }



            h.postDelayed(new Runnable() {
                public void run() {
                    writeSerialHex(initializeCmd);

                }
            }, 5000);





        h.postDelayed(new Runnable() {
            public void run() {
                writeSerialHex(AutoOnOffCmd);
            }
        }, 7000);




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
        uartinterface.DestroyAccessory(true,true);
        if (call != null) {
            call.close();
            callReceiver.kill();
        }

        closeLocalProfile();

        if (callReceiver != null) {
            this.unregisterReceiver(callReceiver);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.qira.portaria.R.menu.menu_main, menu);
        return true;
    }


    @Override
    public void onBackPressed() {
        //DO nothing

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
        SharedPreferences sharedPreferencesState = getSharedPreferences("state", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferencesState.edit();


        if (manager == null) {
            return;
        }

        if (isRegistred)
            return;

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
                    editor.putString("registrationState", "Registering with SIP Server...");
                    isRegistred = false;
                    editor.apply();
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    editor.putString("registrationState", "Ready");
                    isRegistred = true;
                    editor.apply();
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
                    editor.putString("registrationState", "Registration failed.");
                    isRegistred = false;
                    editor.apply();

                }

            });

        } catch (ParseException pe) {
            editor.putString("registrationState", "Registration failed.");
            editor.apply();
        } catch (SipException se) {
            editor.putString("registrationState", "Registration failed.");
            editor.apply();
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


    public void initiateCall(final String roomName, final String companyName) {
        SharedPreferences sharedPreferencesState = getSharedPreferences("state", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferencesState.edit();

        editor.putString("sipAddress", sipAddress);
        editor.apply();
        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                // Much of the client's interaction with the SIP Stack will
                // happen via listeners.  Even making an outgoing call, don't
                // forget to set up a listener to set things up once the call is established.
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    call.startAudio();
                    call.setSpeakerMode(true);
                    if (call.isMuted()) {
                        call.toggleMute();
                    }
                    editor.putString("call", call.toString());
                    editor.apply();

                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    editor.putString("registrationState", "Ready");
                    callReceiver.kill();
                    editor.apply();

                    Intent intent = new Intent("finish_activity");
                    sendBroadcast(intent);

                }
            };

            call = manager.makeAudioCall(me.getUriString(), sipAddress, listener, 30);

            Intent intent = new Intent(getBaseContext(), CallAcitivity.class);
            intent.putExtra("RoomName", roomName);
            intent.putExtra("CompanyName", companyName);
            startActivity(intent);


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


    private void initializeViews() {
        ImageView logoSettings = (ImageView) findViewById(R.id.logo_settings);

        View company1 = findViewById(R.id.button_one_1);
        View company2 = findViewById(R.id.button_two);
        View company3 = findViewById(R.id.button_three);
        View company4 = findViewById(R.id.button_four);
        View company5 = findViewById(R.id.button_five);
        View company6 = findViewById(R.id.button_six);
        View lobby = findViewById(R.id.button_lobby);


        assert logoSettings != null;
        logoSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsActivity = new Intent(getBaseContext(),
                        SettingsActivity.class);
//                settingsActivity.putExtra("RoomName", "teste");
//                settingsActivity.putExtra("RoomNumber", "teste");
                clicksOnLogo++;
                if (clicksOnLogo >= 4) {
                    clicksOnLogo = 0;

                    startActivity(settingsActivity);
                }
            }
        });


        assert company1 != null;
        company1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReceiver.kill();
                if (call != null) {
                    if (call.isInCall()) {
                        try {
                            call.endCall();
                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                    }
                }
                sipAddress = "8196@192.168.1.200";
                //sipAddress = "8201@172.16.100.251:5566";
                initiateCall("Família Silva", "APARTAMENTO 101");

            }
        });


        assert company2 != null;
        company2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReceiver.kill();
                if (call != null) {
                    if (call.isInCall()) {
                        try {
                            call.endCall();
                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                    }
                }
                sipAddress = "8202@172.16.100.251:5566";
                initiateCall("Família Gontijo", "APARTAMENTO 102");
            }
        });


        assert company3 != null;
        company3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReceiver.kill();
                if (call != null) {
                    if (call.isInCall()) {
                        try {
                            call.endCall();
                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                    }
                }
                sipAddress = "8203@172.16.100.251:5566";
                initiateCall("Família Carvalho", "APARTAMENTO 201");
            }
        });


        assert company4 != null;
        company4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReceiver.kill();
                if (call != null) {
                    if (call.isInCall()) {
                        try {
                            call.endCall();
                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                    }
                }
                sipAddress = "8204@172.16.100.251:5566";
                initiateCall("Família Oliveira", "APARTAMENTO 202");
            }
        });


        assert company5 != null;
        company5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReceiver.kill();
                if (call != null) {
                    if (call.isInCall()) {
                        try {
                            call.endCall();
                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                    }
                }

                sipAddress = "8205@172.16.100.251:5566";
                initiateCall("Família Fagundes", "APARTAMENTO 301");

            }
        });

        assert company6 != null;
        company6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReceiver.kill();
                if (call != null) {
                    if (call.isInCall()) {
                        try {
                            call.endCall();
                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                    }
                }
                sipAddress = "8206@172.16.100.251:5566";
                initiateCall("Família Silva", "APARTAMENTO 302");
            }
        });


        assert lobby != null;
        lobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReceiver.kill();
                if (call != null) {
                    if (call.isInCall()) {
                        try {
                            call.endCall();
                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                    }
                }
                sipAddress = "8207@172.16.100.251:5566";
                initiateCall("", "PORTARIA");
            }
        });


    }


    public void checkIsRegistred() {
        h.postDelayed(new Runnable() {
            public void run() {
                if (!isRegistred) {
                    initializeLocalProfile();
                }
                h.postDelayed(this, DELAY);
            }
        }, DELAY);
    }


    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private void writeSerialHex(final String data) {


        if (uartinterface != null) {
            byte[] buffer = hexStringToByteArray(data);
            if (uartinterface.SendData(buffer.length, buffer) == 0) {
                toast = Toast.makeText(this, "write Ok", Toast.LENGTH_SHORT);

            } else {
                toast = Toast.makeText(this, "write Error", Toast.LENGTH_SHORT);

            }
            toast.show();
        }
    }


}
