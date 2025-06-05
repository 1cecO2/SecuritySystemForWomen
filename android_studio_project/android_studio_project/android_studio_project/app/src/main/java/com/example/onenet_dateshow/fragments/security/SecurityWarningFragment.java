package com.example.onenet_dateshow.fragments.security;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onenet_dateshow.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.EditText;
import android.widget.Toast;

public class SecurityWarningFragment extends Fragment {

    private static final String TAG = "SecurityWarningFragment";
    private List<SecurityWarningFragment.ProductInfo> products = Arrays.asList(
            //温度
            new SecurityWarningFragment.ProductInfo("Dv6lHFqZkb", "DHT11",
                    "version=2018-10-31&res=products%2FDv6lHFqZkb%2Fdevices%2FDHT11&et=2061335327&method=md5&sign=3aeWozu8kkrc1vkYlwXOuA%3D%3D",
                    "temp_value"),
            //湿度
            new SecurityWarningFragment.ProductInfo("Dv6lHFqZkb", "DHT11",
                    "version=2018-10-31&res=products%2FDv6lHFqZkb%2Fdevices%2FDHT11&et=2061335327&method=md5&sign=3aeWozu8kkrc1vkYlwXOuA%3D%3D",
                    "humidity_value"),
            //烟雾
            new SecurityWarningFragment.ProductInfo("jLV6eY50hl", "smog",
                    "version=2018-10-31&res=products%2FjLV6eY50hl%2Fdevices%2Fsmog&et=2061335327&method=md5&sign=8f6Yd00aCzY%2Frkn9KCQrLQ%3D%3D",
                    "device_smog"),
            //火焰
            new SecurityWarningFragment.ProductInfo("VlYU663TY1", "Flame_Detector",
                    "version=2018-10-31&res=products%2FVlYU663TY1%2Fdevices%2FFlame_Detector&et=2061335327&method=md5&sign=1U0fgf2Q77yJWFXavKqVqw%3D%3D",
                    "flame"),
            //甲烷
            new SecurityWarningFragment.ProductInfo("7PCKms52qM", "CH4",
                    "version=2018-10-31&res=products%2F7PCKms52qM%2Fdevices%2FCH4&et=2061335327&method=md5&sign=arDtJERPMxS%2FB%2FyxwtRLxQ%3D%3D",
                    "CH4"),
            //霍尔
            new SecurityWarningFragment.ProductInfo("z3937TK346", "HUO_ER",
                    "version=2018-10-31&res=products%2Fz3937TK346%2Fdevices%2FHUO_ER&et=2061335327&method=md5&sign=7GBkpWTTmaOlx1P0U5RBaA%3D%3D",
                    "dr_state"),
            //人体红外
            new SecurityWarningFragment.ProductInfo("32cemE3eW0", "HC_SR501",
                    "version=2018-10-31&res=products%2F32cemE3eW0%2Fdevices%2FHC_SR501&et=1866941340&method=md5&sign=XUqBx3eM9LAn2WEsiwZhcw%3D%3D",
                    "Door_Person")
    );


    private Map<String, Threshold> thresholdMap = new HashMap<>();

    private final OkHttpClient httpClient = new OkHttpClient();
    private Map<String, HistoryRealTimeSecurityDataFragment> fragmentMap = new HashMap<>();

    private RecyclerView recyclerView;
    private AbnormalDataAdapter adapter;
    private List<HistoryDataItem> allAbnormalData = new ArrayList<>();

    // 声明视图组件
    private EditText etTempMin, etTempMax, etHumidityMin, etHumidityMax;
    private EditText etSmogMin, etSmogMax, etFlameMin, etFlameMax;
    private EditText etCh4Min, etCh4Max;
    private Button btnConfirm, btnRefresh;
    private TextView tvThresholdTemp, tvThresholdHumidity, tvThresholdSmog, tvThresholdFlame, tvThresholdCh4;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_security_warning, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图组件
        etTempMin = view.findViewById(R.id.et_temp_min);
        etTempMax = view.findViewById(R.id.et_temp_max);
        etHumidityMin = view.findViewById(R.id.et_humidity_min);
        etHumidityMax = view.findViewById(R.id.et_humidity_max);

        etSmogMin = view.findViewById(R.id.et_smog_min);
        etSmogMax = view.findViewById(R.id.et_smog_max);
        etFlameMin = view.findViewById(R.id.et_flame_min);
        etFlameMax = view.findViewById(R.id.et_flame_max);

        etCh4Min = view.findViewById(R.id.et_ch4_min);
        etCh4Max = view.findViewById(R.id.et_ch4_max);

        btnConfirm = view.findViewById(R.id.button_confirm);
        btnRefresh = view.findViewById(R.id.button_refresh);

        tvThresholdTemp = view.findViewById(R.id.tv_threshold_temp);
        tvThresholdHumidity = view.findViewById(R.id.tv_threshold_humidity);
        tvThresholdSmog = view.findViewById(R.id.tv_threshold_smog);
        tvThresholdFlame = view.findViewById(R.id.tv_threshold_flame);
        tvThresholdCh4 = view.findViewById(R.id.tv_threshold_ch4);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AbnormalDataAdapter(allAbnormalData);
        recyclerView.setAdapter(adapter);


        initThresholds();
        updateAllThresholdTextViews(view);

        fetchWarningData(); // 初始加载

        // 设置按钮的点击事件监听器
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmClicked();
            }
        });

        Button refreshButton = view.findViewById(R.id.button_refresh);
        refreshButton.setOnClickListener(v -> {
            allAbnormalData.clear();
            Log.d(TAG, "手动点击刷新按钮");
            fetchWarningData();
        });

    }

    private void onConfirmClicked() {
        // 获取输入值
        String tempMin = etTempMin.getText().toString();
        String tempMax = etTempMax.getText().toString();
        String humidityMin = etHumidityMin.getText().toString();
        String humidityMax = etHumidityMax.getText().toString();
        String smogMin = etSmogMin.getText().toString();
        String smogMax = etSmogMax.getText().toString();
        String flameMin = etFlameMin.getText().toString();
        String flameMax = etFlameMax.getText().toString();
        String ch4Min = etCh4Min.getText().toString();
        String ch4Max = etCh4Max.getText().toString();

        // 更新 Threshold 对象（已处理“为空不更新”逻辑）
        updateThreshold("temp_value", tempMin, tempMax);
        updateThreshold("humidity_value", humidityMin, humidityMax);
        updateThreshold("device_smog", smogMin, smogMax);
        updateThreshold("flame", flameMin, flameMax);
        updateThreshold("CH4", ch4Min, ch4Max);

        // 从 Map 中取出实际生效的值进行展示
        Threshold temp = thresholdMap.get("temp_value");
        Threshold humidity = thresholdMap.get("humidity_value");
        Threshold smog = thresholdMap.get("device_smog");
        Threshold flame = thresholdMap.get("flame");
        Threshold ch4 = thresholdMap.get("CH4");

        // 显示实际阈值（null 时使用“未设定”）
        tvThresholdTemp.setText("温度阈值：最小 " + valueToString(temp.getMin()) + "°C，最大 " + valueToString(temp.getMax()) + "°C");
        tvThresholdHumidity.setText("湿度阈值：最小 " + valueToString(humidity.getMin()) + "%，最大 " + valueToString(humidity.getMax()) + "%");
        tvThresholdSmog.setText("烟雾阈值：最小 " + valueToString(smog.getMin()) + "，最大 " + valueToString(smog.getMax()));
        tvThresholdFlame.setText("火焰阈值：最小 " + valueToString(flame.getMin()) + "，最大 " + valueToString(flame.getMax()));
        tvThresholdCh4.setText("甲烷阈值：最小 " + valueToString(ch4.getMin()) + "，最大 " + valueToString(ch4.getMax()));
    }

    private String valueToString(Double value) {
        return value != null ? String.valueOf(value) : "null";
    }

    private void updateThreshold(String key, String minStr, String maxStr) {
        try {
            // 获取原有 Threshold 对象
            Threshold threshold = thresholdMap.get(key);
            if (threshold == null) {
                threshold = new Threshold(null, null);
            }

            boolean hasMin = minStr != null && !minStr.trim().isEmpty();
            boolean hasMax = maxStr != null && !maxStr.trim().isEmpty();

            // 如果两个都为空，不更新
            if (!hasMin && !hasMax) {
                return;
            }

            // 分别更新有值的字段
            if (hasMin) {
                double min = Double.parseDouble(minStr);
                threshold.setMin(min); // 使用 setter 设置新值
            }

            if (hasMax) {
                double max = Double.parseDouble(maxStr);
                threshold.setMax(max);
            }

            // 更新 map
            thresholdMap.put(key, threshold);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "请输入有效的数字", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateAllThresholdTextViews(View view) {
        Threshold tempThreshold = thresholdMap.get("temp_value");
        if (tempThreshold != null) {
            TextView tvTemp = view.findViewById(R.id.tv_threshold_temp);
            tvTemp.setText("温度阈值：最小 " + tempThreshold.getMin() + "°C，最大 " + tempThreshold.getMax() + "°C");
        }

        Threshold humidityThreshold = thresholdMap.get("humidity_value");
        if (humidityThreshold != null) {
            TextView tvHumidity = view.findViewById(R.id.tv_threshold_humidity);
            tvHumidity.setText("湿度阈值：最小 " + humidityThreshold.getMin() + "%，最大 " + humidityThreshold.getMax() + "%");
        }

        Threshold smogThreshold = thresholdMap.get("device_smog");
        if (smogThreshold != null) {
            TextView tvSmog = view.findViewById(R.id.tv_threshold_smog);
            tvSmog.setText("烟雾阈值：最小 " + smogThreshold.getMin() + "，最大 " + smogThreshold.getMax());
        }

        // 其他传感器
        Threshold flameThreshold = thresholdMap.get("flame");
        if (flameThreshold != null) {
            TextView tvFlame = view.findViewById(R.id.tv_threshold_flame);
            tvFlame.setText("火焰阈值：最小 " + flameThreshold.getMin() + "，最大 " + flameThreshold.getMax());
        }

        Threshold methaneThreshold = thresholdMap.get("CH4");
        if (methaneThreshold != null) {
            TextView tvMethane = view.findViewById(R.id.tv_threshold_ch4);
            tvMethane.setText("甲烷阈值：最小 " + methaneThreshold.getMin() + "，最大 " + methaneThreshold.getMax());

        }

    }

    public void initThresholds() {
        thresholdMap.put("temp_value", new Threshold(0.0, 40.0));
        thresholdMap.put("humidity_value", new Threshold(20.0, 95.0));
        thresholdMap.put("device_smog", new Threshold(8.0,false));
        thresholdMap.put("flame", new Threshold(3300.0,false));
        thresholdMap.put("CH4", new Threshold(3.0, false));
    }

    public void fetchWarningData() {
        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000;

        for (ProductInfo product : products) {
            String identifier = product.getPropertyIdentifier();
            String url = "https://iot-api.heclouds.com/thingmodel/query-device-property-history?" +
                    "product_id=" + product.getProductId() +
                    "&device_name=" + product.getDeviceName() +
                    "&identifier=" + identifier +
                    "&start_time=" + sevenDaysAgo +
                    "&end_time=" + now;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", product.getAuthorization())
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("fetchWarningData", "请求失败：" + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        RealTimeSecurityDataFragment.HistoryDeviceDataResponse deviceDataResponse =
                                new Gson().fromJson(json, RealTimeSecurityDataFragment.HistoryDeviceDataResponse.class);

                        if (deviceDataResponse.isSuccess()) {
                            List<RealTimeSecurityDataFragment.HistoryDeviceDataResponse.DataPoint> dataPoints =
                                    deviceDataResponse.getData().getHistoryData();
                            List<HistoryDataItem> abnormalData = new ArrayList<>();
                            Threshold threshold = thresholdMap.get(identifier);

                            for (RealTimeSecurityDataFragment.HistoryDeviceDataResponse.DataPoint point : dataPoints) {
                                String formattedTime = point.getFormattedTime();
                                String valueStr = point.getValue();

                                // ✅ 如果是 Door_Person，直接添加
                                if ("Door_Person".equals(identifier)) {
                                    abnormalData.add(new HistoryDataItem(identifier, formattedTime, "门外有人停留超过10s"));
                                    continue;
                                }
                                if (identifier.equals("dr_state")) {
                                    valueStr = valueStr.equals("1") ? "打开" : "关闭";
                                    abnormalData.add(new HistoryDataItem(identifier, formattedTime, valueStr));
                                    continue;
                                }

                                try {
                                    if (valueStr != null && valueStr.matches("^-?\\d+(\\.\\d+)?$")) {
                                        double value = Double.parseDouble(valueStr);  // 兼容整数和小数
                                        // 正常使用 value
                                        HistoryDataItem item = new HistoryDataItem(identifier, formattedTime, valueStr);

                                        if (threshold != null && threshold.isOutOfRange(value)) {
                                            abnormalData.add(item);
                                        }
                                    } else {
                                        Log.w("fetchWarningData", "无效或非数字格式: " + valueStr);
                                    }
                                } catch (NumberFormatException e) {
                                    Log.w("fetchWarningData", "无效数值: " + valueStr+ "'", e);
                                }
                            }

                            // ✅ 排序：时间降序（新在前）
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            abnormalData.sort((item1, item2) -> {
                                try {
                                    Date date1 = sdf.parse(item1.getEventTime());
                                    Date date2 = sdf.parse(item2.getEventTime());
                                    return date2.compareTo(date1);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return 0;
                                }
                            });

                            // 合并所有异常数据项并刷新适配器
                            synchronized (allAbnormalData) {
                                allAbnormalData.addAll(abnormalData);
                                requireActivity().runOnUiThread(() -> adapter.setData(new ArrayList<>(allAbnormalData)));
                            }
                        }
                    }
                }
            });
        }
    }


    public class Threshold {
        private Double min;
        private Double max;

        // Constructor for both min and max values
        public Threshold(Double min, Double max) {
            this.min = min;
            this.max = max;
        }

        // Constructor for only one value (either min or max)
        public Threshold(Double value, boolean isMin) {
            if (isMin) {
                this.min = value;
                this.max = null; // No max value
            } else {
                this.min = null;
                this.max = value; // No min value
            }
        }

        // Getter methods
        public Double getMin() {
            return min;
        }

        public Double getMax() {
            return max;
        }

        // You could also add a utility method to check if there's no max threshold
        public boolean hasMax() {
            return max != null;
        }

        public boolean isOutOfRange(double value) {
            if (min != null && value < min) return true;
            if (max != null && value > max) return true;
            return false;
        }
        public void setMin(Double min) {
            this.min = min;
        }

        public void setMax(Double max) {
            this.max = max;
        }

    }


    class ProductInfo {

        private String productId;
        private String deviceName;
        private String authorization; // ✅ 原来的 apiKey 换成 authorization
        private String propertyIdentifier;

        public ProductInfo(String productId, String deviceName, String authorization, String propertyIdentifier) {
            this.productId = productId;
            this.deviceName = deviceName;
            this.authorization = authorization;
            this.propertyIdentifier = propertyIdentifier;
        }

        public String getProductId() {
            return productId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getAuthorization() {
            return authorization;
        }

        public String getPropertyIdentifier() {
            return propertyIdentifier;
        }
    }
}
