package com.haiming.baserecyclerviewadapterproject;

import android.view.View;
import android.widget.TextView;

import com.haiming.baserecyclerviewadapterproject.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RvViewHolder extends RecyclerView.ViewHolder {

    public TextView mTextView;
    public RvViewHolder(@NonNull View itemView) {
        super(itemView);
        mTextView = itemView.findViewById(R.id.txt);
    }
}
