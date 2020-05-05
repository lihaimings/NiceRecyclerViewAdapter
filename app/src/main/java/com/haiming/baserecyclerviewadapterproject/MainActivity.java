package com.haiming.baserecyclerviewadapterproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RvAdapter mAdapter;
    private List<String> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < 23; i++) {
            mData.add("" + i);
        }
        mRecyclerView = findViewById(R.id.rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RvAdapter(this);
        View header = LayoutInflater.from(this).inflate(R.layout.header_layout,mRecyclerView,false);
        View footer = LayoutInflater.from(this).inflate(R.layout.footer_layout,mRecyclerView,false);
        mAdapter.setData(mData);
        mAdapter.addHeaderView(header);
       // mAdapter.addHeaderView(footer);
        mAdapter.addFooterView(footer);

        //如果添加了首尾view，则设置这个adapter
        mRecyclerView.setAdapter(mAdapter.getHeaderAndFooterAdapter());

        // 没有添加首尾view，则直接adapter
        // mRecyclerView.setAdapter(mAdapter);

    }
}
