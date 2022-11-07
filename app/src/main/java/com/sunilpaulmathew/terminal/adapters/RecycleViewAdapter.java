package com.sunilpaulmathew.terminal.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.sunilpaulmathew.terminal.R;
import com.sunilpaulmathew.terminal.utils.RecycleViewItem;
import com.sunilpaulmathew.terminal.utils.Utils;

import java.util.ArrayList;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 23, 2021
 */

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

    private final ArrayList<RecycleViewItem> data;

    private static RecycleViewAdapter.ClickListener mClickListener;

    public RecycleViewAdapter(ArrayList<RecycleViewItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_settings, parent, false);
        return new RecycleViewAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {
        if (this.data.get(position).getTitle() != null) {
            holder.mTitle.setText(this.data.get(position).getTitle());
            holder.mTitle.setVisibility(View.VISIBLE);
            holder.mTitle.setTextColor(Utils.isDarkTheme(holder.mIcon.getContext()) ? Color.WHITE : Color.BLACK);
        }
        if (this.data.get(position).getDescription() != null) {
            holder.mDescription.setText(this.data.get(position).getDescription());
            holder.mDescription.setVisibility(View.VISIBLE);
        }
        if (this.data.get(position).getIcon() != null) {
            holder.mIcon.setImageDrawable(this.data.get(position).getIcon());
            holder.mIcon.setColorFilter(Utils.isDarkTheme(holder.mIcon.getContext()) ? Color.WHITE : Color.BLACK);
            holder.mIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageView mIcon;
        private final MaterialTextView mTitle, mDescription;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
        }

        @Override
        public void onClick(View view) {
            mClickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public void setOnItemClickListener(RecycleViewAdapter.ClickListener clickListener) {
        RecycleViewAdapter.mClickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}