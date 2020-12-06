package com.sunilpaulmathew.terminal.utils;

import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

import com.sunilpaulmathew.terminal.BuildConfig;
import com.topjohnwu.superuser.Shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 25, 2020
 */

public class Utils {

    static {
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    public static void runCommand(String command, List<String> output) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader mInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader mError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = mInput.readLine()) != null) {
                output.add(line);
            }
            while ((line = mError.readLine()) != null) {
                output.add(line);
            }
        } catch (Exception ignored) {
        }
    }

    public static void runRootCommand(String command, List<String> output) {
        Shell.su(command).to(output, output).exec();
    }

    public static boolean rootAccess() {
        return Shell.rootAccess();
    }

    public static void closeSU() {
        try {
            Objects.requireNonNull(Shell.getCachedShell()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isDarkTheme(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void initializeAppTheme(Context context) {
        if (isDarkTheme(context)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static String getOutput(List<String> output) {
        List<String> mData = new ArrayList<>();
        for (String line : output.toString().substring(1, output.toString().length() - 1).replace(
                ", ","\n").replace("ui_print","").split("\\r?\\n")) {
            if (!line.startsWith("progress")) {
                mData.add(line);
            }
        }
        return mData.toString().substring(1, mData.toString().length() - 1).replace(", ","\n")
                .replaceAll("(?m)^[ \t]*\r?\n", "");
    }

}