package com.sunilpaulmathew.terminal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sunilpaulmathew.terminal.activities.AboutActivity;
import com.sunilpaulmathew.terminal.activities.LicenceActivity;
import com.sunilpaulmathew.terminal.activities.ManualActivity;
import com.sunilpaulmathew.terminal.fragments.TerminalFragment;
import com.sunilpaulmathew.terminal.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 25, 2020
 */

public class MainActivity extends AppCompatActivity {

    private boolean mExit;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme
        Utils.initializeAppTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatImageButton mMenu = findViewById(R.id.menu_button);

        mMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mMenu);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, R.string.manual);
            menu.add(Menu.NONE, 1, Menu.NONE, R.string.source_code);
            menu.add(Menu.NONE, 2, Menu.NONE, R.string.licence);
            menu.add(Menu.NONE, 3, Menu.NONE, R.string.about);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        Intent manual = new Intent(this, ManualActivity.class);
                        startActivity(manual);
                        break;
                    case 1:
                        Utils.launchUrl("https://github.com/sunilpaulmathew/SimpleTerminal", this);
                        break;
                    case 2:
                        Intent licence = new Intent(this, LicenceActivity.class);
                        startActivity(licence);
                        break;
                    case 3:
                        Intent about = new Intent(this, AboutActivity.class);
                        startActivity(about);
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new TerminalFragment()).commit();
    }

    private void close() {
        Utils.closeSU();
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (Utils.mRunning) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.stop_command_question, Utils.mCommand))
                    .setNegativeButton(getString(R.string.cancel), (dialog1, id1) -> {
                    })
                    .setPositiveButton(getString(R.string.exit), (dialog1, id1) -> {
                        close();
                    }).show();
            return;
        }
        if (mExit) {
            mExit = false;
            super.onBackPressed();
        } else {
            Utils.showSnackbar(findViewById(android.R.id.content), getString(R.string.press_back));
            mExit = true;
            mHandler.postDelayed(() -> mExit = false, 2000);
        }
    }

}