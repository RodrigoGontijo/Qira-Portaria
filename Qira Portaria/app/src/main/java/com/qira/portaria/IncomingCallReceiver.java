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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipProfile;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Listens for incoming SIP calls, intercepts and hands them off to WalkieTalkieActivity.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    private boolean bKeepRunning = true;
    private String lastMessage = "";
    private SipAudioCall incomingCall = null;
    private DatagramSocket socket;


    /**
     * Processes the incoming call, answers it, and hands it over to the
     * WalkieTalkieActivity.
     *
     * @param context The context under which the receiver is running.
     * @param intent  The intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {


        try {

            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    try {
                        call.answerCall(30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };


            WalkieTalkieActivity wtActivity = (WalkieTalkieActivity) context;

            incomingCall = wtActivity.manager.takeAudioCall(intent, listener);
            incomingCall.answerCall(30);
            incomingCall.startAudio();
            incomingCall.setSpeakerMode(true);
            if (incomingCall.isMuted()) {
                incomingCall.toggleMute();
            }

            wtActivity.call = incomingCall;


            run(incomingCall);
            //wtActivity.updateStatus(incomingCall);


        } catch (Exception e) {
            if (incomingCall != null) {
                incomingCall.close();
                try {
                    incomingCall.endCall();
                } catch (SipException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    public void run(final SipAudioCall sipAudioCall) {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String message;
                    byte[] lmessage = new byte[1500];
                    DatagramPacket packet = new DatagramPacket(lmessage, lmessage.length);

                    socket = new DatagramSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(15000));
                    socket.setBroadcast(true);



                    try {
                        while (bKeepRunning) {

                            if (sipAudioCall.isInCall()) {
                                socket.receive(packet);
                                message = new String(lmessage, 0, packet.getLength());
                                lastMessage = message;
                                Log.d("Recebeu:", message);
                            } else {
                                kill();
                                sipAudioCall.endCall();
                                socket.close();
                                return;
                            }

                        }
                    } catch (Throwable e) {
                        e.printStackTrace();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    socket.close();
                }
            }
        });

        thread.start();


    }

    public void kill() {
        bKeepRunning = false;

    }

    public String getLastMessage() {
        return lastMessage;
    }


}
