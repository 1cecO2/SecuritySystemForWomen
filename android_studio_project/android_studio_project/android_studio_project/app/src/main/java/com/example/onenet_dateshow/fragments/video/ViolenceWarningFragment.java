package com.example.onenet_dateshow.fragments.video;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.onenet_dateshow.R;
import com.example.onenet_dateshow.onenet.Token;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ViolenceWarningFragment extends Fragment {

    private LinearLayout violenceEventsContainer;
    private TextView tvNoData;
    private Button btnLast1Hour, btnLast24Hours, btnLast7Days;

    private OkHttpClient client;
    private Call currentCall;
    private static final Gson GSON = new Gson();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    // OneNET 设备配置（请替换为你的实际参数）
    private static final String PRODUCT_ID = "ZBPW56xh7m";
    private static final String DEVICE_NAME = "camera";
    private static final String EVENT_IDENTIFIER = "camera_violence"; // 暴力行为事件标识符

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_violence_warning, container, false);
        violenceEventsContainer = view.findViewById(R.id.violence_events_container);
        tvNoData = view.findViewById(R.id.tv_no_data);
        btnLast1Hour = view.findViewById(R.id.btn_last_1_hour);
        btnLast24Hours = view.findViewById(R.id.btn_last_24_hours);
        btnLast7Days = view.findViewById(R.id.btn_last_7_days);

        // 初始化网络客户端
        client = new OkHttpClient();
        // 默认加载最近7天数据
        setButtonSelected(btnLast7Days);
        try {
            loadViolenceEvents(24 * 7);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            // 统一处理异常，避免代码重复
            showErrorOnUiThread("初始化数据加载失败：" + e.getMessage());
        }

        // 按钮点击事件
        btnLast1Hour.setOnClickListener(v -> {
            setButtonSelected(btnLast1Hour);
            try {
                loadViolenceEvents(1);
            } catch (Exception e) {
                showErrorOnUiThread("请求最近1小时数据失败：" + e.getMessage());
            }
        });
        btnLast24Hours.setOnClickListener(v -> {
            setButtonSelected(btnLast24Hours);
            try {
                loadViolenceEvents(24);
            } catch (Exception e) {
                showErrorOnUiThread("请求最近24小时数据失败：" + e.getMessage());
            }
        });
        btnLast7Days.setOnClickListener(v -> {
            setButtonSelected(btnLast7Days);
            try {
                loadViolenceEvents(24 * 7);
            } catch (Exception e) {
                showErrorOnUiThread("请求最近7天数据失败：" + e.getMessage());
            }
        });

        return view;
    }

    private void setButtonSelected(Button selectedButton) {
        btnLast1Hour.setSelected(false);
        btnLast24Hours.setSelected(false);
        btnLast7Days.setSelected(false);
        selectedButton.setSelected(true);
    }

    private void loadViolenceEvents(int hoursAgo) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        violenceEventsContainer.removeAllViews();
        tvNoData.setVisibility(View.GONE);
        long startTime = getStartTime(hoursAgo);
        long endTime = System.currentTimeMillis();
        fetchViolenceData(startTime, endTime);
    }

    private long getStartTime(int hoursAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -hoursAgo);
        return calendar.getTimeInMillis();
    }

    private void fetchViolenceData(long startTime, long endTime) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        String url = buildRequestUrl(startTime, endTime);
        String token = Token.token(); // 从你的Token工具类获取

        Request request = new Request.Builder()
                .url(url)
                .addHeader("authorization", token)
                .addHeader("Accept", "application/json")
                .build();

        currentCall = client.newCall(request);
        currentCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "网络请求失败：" + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "服务器错误: " + response.code(), Toast.LENGTH_LONG).show());
                    return;
                }

                String responseBody = response.body().string();
                try {
                    JsonRootBean root = GSON.fromJson(responseBody, JsonRootBean.class);
                    // 添加 root 空指针检查
                    if (root == null || root.getCode() != 0) {
                        String errorMsg = root != null ? root.getMsg() : "未知错误";
                        showErrorOnUiThread("请求失败：" + errorMsg);
                        return;
                    }
                    // 添加 data 空指针检查
                    Data data = root.getData();
                    if (data == null) {
                        showErrorOnUiThread("数据为空，无该时间段的暴力行为记录");
                        // 显示无数据提示
                        requireActivity().runOnUiThread(() -> {
                            tvNoData.setVisibility(View.VISIBLE);
                            violenceEventsContainer.setVisibility(View.GONE);
                        });
                        return;
                    }
                    // 添加 list 空指针检查
                    List<EventLog> eventList = data.getList();
                    if (eventList == null || eventList.isEmpty()) {
                        showErrorOnUiThread("无该时间段的暴力行为记录");
                        // 显示无数据提示
                        requireActivity().runOnUiThread(() -> {
                            tvNoData.setVisibility(View.VISIBLE);
                            violenceEventsContainer.setVisibility(View.GONE);
                        });
                        return;
                    }

                    processViolenceEvents(eventList);
                } catch (JsonSyntaxException e) {
                    showErrorOnUiThread("数据解析失败：" + e.getMessage());
                }
            }
        });
    }

    private String buildRequestUrl(long startTime, long endTime) {
        return "https://iot-api.heclouds.com/device/event-log?" +
                "product_id=" + PRODUCT_ID + "&" +
                "device_name=" + DEVICE_NAME + "&" +
                "start_time=" + startTime + "&" +
                "end_time=" + endTime + "&" +
                "identifier=" + EVENT_IDENTIFIER;
    }

    private void processViolenceEvents(List<EventLog> eventList) {
        requireActivity().runOnUiThread(() -> {
            if (eventList.isEmpty()) {
                tvNoData.setVisibility(View.VISIBLE);
                violenceEventsContainer.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "无该时间段的暴力行为记录", Toast.LENGTH_SHORT).show();
                return;
            }

            violenceEventsContainer.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);
            violenceEventsContainer.removeAllViews();

            for (EventLog event : eventList) {
                View itemView = createEventItem(event);
                violenceEventsContainer.addView(itemView);
            }
        });
    }

    private View createEventItem(EventLog event) {
        View itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_violence_event, violenceEventsContainer, false);

        TextView tvEventName = itemView.findViewById(R.id.tv_event_name);
        TextView tvEventTime = itemView.findViewById(R.id.tv_event_time);
        TextView tvConfidence = itemView.findViewById(R.id.tv_confidence);

        ViolenceData violenceData = GSON.fromJson(event.getValue(), ViolenceData.class);
        tvEventName.setText(event.getName());
        tvEventTime.setText(formatTime(violenceData.getTimeViolence()));
        tvConfidence.setText("置信度：" + String.format("%.2f", violenceData.getConfidenceViolence()));

        return itemView;
    }

    private String formatTime(long timestamp) {
        return DATE_FORMAT.format(timestamp);
    }

    private void showErrorOnUiThread(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentCall != null) {
            currentCall.cancel();
        }
    }

    // 自动生成的 JsonRootBean 类
    static class JsonRootBean {
        private int code;
        private Data data;
        private String msg;
        private String request_id;

        public void setCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public Data getData() {
            return data;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }

        public void setRequest_id(String request_id) {
            this.request_id = request_id;
        }

        public String getRequest_id() {
            return request_id;
        }
    }

    // 自动生成的 List 类，这里改为 EventLog 避免冲突
    static class EventLog {
        private long time;
        private String value;
        private int event_type;
        private String identifier;
        private String name;

        public void setTime(long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setEvent_type(int event_type) {
            this.event_type = event_type;
        }

        public int getEvent_type() {
            return event_type;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    static class Data {
        private List<EventLog> list;

        public List<EventLog> getList() {
            return list;
        }

        public void setList(List<EventLog> list) {
            this.list = list;
        }
    }

    static class ViolenceData {
        private long time_violence;
        private double confidence_violence;

        public long getTimeViolence() {
            return time_violence;
        }

        public double getConfidenceViolence() {
            return confidence_violence;
        }
    }
}