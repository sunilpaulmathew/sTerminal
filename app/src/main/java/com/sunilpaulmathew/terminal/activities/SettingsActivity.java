package com.sunilpaulmathew.terminal.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.sunilpaulmathew.terminal.BuildConfig;
import com.sunilpaulmathew.terminal.R;
import com.sunilpaulmathew.terminal.adapters.RecycleViewAdapter;
import com.sunilpaulmathew.terminal.utils.RecycleViewItem;
import com.sunilpaulmathew.terminal.utils.Utils;

import java.util.ArrayList;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 23, 2021
 */

public class SettingsActivity extends AppCompatActivity {

    private ArrayList <RecycleViewItem> mData = new ArrayList<>();

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        AppCompatImageButton mBack = findViewById(R.id.back_button);
        LinearLayout mAppInfo = findViewById(R.id.app_info);
        MaterialTextView mAppTitle = findViewById(R.id.title);
        MaterialTextView mAppDescription = findViewById(R.id.description);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mAppTitle.setText(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        mAppTitle.setTextColor(Utils.isDarkTheme(this) ? Color.WHITE : Color.BLACK);
        mAppDescription.setText(BuildConfig.APPLICATION_ID);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        RecycleViewAdapter mRecycleViewAdapter = new RecycleViewAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mAppInfo.setOnClickListener(v -> Utils.goToSettings(this));

        mData.add(new RecycleViewItem(null, getString(R.string.app_info), null));
        mData.add(new RecycleViewItem(getString(R.string.source_code), getString(R.string.source_code_summary), getResources().getDrawable(R.drawable.ic_github)));
        mData.add(new RecycleViewItem(getString(R.string.manual), getString(R.string.manual_summary), getResources().getDrawable(R.drawable.ic_manual)));
        mData.add(new RecycleViewItem(getString(R.string.support_development), getString(R.string.support_development_summary), getResources().getDrawable(R.drawable.ic_donate)));
        mData.add(new RecycleViewItem(getString(R.string.invite_friends), getString(R.string.invite_friends), getResources().getDrawable(R.drawable.ic_share)));
        mData.add(new RecycleViewItem(getString(R.string.translations), getString(R.string.translations_summary), getResources().getDrawable(R.drawable.ic_translate)));
        mData.add(new RecycleViewItem(getString(R.string.licence), getString(R.string.licence_summary), getResources().getDrawable(R.drawable.ic_licence)));
        mData.add(new RecycleViewItem(getString(R.string.about), getString(R.string.about_summary), getResources().getDrawable(R.drawable.ic_info)));

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (position == 0) {
                Utils.goToSettings(this);
            } else if (position == 1) {
                Utils.launchUrl("https://github.com/sunilpaulmathew/sTerminal/", this);
            } else if (position == 2) {
                Intent manual = new Intent(this, ManualActivity.class);
                startActivity(manual);
            } else if (position == 3) {
                Utils.launchUrl("https://smartpack.github.io/donation/", this);
            } else if (position == 4) {
                Intent share_app = new Intent();
                share_app.setAction(Intent.ACTION_SEND);
                share_app.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                share_app.putExtra(Intent.EXTRA_TEXT, getString(R.string.shared_by_message, BuildConfig.VERSION_NAME));
                share_app.setType("text/plain");
                Intent shareIntent = Intent.createChooser(share_app, getString(R.string.share_with));
                startActivity(shareIntent);
            } else if (position == 5) {
                Utils.launchUrl("https://github.com/sunilpaulmathew/sTerminal/blob/main/app/src/main/res/values/strings.xml", this);
            } else if (position == 6) {
                Intent licence = new Intent(this, LicenceActivity.class);
                startActivity(licence);
            } else if (position == 7) {
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
            }
        });

        mBack.setOnClickListener(v -> super.onBackPressed());
    }

}