package com.sunilpaulmathew.terminal.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.sunilpaulmathew.terminal.R;
import com.sunilpaulmathew.terminal.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 05, 2021
 */

public class TerminalFragment extends Fragment {

    private AppCompatAutoCompleteTextView mShellCommand;
    private AppCompatImageButton mUpButtom;
    private boolean mExit, mRunning = false, mSU = false;
    private CharSequence mHistory = null;
    private final Handler mHandler = new Handler();
    private MaterialTextView mShellCommandTitle, mShellOutput;
    private int i;
    private List<String> mLastCommand = null, PWD = null, mResult, whoAmI = null;
    private NestedScrollView mScrollView;
    private String mCommand;

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_terminal, container, false);

        mUpButtom = mRootView.findViewById(R.id.up_button);
        mShellCommand = mRootView.findViewById(R.id.shell_command);
        mShellCommandTitle = mRootView.findViewById(R.id.shell_command_title);
        mShellOutput = mRootView.findViewById(R.id.shell_output);
        mScrollView = mRootView.findViewById(R.id.scroll_view);

        mShellCommand.requestFocus();
        mLastCommand = new ArrayList<>();
        whoAmI = new ArrayList<>();
        PWD = new ArrayList<>();
        requireActivity().getExternalFilesDir("home").mkdirs();
        whoAmI = Utils.runCommand("whoami");
        PWD = Utils.runCommand("pwd");
        mShellCommandTitle.setText(Utils.getOutput(whoAmI) + ": " + Utils.getOutput(PWD));

        mShellCommand.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (mRunning) return;
                if (s.toString().contains("\n")) {
                    runShellCommand(requireActivity());
                }
            }
        });

        mUpButtom.setOnClickListener(v -> {
            List<String> mRecentCommands = new ArrayList<>();
            for (i = 0; i < mLastCommand.size(); i++) {
                mRecentCommands.add(mLastCommand.get(i));
            }
            Collections.reverse(mRecentCommands);
            PopupMenu popupMenu = new PopupMenu(requireContext(), mShellCommand);
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
                        mShellCommand.setSelection(mShellCommand.getText().length());
                    }
                }
                return false;
            });
            popupMenu.show();
        });

        Thread mRefreshThread = new RefreshThread();
        mRefreshThread.start();

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mRunning) {
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(getString(R.string.stop_command_question, mCommand))
                            .setNegativeButton(getString(R.string.cancel), (dialog1, id1) -> {
                            })
                            .setPositiveButton(getString(R.string.exit), (dialog1, id1) -> {
                                if (mSU) {
                                    Utils.closeSU();
                                } else {
                                    Utils.destroyProcess();
                                }
                                this.remove();
                                requireActivity().onBackPressed();
                            }).show();
                    return;
                }
                if (mExit) {
                    mExit = false;
                    this.remove();
                    requireActivity().onBackPressed();
                } else {
                    Utils.showSnackbar(mRootView, getString(R.string.press_back));
                    mExit = true;
                    mHandler.postDelayed(() -> mExit = false, 2000);
                }
            }
        });

        return mRootView;
    }

    private class RefreshThread extends Thread {
        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Thread.sleep(250);
                    requireActivity().runOnUiThread(() -> {
                        if (mRunning) {
                            mShellOutput.setTextIsSelectable(false);
                            mScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
                            if (!mCommand.startsWith("sleep") && mResult.isEmpty()) {
                                if (mSU) {
                                    Utils.closeSU();
                                } else {
                                    Utils.destroyProcess();
                                }
                            } else {
                                try {
                                    mShellOutput.setText(Utils.getOutput(mResult));
                                } catch (ConcurrentModificationException | NullPointerException ignored) {}
                            }
                        } else {
                            mShellOutput.setTextIsSelectable(true);
                        }
                    });
                }
            } catch (InterruptedException ignored) {}
        }
    }

    @SuppressLint({"SetTextI18n", "StaticFieldLeak"})
    private void runShellCommand(Activity activity) {
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
                    whoAmI = Utils.runCommand("whoami");
                    mShellCommand.setText(null);
                    mShellCommandTitle.setText(Utils.getOutput(whoAmI) + ": " + Utils.getOutput(PWD));
                } else {
                    mShellCommand.setText(null);
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.exit_confirmation)
                            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            })
                            .setPositiveButton(R.string.exit, (dialog, which) -> activity.onBackPressed())
                            .show();
                }
                return;
            }
            if (mCommand.equals("su") || mCommand.startsWith("su ")) {
                if (mSU && Utils.rootAccess()) {
                    mShellCommand.setText(null);
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.root_status_available)
                            .setPositiveButton(R.string.cancel, (dialog, which) -> {
                            })
                            .show();
                    return;
                } else if (Utils.rootAccess()) {
                    mSU = true;
                    mShellCommand.setText(null);
                    whoAmI = new ArrayList<>();
                    whoAmI = Utils.runRootCommand("whoami");
                    mShellCommandTitle.setText(Utils.getOutput(whoAmI) + ": " + Utils.getOutput(PWD));
                    return;
                } else {
                    mShellCommand.setText(null);
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.root_status_unavailable)
                            .setPositiveButton(R.string.cancel, (dialog, which) -> {
                            })
                            .show();
                    return;
                }
            }
        }

        mRunning = true;
        mHistory = mShellOutput.getText();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mResult = new ArrayList<>();
        PWD = new ArrayList<>();
        mShellCommand.setText(null);
        mShellOutput.setVisibility(View.VISIBLE);

        ExecutorService mExecutors = Executors.newSingleThreadExecutor();
        mExecutors.execute(() -> {
            if (mShellCommand.getText() != null && !mCommand.isEmpty()) {
                mResult.add(whoAmI + ": " + mCommand);
                if (mSU) {
                    mResult = Utils.runRootCommand(mCommand);
                    PWD = Utils.runRootCommand("pwd");
                } else {
                    mResult = Utils.runCommand(mCommand);
                    PWD = Utils.runCommand("pwd");
                }
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                if (mResult != null) {
                    if (mHistory != null && !mHistory.toString().isEmpty()) {
                        mShellOutput.setText(mHistory + "\n\n" + Utils.getOutput(mResult));
                    } else {
                        mShellOutput.setText(Utils.getOutput(mResult));
                    }
                }
                mShellCommandTitle.setText(Utils.getOutput(whoAmI) + ": " + Utils.getOutput(PWD));
                mHistory = null;
                mRunning = false;
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                if (mLastCommand.size() > 0) {
                    mUpButtom.setVisibility(View.VISIBLE);
                }
            });
            if (!mExecutors.isShutdown()) mExecutors.shutdown();
        });
    }

    private void clearAll() {
        mShellOutput.setText(null);
        mShellOutput.setVisibility(View.GONE);
        mResult.clear();
        mShellCommand.setText(null);
    }

}