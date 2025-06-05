package com.example.onenet_dateshow.fragments.security;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onenet_dateshow.R;

import java.util.List;

public class AbnormalDataAdapter extends RecyclerView.Adapter<AbnormalDataAdapter.ViewHolder> {

    private List<HistoryDataItem> dataList;

    public AbnormalDataAdapter(List<HistoryDataItem> dataList) {
        this.dataList = dataList;
    }

    public void setData(List<HistoryDataItem> newData) {
        dataList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AbnormalDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_security_data_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AbnormalDataAdapter.ViewHolder holder, int position) {
        HistoryDataItem item = dataList.get(position);
        holder.eventName.setText(item.getEventName());
        holder.eventTime.setText("时间：" + item.getEventTime());
        holder.eventValue.setText("值：" + item.getValue());
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;
        TextView eventTime;
        TextView eventValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.tv_event_name);
            eventTime = itemView.findViewById(R.id.tv_event_time);
            eventValue = itemView.findViewById(R.id.tv_value);
        }
    }
}
