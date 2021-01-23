package com.sunilpaulmathew.terminal.utils;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 23, 2021
 */

public class RecycleViewItem implements Serializable {
    private String mTitle;
    private String mDescription;
    private Drawable mIcon;

    public RecycleViewItem(String title, String description, Drawable icon) {
        this.mTitle = title;
        this.mDescription = description;
        this.mIcon = icon;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public Drawable getIcon() {
        return mIcon;
    }

}