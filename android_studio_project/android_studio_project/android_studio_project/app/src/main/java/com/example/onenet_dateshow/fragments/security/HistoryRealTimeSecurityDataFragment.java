package com.example.onenet_dateshow.fragments.security;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onenet_dateshow.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryRealTimeSecurityDataFragment extends Fragment {
    private static final String  TAG = "HistoryRealTimeSecurityDataFragment";

    private RecyclerView historyRecyclerView;
    private HistoryDataAdapter historyDataAdapter;
    private List<HistoryDataItem> historyDataList = new ArrayList<>();

    private static final String ARG_TITLE = "title";
    private static final String ARG_DATA = "data";
    private static final String ARG_TIME = "time";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_real_time_security_data, container, false);

        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        historyDataAdapter = new HistoryDataAdapter(historyDataList);
        historyRecyclerView.setAdapter(historyDataAdapter);

        if (getArguments() != null) {
            Log.d(TAG, "into HistoryRealTimeSecurityDataFragment");

            String title = getArguments().getString(ARG_TITLE);
            String data = getArguments().getString(ARG_DATA);
            String time = getArguments().getString(ARG_TIME, "");  // 使用默认值 ""

            List<HistoryDataItem> newData = new ArrayList<>();
            newData.add(new HistoryDataItem(title, data, time));
            loadData(newData);
        }

        return view;
    }

    public static HistoryRealTimeSecurityDataFragment newInstance(String title, String data, String time) {
        HistoryRealTimeSecurityDataFragment fragment = new HistoryRealTimeSecurityDataFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DATA, data);
        args.putString(ARG_TIME, time);
        fragment.setArguments(args);
        return fragment;
    }

    public void loadData(List<HistoryDataItem> newData) {
        requireActivity().runOnUiThread(() -> {
            for (HistoryDataItem newItem : newData) {
                if (!historyDataList.contains(newItem)) {
                    historyDataList.add(newItem);
                }
            }

            if (historyDataAdapter != null) {
                historyDataAdapter.notifyDataSetChanged();
            }
        });
    }

}

class HistoryDataAdapter extends RecyclerView.Adapter<HistoryDataAdapter.HistoryDataViewHolder> {  // 修改这里
    private String TAG="HistoryDataAdapter";
    private List<HistoryDataItem> historyDataList;

    public HistoryDataAdapter(List<HistoryDataItem> historyDataList) {
        this.historyDataList = historyDataList;
    }

    @NonNull
    @Override
    public HistoryDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_security_data_record, parent, false);
        return new HistoryDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryDataViewHolder holder, int position) {
        HistoryDataItem dataItem = historyDataList.get(position);
        if (dataItem != null) {
            holder.tvEventName.setText(dataItem.getEventName());
            holder.tvEventTime.setText(dataItem.getEventTime());
            holder.tvValue.setText(dataItem.getValue());
        } else {
            // 如果数据项为 null，可以设置默认值或记录日志
            holder.tvEventName.setText("未知事件");
            holder.tvEventTime.setText("时间：未知");
            holder.tvValue.setText("值：未知");
            Log.e(TAG, "onBindViewHolder: 数据项为 null，位置：" + position);
        }
    }

    @Override
    public int getItemCount() {
        return historyDataList.size();
    }

    public static class HistoryDataViewHolder extends RecyclerView.ViewHolder {

        TextView tvEventName;
        TextView tvEventTime;
        TextView tvValue;

        public HistoryDataViewHolder(View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvEventTime = itemView.findViewById(R.id.tv_event_time);
            tvValue = itemView.findViewById(R.id.tv_value);
        }
    }
}

class HistoryDataItem {
    private String eventName;
    private String eventTime;
    private String value;

    public HistoryDataItem(String eventName, String eventTime, String value) {
        this.eventName = eventName;
        this.eventTime = eventTime;
        this.value = value;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventTime() {
        return eventTime;
    }

    public String getValue() {
        return value;
    }
}

