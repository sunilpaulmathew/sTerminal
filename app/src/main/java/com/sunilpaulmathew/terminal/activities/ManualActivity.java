package com.sunilpaulmathew.terminal.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sunilpaulmathew.terminal.R;
import com.sunilpaulmathew.terminal.fragments.ManualFragment;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on December 29, 2020
 */

public class ManualActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new ManualFragment()).commit();
    }

}