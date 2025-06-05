package com.example.onenet_dateshow.fragments.audio;

import android.os.Bundle;
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
import com.example.onenet_dateshow.onenet.SoundToken;
import com.google.gson.Gson; // 确保导入正确
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SoundAbnormalWarningFragment extends Fragment implements View.OnClickListener {

    private LinearLayout soundEventsContainer;
    private TextView tvNoData;
    private Button btnLast1Hour, btnLast24Hours, btnLast7Days;

    private OkHttpClient client;
    private Call currentCall;
    // 关键修改：将 GSON 改为 gson（实例名小写，且确保是 Gson 类型）
    private static final Gson gson = new Gson();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    // OneNET 设备配置（确保与平台一致）
    private static final String PRODUCT_ID = "A14jhFn6D2";
    private static final String DEVICE_NAME = "ESP_AudioX";
    private static final String EVENT_IDENTIFIER = "abnormal_sound_alert";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sound_abnormal_warning, container, false);

        soundEventsContainer = view.findViewById(R.id.sound_warning_events_container);
        tvNoData = view.findViewById(R.id.tv_no_sound_warning);
        btnLast1Hour = view.findViewById(R.id.btn_last_1h_warning);
        btnLast24Hours = view.findViewById(R.id.btn_last_24h_warning);
        btnLast7Days = view.findViewById(R.id.btn_last_7d_warning);

        client = new OkHttpClient();
        setButtonSelected(btnLast7Days);
        try {
            loadSoundEvents(24 * 7);
        } catch (Exception e) {
            showError("初始化数据加载失败：" + e.getMessage());
        }

        btnLast1Hour.setOnClickListener(this);
        btnLast24Hours.setOnClickListener(this);
        btnLast7Days.setOnClickListener(this);

        return view;
    }

    private void setButtonSelected(Button selectedBtn) {
        btnLast1Hour.setSelected(false);
        btnLast24Hours.setSelected(false);
        btnLast7Days.setSelected(false);
        selectedBtn.setSelected(true);
    }

    private void loadSoundEvents(int hoursAgo) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        soundEventsContainer.removeAllViews();
        tvNoData.setVisibility(View.GONE);
        long startTime = getStartTime(hoursAgo);
        long endTime = System.currentTimeMillis();
        fetchSoundEventLogs(startTime, endTime);
    }

    private long getStartTime(int hoursAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -hoursAgo);
        return calendar.getTimeInMillis();
    }

    private void fetchSoundEventLogs(long startTime, long endTime) {
        String url = buildRequestUrl(startTime, endTime);
        String token;
        try {
            token = SoundToken.token();
        } catch (Exception e) {
            showErrorOnUiThread("Token生成失败：" + e.getMessage());
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .header("authorization", token)
                .header("Accept", "application/json")
                .build();

        currentCall = client.newCall(request);
        currentCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "网络请求失败：" + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "服务器错误：" + response.code(), Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                String responseBody = response.body().string();
                try {
                    // 关键修改：使用 gson 实例调用 fromJson
                    JsonRootBean root = gson.fromJson(responseBody, JsonRootBean.class);
                    if (root == null || root.code != 0) {
                        String msg = root != null ? root.msg : "请求失败，未知错误";
                        showErrorOnUiThread(msg);
                        return;
                    }

                    if (root.data == null || root.data.list == null) {
                        showNoDataState();
                        return;
                    }

                    List<EventLog> eventList = root.data.list;
                    if (eventList.isEmpty()) {
                        showNoDataState();
                        return;
                    }

                    updateEventList(eventList);
                } catch (JsonSyntaxException e) {
                    showErrorOnUiThread("数据解析失败：" + e.getMessage() + "\n响应内容：" + responseBody);
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

    private void updateEventList(List<EventLog> eventList) {
        requireActivity().runOnUiThread(() -> {
            soundEventsContainer.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);
            soundEventsContainer.removeAllViews();

            for (EventLog event : eventList) {
                View itemView = createEventItem(event);
                soundEventsContainer.addView(itemView);
            }
        });
    }

    private View createEventItem(EventLog event) {
        View itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_sound_warning, soundEventsContainer, false);

        SoundEventData data = gson.fromJson(event.value, SoundEventData.class); // 关键修改：使用 gson 实例

        TextView tvFileName = itemView.findViewById(R.id.tv_file_name);
        TextView tvTime = itemView.findViewById(R.id.tv_event_time);
        TextView tvType = itemView.findViewById(R.id.tv_sound_type);

        tvFileName.setText("文件：" + data.filename);
        tvTime.setText("时间：" + formatToStandardTime(data.timestamp));
        tvType.setText("类型：" + data.abnormal_class);

        return itemView;
    }

    // 优化后的时间格式化方法（使用正则表达式）
    private String formatToStandardTime(String rawTimestamp) {
        // 匹配格式：YYYYMMdd_HHmmss 转换为 yyyy-MM-dd HH:mm:ss
        return rawTimestamp.replace('_', ' ')
                .replaceAll("(\\d{4})(\\d{2})(\\d{2}) (\\d{2})(\\d{2})(\\d{2})", "$1-$2-$3 $4:$5:$6");
    }

    private void showNoDataState() {
        requireActivity().runOnUiThread(() -> {
            tvNoData.setVisibility(View.VISIBLE);
            soundEventsContainer.setVisibility(View.GONE);
        });
    }

    private void showErrorOnUiThread(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        );
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_last_1h_warning) {
            setButtonSelected(btnLast1Hour);
            handleButtonClick(1);
        } else if (id == R.id.btn_last_24h_warning) {
            setButtonSelected(btnLast24Hours);
            handleButtonClick(24);
        } else if (id == R.id.btn_last_7d_warning) {
            setButtonSelected(btnLast7Days);
            handleButtonClick(24 * 7);
        }
    }

    private void handleButtonClick(int hoursAgo) {
        try {
            loadSoundEvents(hoursAgo);
        } catch (Exception e) {
            showError("加载数据失败：" + e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
    }

    private void showError(String message) {
        showErrorOnUiThread(message);
    }

    // 数据模型类
    static class JsonRootBean {
        int code;
        Data data;
        String msg;
        String request_id;
    }

    static class Data {
        List<EventLog> list;
    }

    static class EventLog {
        long time;
        String value;
        String identifier;
        String name;
    }

    static class SoundEventData {
        String filename;
        String timestamp;  // 云平台返回的原始时间字符串
        String abnormal_class;
    }
}