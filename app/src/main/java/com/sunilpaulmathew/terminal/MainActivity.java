package com.sunilpaulmathew.terminal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.sunilpaulmathew.terminal.activities.AboutActivity;
import com.sunilpaulmathew.terminal.activities.LicenceActivity;
import com.sunilpaulmathew.terminal.activities.ManualActivity;
import com.sunilpaulmathew.terminal.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 25, 2020
 */

public class MainActivity extends AppCompatActivity {

    private AppCompatEditText mShellCommand;
    private AppCompatImageButton mUpButtom;
    private MaterialTextView mShellCommandTitle, mShellOutput;
    private boolean mExit, mSU = false, mRunning = false;
    private CharSequence mHistory = null;
    private Handler mHandler = new Handler();
    private int i;
    private List<String> mLastCommand = null, mResult = null, PWD = null, whoAmI = null;
    private NestedScrollView mScrollView;
    private String mCommand;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme
        Utils.initializeAppTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatImageButton mMenu = findViewById(R.id.menu_button);
        mUpButtom = findViewById(R.id.up_button);
        mShellCommand = findViewById(R.id.shell_command);
        mShellCommandTitle = findViewById(R.id.shell_command_title);
        mShellOutput = findViewById(R.id.shell_output);
        mScrollView = findViewById(R.id.scroll_view);

        mShellCommand.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().contains("\n")) {
                    runShellCommand();
                    mShellOutput.setVisibility(View.VISIBLE);
                }
            }
        });

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

        mUpButtom.setOnClickListener(v -> {
            List<String> mRecentCommands = new ArrayList<>();
            for (i = 0; i < mLastCommand.size(); i++) {
                mRecentCommands.add(mLastCommand.get(i));
            }
            Collections.reverse(mRecentCommands);
            PopupMenu popupMenu = new PopupMenu(this, mShellCommand);
            Menu menu = popupMenu.getMenu();
            if (mLastCommand.size() == 0) {
                return;
            }
            for (i = 0; i < mRecentCommands.size(); i++) {
                menu.add(Menu.NONE, i, Menu.NONE, mRecentCommands.get(i));
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                for (i = 0; i < mRecentCommands.size(); i++) {
                    if (item.getItemId() == i) {
                        mShellCommand.setText(mRecentCommands.get(i));
                    }
                }
                return false;
            });
            popupMenu.show();
        });

        refreshStatus();
    }

    public void refreshStatus() {
        new Thread() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(() -> {
                            if (mRunning) {
                                mShellOutput.setTextIsSelectable(false);
                                mScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
                                try {
                                    mShellOutput.setText(Utils.getOutput(mResult));
                                } catch (ConcurrentModificationException | NullPointerException ignored) {
                                }
                            } else {
                                mShellOutput.setTextIsSelectable(true);
                            }
                        });
                    }
                } catch (InterruptedException ignored) {}
            }
        }.start();
    }

    @SuppressLint({"SetTextI18n", "StaticFieldLeak"})
    private void runShellCommand() {
        if (mShellCommand.getText() == null || mShellCommand.getText().toString().isEmpty()) {
            return;
        }
        String[] array = mShellCommand.getText().toString().trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            if (s != null && !s.isEmpty())
                sb.append(" ").append(s);
        }
        mCommand = sb.toString().replaceFirst(" ", "");
        mLastCommand.add(mCommand);
        if (mShellCommand.getText() != null && !mCommand.isEmpty()) {
            if (mCommand.equals("clear")) {
                clearAll();
                return;
            }
            if (mCommand.equals("exit")) {
                if (mSU) {
                    mSU = false;
                    whoAmI = new ArrayList<>();
                    Utils.runCommand("whoami", whoAmI);
                    mShellCommand.setText(null);
                    mShellCommandTitle.setText(Utils.getOutput(whoAmI) + ": " + Utils.getOutput(PWD));
                } else {
                    mShellCommand.setText(null);
                    new MaterialAlertDialogBuilder(this)
                            .setMessage(R.string.exit_confirmation)
                            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            })
                            .setPositiveButton(R.string.exit, (dialog, which) -> {
                                super.onBackPressed();
                            })
                            .show();
                }
                return;
            }
            if (mCommand.equals("su") || mCommand.startsWith("su ")) {
                if (mSU && Utils.rootAccess()) {
                    mShellCommand.setText(null);
                    new MaterialAlertDialogBuilder(this)
                            .setMessage(R.string.root_status_available)
                            .setPositiveButton(R.string.cancel, (dialog, which) -> {
                            })
                            .show();
                    return;
                } else if (Utils.rootAccess()) {
                    mSU = true;
                    mShellCommand.setText(null);
                    whoAmI = new ArrayList<>();
                    Utils.runRootCommand("whoami", whoAmI);
                    mShellCommandTitle.setText(Utils.getOutput(whoAmI) + ": " + Utils.getOutput(PWD));
                    return;
                } else {
                    mShellCommand.setText(null);
                    new MaterialAlertDialogBuilder(this)
                            .setMessage(R.string.root_status_unavailable)
                            .setPositiveButton(R.string.cancel, (dialog, which) -> {
                            })
                            .show();
                    return;
                }
            }
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mRunning = true;
                mHistory = mShellOutput.getText();
                mResult = new ArrayList<>();
                PWD = new ArrayList<>();
                mShellCommand.setText(null);
            }
            @SuppressLint("WrongThread")
            @Override
            protected Void doInBackground(Void... voids) {
                if (mShellCommand.getText() != null && !mCommand.isEmpty()) {
                    mResult.add(whoAmI + ": " + mCommand);
                    if (mSU) {
                        Utils.runRootCommand(mCommand, mResult);
                        Utils.runRootCommand("pwd", PWD);
                    } else {
                        Utils.runCommand(mCommand, mResult);
                        Utils.runCommand("pwd", PWD);
                    }
                    if (Utils.getOutput(mResult).equals(whoAmI + ": " + mCommand + "\n")) {
                        mResult.add(whoAmI + ": " + mCommand + "\n" + mCommand);
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mShellCommand.requestFocus();
                if (mHistory != null && !mHistory.toString().isEmpty()) {
                    mShellOutput.setText(mHistory + "\n\n" + Utils.getOutput(mResult));
                } else {
                    mShellOutput.setText(Utils.getOutput(mResult));
                }
                mShellCommandTitle.setText(Utils.getOutput(whoAmI) + ": " + Utils.getOutput(PWD));
                mHistory = null;
                mRunning = false;
                mShellOutput.setVisibility(View.VISIBLE);
                if (mLastCommand.size() > 0) {
                    mUpButtom.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    private void close() {
        Utils.closeSU();
        super.onBackPressed();
    }

    private void clearAll() {
        mShellOutput.setText(null);
        mShellOutput.setVisibility(View.GONE);
        mShellCommand.setText(null);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStart() {
        super.onStart();

        mShellCommand.requestFocus();
        mLastCommand = new ArrayList<>();
        whoAmI = new ArrayList<>();
        PWD = new ArrayList<>();
        Utils.runCommand("whoami", whoAmI);
        Utils.runCommand("pwd", PWD);
        mShellCommandTitle.setText(Utils.getOutput(whoAmI) + ": " + Utils.getOutput(PWD));
    }

    @Override
    public void onBackPressed() {
        if (mRunning) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.stop_command_question, mCommand))
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