package com.example.onenet_dateshow.fragments.video;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onenet_dateshow.R;

import java.util.List;

public class FlvFileAdapter extends RecyclerView.Adapter<FlvFileAdapter.ViewHolder> {

    private List<String> fileNames;
    private OnItemClickListener listener;
    private int selectedPosition = -1; // 记录选中位置

    public FlvFileAdapter(List<String> fileNames, OnItemClickListener listener) {
        this.fileNames = fileNames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flv_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.fileName.setText(fileNames.get(position));
        int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition == selectedPosition) {
            holder.fileName.setTextColor(Color.parseColor("#33B5E5")); // 选中时文字变蓝色
        } else {
            holder.fileName.setTextColor(Color.BLACK); // 默认文字颜色为黑色
        }
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                selectedPosition = currentPosition;
                notifyDataSetChanged(); // 刷新列表
                listener.onItemClick(currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileNames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.tv_file_name);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}