package com.sunilpaulmathew.terminal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.snackbar.Snackbar;
import com.sunilpaulmathew.terminal.utils.Utils;

import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 25, 2020
 */

public class MainActivity extends AppCompatActivity {

    private AppCompatEditText mShellCommand;
    private AppCompatEditText mShellOutput;
    private AppCompatTextView mShellCommandTitle;
    private boolean mExit;
    private boolean mSU = false;
    private Handler mHandler = new Handler();
    private int i;
    private LinearLayout mLinearLayout;
    private String whoAmI = Utils.runCommand("whoami").replace("\n","");
    private StringBuilder mLastCommand;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme
        Utils.initializeAppTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatImageButton mEnter = findViewById(R.id.enter_button);
        AppCompatImageButton mUpButtom = findViewById(R.id.up_button);
        mShellCommand = findViewById(R.id.shell_command);
        mShellCommandTitle = findViewById(R.id.shell_command_title);
        mShellOutput = findViewById(R.id.shell_output);
        mLinearLayout = findViewById(R.id.progress_layout);
        mShellCommandTitle.setText(whoAmI);
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
                    runShellCommand(MainActivity.this);
                }
            }
        });
        mEnter.setOnClickListener(v -> runShellCommand(this));
        mUpButtom.setOnClickListener(v -> {
            String[] lines = mLastCommand.toString().split(",");
            PopupMenu popupMenu = new PopupMenu(this, mShellCommand);
            Menu menu = popupMenu.getMenu();
            if (mLastCommand.toString().isEmpty()) {
                return;
            }
            for (i = 0; i < lines.length; i++) {
                menu.add(Menu.NONE, i, Menu.NONE, lines[i]);
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                for (i = 0; i < lines.length; i++) {
                    if (item.getItemId() == i) {
                        mShellCommand.setText(lines[i]);
                    }
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @SuppressLint({"SetTextI18n", "StaticFieldLeak"})
    private void runShellCommand(Context context) {
        final String[] mResult = new String[1];
        if (mShellCommand.getText() == null || mShellCommand.getText().toString().isEmpty()) {
            return;
        }
        String[] array = Objects.requireNonNull(mShellCommand.getText()).toString().trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            if (s != null && !s.isEmpty())
                sb.append(" ").append(s);
        }
        final String[] mCommand = {sb.toString().replaceFirst(" ","")};
        mLastCommand.append(mCommand[0]).append(",");
        if (mShellCommand.getText() != null && !mCommand[0].isEmpty()) {
            if (mCommand[0].equals("clear")) {
                clearAll();
                return;
            }
            if (mCommand[0].equals("exit")) {
                if (mSU) {
                    mSU = false;
                    whoAmI = Utils.runCommand("whoami").replace("\n","");
                    mShellCommand.setText(null);
                    mShellCommandTitle.setText(whoAmI);
                } else {
                    mShellCommand.setText(null);
                    new AlertDialog.Builder(this)
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
            if (mCommand[0].equals("su") || mCommand[0].startsWith("su ")) {
                if (mSU && Utils.rootAccess()) {
                    mShellCommand.setText(null);
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.root_status_available)
                            .setPositiveButton(R.string.cancel, (dialog, which) -> {
                            })
                            .show();
                    return;
                } else if (Utils.rootAccess()) {
                    mSU = true;
                    mShellCommand.setText(null);
                    whoAmI = Utils.runRootCommand("whoami").replace("\n","");
                    mShellCommandTitle.setText(whoAmI);
                    return;
                } else {
                    mShellCommand.setText(null);
                    new AlertDialog.Builder(this)
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
                mLinearLayout.setVisibility(View.VISIBLE);
            }
            @SuppressLint("WrongThread")
            @Override
            protected Void doInBackground(Void... voids) {
                if (mShellCommand.getText() != null && !mCommand[0].isEmpty()) {
                    mResult[0] = whoAmI + ": " + mCommand[0] + "\n" + (mSU ?
                            Utils.runRootCommand(mCommand[0]) : Utils.runCommand(mCommand[0]));
                    if (mResult[0].equals(whoAmI + ": " + mCommand[0] + "\n")) {
                        mResult[0] = whoAmI + ": " + mCommand[0] + "\n" + mCommand[0];
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mLinearLayout.setVisibility(View.GONE);
                mShellCommand.setText(null);
                mShellOutput.setText(mResult[0] + "\n\n" + mShellOutput.getText());
                mShellOutput.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(mShellCommand, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.dismiss, v -> snackbar.dismiss());
        snackbar.show();
    }

    private void clearAll() {
        mShellOutput.setText(null);
        mShellOutput.setVisibility(View.GONE);
        mShellCommand.setText(null);
    }

    @Override
    public void onStart() {
        super.onStart();

        mLastCommand = new StringBuilder();
    }

    @Override
    public void onBackPressed() {
        if (mExit) {
            mExit = false;
            super.onBackPressed();
        } else {
            showSnackbar(getString(R.string.press_back));
            mExit = true;
            mHandler.postDelayed(() -> mExit = false, 2000);
        }
    }

}