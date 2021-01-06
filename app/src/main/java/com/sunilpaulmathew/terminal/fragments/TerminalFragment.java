package com.sunilpaulmathew.terminal.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.sunilpaulmathew.terminal.R;
import com.sunilpaulmathew.terminal.adapters.RecycleViewAdapter;
import com.sunilpaulmathew.terminal.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 05, 2021
 */

public class TerminalFragment extends Fragment {

    private AppCompatEditText mShellCommand;
    private AppCompatImageButton mUpButtom;
    private boolean mExit, mRunning = false, mSU = false;
    private Handler mHandler = new Handler();
    private MaterialTextView mShellCommandTitle;
    private int i;
    private List<String> mHistory = new ArrayList<>(), mLastCommand = null, PWD = null, mResult, whoAmI = null;
    private NestedScrollView mScrollView;
    private RecyclerView mRecyclerView;
    private String mCommand;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_terminal, container, false);

        mUpButtom = mRootView.findViewById(R.id.up_button);
        mShellCommand = mRootView.findViewById(R.id.shell_command);
        mShellCommandTitle = mRootView.findViewById(R.id.shell_command_title);
        mScrollView = mRootView.findViewById(R.id.scroll_view);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

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
                    }
                }
                return false;
            });
            popupMenu.show();
        });

        refreshStatus(requireActivity());

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mRunning) {
                    new MaterialAlertDialogBuilder(requireActivity())
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

    private List<String> getData() {
        List<String> mData = new ArrayList<>();
        mData.addAll(mHistory);
        mData.addAll(mResult);
        return mData;
    }

    public void refreshStatus(Activity activity) {
        new Thread() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        activity.runOnUiThread(() -> {
                            if (mRunning) {
                                mScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
                                if (!mCommand.startsWith("sleep") && mResult.isEmpty()) {
                                    if (mSU) {
                                        Utils.closeSU();
                                    } else {
                                        Utils.destroyProcess();
                                    }
                                }
                            }
                            try {
                                RecycleViewAdapter mRecycleViewAdapter = new RecycleViewAdapter(getData());
                                mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
                                mRecyclerView.setAdapter(mRecycleViewAdapter);
                            } catch (NullPointerException ignored) {}
                        });
                    }
                } catch (InterruptedException ignored) {}
            }
        }.start();
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
                    Utils.runCommand("whoami", whoAmI);
                    mShellCommand.setText(null);
                    mShellCommandTitle.setText(Utils.getOutput(whoAmI) + ": " + Utils.getOutput(PWD));
                } else {
                    mShellCommand.setText(null);
                    new MaterialAlertDialogBuilder(activity)
                            .setMessage(R.string.exit_confirmation)
                            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            })
                            .setPositiveButton(R.string.exit, (dialog, which) -> {
                                activity.onBackPressed();
                            })
                            .show();
                }
                return;
            }
            if (mCommand.equals("su") || mCommand.startsWith("su ")) {
                if (mSU && Utils.rootAccess()) {
                    mShellCommand.setText(null);
                    new MaterialAlertDialogBuilder(activity)
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
                    new MaterialAlertDialogBuilder(activity)
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
                try {
                    mHistory.addAll(mResult);
                } catch (NullPointerException ignored) {}
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
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mShellCommandTitle.setText(Utils.getOutput(whoAmI) + ": " + Utils.getOutput(PWD));
                mRunning = false;
                if (mLastCommand.size() > 0) {
                    mUpButtom.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    private void clearAll() {
        mHistory.clear();
        mResult.clear();
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

}