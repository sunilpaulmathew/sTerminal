package com.sunilpaulmathew.terminal.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.snackbar.Snackbar;
import com.sunilpaulmathew.terminal.BuildConfig;
import com.sunilpaulmathew.terminal.R;
import com.topjohnwu.superuser.Shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 25, 2020
 */

public class Utils {

    private static Process mProcess;

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
    }

    public static boolean isDarkTheme(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean rootAccess() {
        return Shell.rootAccess();
    }

    public static List<String> runCommand(String command) {
        List<String> output = new ArrayList<>();
        try {
            mProcess = Runtime.getRuntime().exec(command);
            BufferedReader mInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            BufferedReader mError = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
            String line;
            while ((line = mInput.readLine()) != null) {
                output.add(line);
            }
            while ((line = mError.readLine()) != null) {
                output.add(line);
            }
            mProcess.waitFor();
        } catch (Exception e) {
            output.add(e.getMessage());
        }
        return output;
    }

    public static List<String> runRootCommand(String command) {
        List<String> output = new ArrayList<>();
        Shell.su(command).to(output, output).exec();
        return output;
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

    public static String readAssetFile(Context context, String file) {
        InputStream input = null;
        BufferedReader buf = null;
        try {
            StringBuilder s = new StringBuilder();
            input = context.getAssets().open(file);
            buf = new BufferedReader(new InputStreamReader(input));

            String str;
            while ((str = buf.readLine()) != null) {
                s.append(str).append("\n");
            }
            return s.toString().trim();
        } catch (IOException ignored) {
        } finally {
            try {
                if (input != null) input.close();
                if (buf != null) buf.close();
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    public static void closeSU() {
        try {
            Objects.requireNonNull(Shell.getCachedShell()).close();
        } catch (Exception ignored) {
        }
    }

    public static void destroyProcess() {
        if (mProcess != null) mProcess.destroy();
    }

    public static void goToSettings(Activity activity) {
        Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        settings.setData(uri);
        activity.startActivity(settings);
        activity.finish();
    }

    public static void initializeAppTheme(Context context) {
        if (isDarkTheme(context)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static void launchUrl(String url, Activity activity) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            activity.startActivity(i);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    public static void showSnackbar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.dismiss, v -> snackbar.dismiss());
        snackbar.show();
    }

}