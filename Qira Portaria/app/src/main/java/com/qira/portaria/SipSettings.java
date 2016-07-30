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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Handles SIP authentication settings for the Walkie Talkie app.
 */
public class SipSettings extends AppCompatActivity {

    private EditText user;
    private EditText password;
    private EditText domain;
    private Button saveSettings;
    private SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Note that none of the preferences are actually defined here.
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences("settings", MODE_PRIVATE);
        setContentView(com.qira.portaria.R.layout.sip_settings);


        user = (EditText) findViewById(com.qira.portaria.R.id.sip_username);
        password = (EditText) findViewById(com.qira.portaria.R.id.sip_password);
        domain = (EditText) findViewById(com.qira.portaria.R.id.sip_domain);
        saveSettings = (Button) findViewById(com.qira.portaria.R.id.save_button);

        user.setText(sharedPref.getString(getResources().getString(com.qira.portaria.R.string.user), ""));
        password.setText(sharedPref.getString(getResources().getString(com.qira.portaria.R.string.password), ""));
        domain.setText(sharedPref.getString(getResources().getString(com.qira.portaria.R.string.domain), ""));


        saveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getResources().getString(com.qira.portaria.R.string.user), user.getText().toString());
                editor.putString(getResources().getString(com.qira.portaria.R.string.password), password.getText().toString());
                editor.putString(getResources().getString(com.qira.portaria.R.string.domain), domain.getText().toString());

                editor.commit();

                Toast toast = Toast.makeText(getApplicationContext(), "Settings updated", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }
}
