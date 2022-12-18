package com.sunilpaulmathew.terminal.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sunilpaulmathew.terminal.R;
import com.sunilpaulmathew.terminal.adapters.ShellOutputAdapter;
import com.sunilpaulmathew.terminal.utils.Utils;
import com.sunilpaulmathew.terminal.utils.sShell;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 05, 2021
 */

public class TerminalFragment extends Fragment {

    private AppCompatAutoCompleteTextView mShellCommand;
    private AppCompatImageButton mUpButtom;
    private boolean mExit, mSU = false;
    private final Handler mHandler = new Handler();
    private int i;
    private List<String> mLastCommand = null, mResult;
    private RecyclerView mRecyclerView;
    private sShell mShell = null;
    private ShellOutputAdapter mShellOutputAdapter = null;
    private String mCommand, mUser = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_terminal, container, false);

        mUpButtom = mRootView.findViewById(R.id.up_button);
        mShellCommand = mRootView.findViewById(R.id.shell_command);
        MaterialCardView mSendButton = mRootView.findViewById(R.id.send_button);

        mShellCommand.requestFocus();

        mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mShell = new sShell(mResult, null);
        File mHome = requireActivity().getExternalFilesDir("home");
        if (!mHome.exists()) {
            mHome.mkdirs();
        }
        mUser = mShell.getUserName();

        mLastCommand = new ArrayList<>();

        mShellCommand.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    if (mLastCommand.size() > 0) {
                        mUpButtom.setImageDrawable(Utils.getDrawable(R.drawable.ic_arrow_up, requireActivity()));
                    }
                } else {
                    mUpButtom.setImageDrawable(Utils.getDrawable(R.drawable.ic_send, requireActivity()));
                    if (s.toString().contains("\n")) {
                        if (!s.toString().endsWith("\n")) {
                            mShellCommand.setText(s.toString().replace("\n", ""));
                        }
                        runShellCommand(requireActivity());
                    }
                }
            }
        });

        mSendButton.setOnClickListener(v -> {
            if (mResult != null && mResult.size() > 0 && !mResult.get(mResult.size() - 1).equals("sTerminal: Finish")) {
                mShell.destroy();
            } else if (mShellCommand.getText() != null && !mShellCommand.getText().toString().isEmpty()) {
                runShellCommand(requireActivity());
            } else if (mLastCommand.size() > 0) {
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
            }
        });

        Thread mRefreshThread = new RefreshThread();
        mRefreshThread.start();

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mResult != null && mResult.size() > 0 && !mResult.get(mResult.size() - 1).equals("sTerminal: Finish")) {
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(getString(R.string.stop_command_question, mCommand))
                            .setNegativeButton(getString(R.string.cancel), (dialog1, id1) -> {
                            })
                            .setPositiveButton(getString(R.string.exit), (dialog1, id1) -> {
                                if (mSU) {
                                    try {
                                        Objects.requireNonNull(Shell.getCachedShell()).close();
                                    } catch (IOException ignored) {
                                    }
                                } else {
                                    mShell.destroy();
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
                        if (mResult != null && mResult.size() > 0 && !mResult.get(mResult.size() - 1).equals("sTerminal: Finish")) {
                            try {
                                updateUI(mResult);
                            } catch (ConcurrentModificationException ignored) {
                            }
                        }
                    });
                }
            } catch (InterruptedException | IllegalStateException ignored) {}
        }
    }

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
                    mShellCommand.setText(null);
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
            if (mCommand.startsWith("logcat") && !Shell.rootAccess() && activity.checkCallingOrSelfPermission(
                    Manifest.permission.READ_LOGS) != PackageManager.PERMISSION_GRANTED) {
                new MaterialAlertDialogBuilder(activity)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.logcat_permission_message)
                        .setPositiveButton(R.string.cancel, (dialog, which) -> {
                        })
                        .show();
            }
            if (mCommand.equals("su") || mCommand.startsWith("su ")) {
                if (mSU && Shell.rootAccess()) {
                    mShellCommand.setText(null);
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.root_status_available)
                            .setPositiveButton(R.string.cancel, (dialog, which) -> {
                            })
                            .show();
                    return;
                } else if (Shell.rootAccess()) {
                    mSU = true;
                    mShellCommand.setText(null);
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

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        if (mResult == null) mResult = new ArrayList<>();
        mShellCommand.setText(null);
        mShellCommand.clearFocus();

        ExecutorService mExecutors = Executors.newSingleThreadExecutor();
        mExecutors.execute(() -> {
            if (mShellCommand.getText() != null && !mCommand.isEmpty()) {
                mResult.add(mUser + ": " + mCommand);
                if (mSU) {
                    List<String> mError = new ArrayList<>();
                    Shell.cmd(mCommand).to(mResult, mError).exec();
                    for (String errorString : mError) {
                        mResult.add("<font color=#FF0000>" + errorString + "</font>");
                    }
                } else {
                    mShell = new sShell(mResult, mCommand);
                    mShell.exec();
                }
                mResult.add("<i></i>");
                mResult.add("sTerminal: Finish");
            }

            try {
                TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException ignored) {}

            new Handler(Looper.getMainLooper()).post(() -> {
                if (mResult != null) {
                    updateUI(mResult);
                }
                if (mLastCommand.size() > 0) {
                    mUpButtom.setImageDrawable(Utils.getDrawable(R.drawable.ic_arrow_up, requireActivity()));
                    mUpButtom.setColorFilter(Utils.getColor(R.color.colorWhite, requireActivity()));
                }
                mShellCommand.requestFocus();
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            });
            if (!mExecutors.isShutdown()) mExecutors.shutdown();
        });
    }

    private void updateUI(List<String> data) {
        List<String> mData = new ArrayList<>();
        try {
            for (String result : data) {
                if (!result.trim().isEmpty() && !result.equals("sTerminal: Finish")) {
                    mData.add(result);
                }
            }
        } catch (ConcurrentModificationException ignored) {
        }

        mUpButtom.setImageDrawable(Utils.getDrawable(R.drawable.ic_stop, requireActivity()));
        mUpButtom.setColorFilter(Utils.getColor(R.color.ColorRed, requireActivity()));

        ExecutorService mExecutors = Executors.newSingleThreadExecutor();
        mExecutors.execute(() -> {
            mShellOutputAdapter = new ShellOutputAdapter(mData);
            new Handler(Looper.getMainLooper()).post(() -> {
                mRecyclerView.setAdapter(mShellOutputAdapter);
                mRecyclerView.scrollToPosition(mData.size() - 1);
                if (mResult != null && mRecyclerView.getVisibility() == View.GONE) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            });
            if (!mExecutors.isShutdown()) mExecutors.shutdown();
        });
    }

    private void clearAll() {
        mResult = null;
        mShellCommand.setText(null);
        mRecyclerView.setVisibility(View.GONE);
    }

}