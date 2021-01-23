package com.sunilpaulmathew.terminal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sunilpaulmathew.terminal.activities.SettingsActivity;
import com.sunilpaulmathew.terminal.adapters.PagerAdapter;
import com.sunilpaulmathew.terminal.fragments.TerminalFragment;
import com.sunilpaulmathew.terminal.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 25, 2020
 */

public class MainActivity extends AppCompatActivity {

    private int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme
        Utils.initializeAppTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatImageButton mMenu = findViewById(R.id.menu_button);
        AppCompatImageButton mAdd = findViewById(R.id.add_button);
        TabLayout mTabLayout = findViewById(R.id.tab_Layout);
        ViewPager mViewPager = findViewById(R.id.view_pager);

        mMenu.setOnClickListener(v -> {
            Intent manual = new Intent(this, SettingsActivity.class);
            startActivity(manual);
        });

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new TerminalFragment(), getString(R.string.tab_count, String.valueOf(i)));
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);

        mAdd.setOnClickListener(v -> {
            i = ++i;
            if (i > 1 && mTabLayout.getVisibility() == View.GONE) {
                mTabLayout.setVisibility(View.VISIBLE);
            }
            adapter.AddFragment(new TerminalFragment(), getString(R.string.tab_count, String.valueOf(i)));
            mViewPager.setAdapter(adapter);
        });
    }

}