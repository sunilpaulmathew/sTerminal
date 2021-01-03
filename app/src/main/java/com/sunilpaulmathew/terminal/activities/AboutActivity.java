package com.sunilpaulmathew.terminal.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.textview.MaterialTextView;
import com.sunilpaulmathew.terminal.BuildConfig;
import com.sunilpaulmathew.terminal.R;
import com.sunilpaulmathew.terminal.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on December 29, 2020
 */

public class AboutActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageView mDeveloper = findViewById(R.id.developer);
        MaterialTextView mChangeLog = findViewById(R.id.change_log);
        MaterialTextView mTitle = findViewById(R.id.app_title);
        MaterialTextView mCancel = findViewById(R.id.cancel_button);
        mTitle.setText(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        String change_log = null;
        try {
            change_log = new JSONObject(Objects.requireNonNull(Utils.readAssetFile(
                    this, "changelog.json"))).getString("changeLogs");
        } catch (JSONException ignored) {
        }
        mChangeLog.setText(change_log);
        mDeveloper.setOnClickListener(v -> {
            Utils.launchUrl("https://github.com/sunilpaulmathew", this);
        });
        mCancel.setOnClickListener(v -> {
            onBackPressed();
        });
        mBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

}