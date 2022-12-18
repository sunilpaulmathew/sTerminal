package com.sunilpaulmathew.terminal.utils;

import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 18, 2020
 */

public class sShell {

    private static List<String> mResult = null;
    private static String mCommand = null;
    private static String mDir = "/storage/emulated/0/Android/data/com.sunilpaulmathew.terminal/files/home";

    private static Process mProcess;

    public sShell(List<String> result, String command) {
        mResult = result;
        mCommand = command;
    }

    public String getUserName() {
        StringBuilder sb = new StringBuilder();
        try {
            mProcess = Runtime.getRuntime().exec("whoami");
            BufferedReader mInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            String line;
            while ((line = mInput.readLine()) != null) {
                sb.append(line);
            }
            mProcess.waitFor();
        } catch (Exception ignored) {
        }
        return "<font color=#4285f4>" + sb + "@" + Build.MODEL + "</font># <i>";
    }

    public void exec() {
        try {
            mProcess = Runtime.getRuntime().exec(new String[] {"sh", "-c", mCommand}, null, new File(mDir));
            BufferedReader mOutput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            BufferedReader mError = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
            String line;
            while ((line = mOutput.readLine()) != null) {
                mResult.add(line);
            }
            while ((line = mError.readLine()) != null) {
                mResult.add("<font color=#FF0000>" + line + "</font>");
            }

            // Handle current directory
            if (mCommand.startsWith("cd ") && !mResult.get(mResult.size() - 1).endsWith("</font>")) {
                String[] array = mCommand.split("\\s+");
                String dir;
                if (array[array.length - 1].equals("/")) {
                    dir = "/";
                } else if (array[array.length - 1].startsWith("/")) {
                    dir = array[array.length - 1];
                } else {
                    dir = mDir + array[array.length - 1];
                }
                if (!dir.endsWith("/")) {
                    dir = dir + "/";
                }
                mDir = dir;
            }

            mProcess.waitFor();
        } catch (Exception e) {
            mResult.add(e.getMessage());
        }
    }

    public void destroy() {
        if (mProcess != null) mProcess.destroy();
    }

}