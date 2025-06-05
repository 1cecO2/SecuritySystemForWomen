package com.example.onenet_dateshow.fragments.security;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Call;
import okhttp3.Response;

import com.example.onenet_dateshow.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.JsonElement;

public class RealTimeSecurityDataFragment extends Fragment
        implements View.OnClickListener {


    private static final String TAG = "fragement";

    private Button buttonRefresh;

    private TextView textTemperature;
    private TextView textHumidity;
    private TextView textSmoke;
    private TextView textMethane;
    private TextView textFlame;
    private TextView textDoorStatus;


    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private SecurityViewPagerAdapter adapter;

    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private String currentSelectedIdentifier = "temp_value"; // 默认初始值

    private List<Fragment> fragmentList = new ArrayList<>();

    private final Map<String, HistoryRealTimeSecurityDataFragment> fragmentMap = new HashMap<>();


    private OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private List<ProductInfo> products = Arrays.asList(
            //烟雾
            new ProductInfo("Dv6lHFqZkb", "DHT11",
                    "version=2018-10-31&res=products%2FDv6lHFqZkb%2Fdevices%2FDHT11&et=2061335327&method=md5&sign=3aeWozu8kkrc1vkYlwXOuA%3D%3D",
                    "temp_value"),
            //湿度
            new ProductInfo("Dv6lHFqZkb", "DHT11",
                    "version=2018-10-31&res=products%2FDv6lHFqZkb%2Fdevices%2FDHT11&et=2061335327&method=md5&sign=3aeWozu8kkrc1vkYlwXOuA%3D%3D",
                    "humidity_value"),
            //烟雾
            new ProductInfo("jLV6eY50hl", "smog",
                    "version=2018-10-31&res=products%2FjLV6eY50hl%2Fdevices%2Fsmog&et=2061335327&method=md5&sign=8f6Yd00aCzY%2Frkn9KCQrLQ%3D%3D",
                    "device_smog"),
            //火焰
            new ProductInfo("VlYU663TY1", "Flame_Detector",
                    "version=2018-10-31&res=products%2FVlYU663TY1%2Fdevices%2FFlame_Detector&et=2061335327&method=md5&sign=1U0fgf2Q77yJWFXavKqVqw%3D%3D",
                    "flame"),
            //甲烷
            new ProductInfo("7PCKms52qM", "CH4",
                    "version=2018-10-31&res=products%2F7PCKms52qM%2Fdevices%2FCH4&et=2061335327&method=md5&sign=arDtJERPMxS%2FB%2FyxwtRLxQ%3D%3D",
                    "CH4"),
            //霍尔
            new ProductInfo("z3937TK346", "HUO_ER",
                    "version=2018-10-31&res=products%2Fz3937TK346%2Fdevices%2FHUO_ER&et=2061335327&method=md5&sign=7GBkpWTTmaOlx1P0U5RBaA%3D%3D",
                    "dr_state"),
            //人体红外
            new ProductInfo("32cemE3eW0", "HC_SR501",
                    "version=2018-10-31&res=products%2F32cemE3eW0%2Fdevices%2FHC_SR501&et=1866941340&method=md5&sign=XUqBx3eM9LAn2WEsiwZhcw%3D%3D",
                    "Door_Person")
    );

    private final int REFRESH_INTERVAL_MS = 2 * 1000; // 每60秒刷新一次

    private final int TAB_REFRESH_INTERVAL_MS = 2 * 1000;

    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            fetchLatestRealTimeData(); // 执行刷新任务
            fetchRealTimeData(currentSelectedIdentifier);
            mainHandler.postDelayed(this, REFRESH_INTERVAL_MS); // 再次延迟执行
        }
    };

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_real_time_security_data, container, false);

        buttonRefresh = view.findViewById(R.id.buttonRefresh);

        textTemperature = view.findViewById(R.id.textTemperature);
        textHumidity = view.findViewById(R.id.textHumidity);
        textSmoke = view.findViewById(R.id.textSmoke);
        textMethane = view.findViewById(R.id.textMethane);
        textFlame = view.findViewById(R.id.textFlame);
        textDoorStatus = view.findViewById(R.id.textDoorStatus);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Initializing ViewModel and observers");
        fetchLatestRealTimeData();
        buttonRefresh.setOnClickListener(this);

        // 启动定时刷新
        mainHandler.postDelayed(autoRefreshRunnable, REFRESH_INTERVAL_MS);
        mainHandler.postDelayed(tabDataAutoRefreshRunnable, TAB_REFRESH_INTERVAL_MS);

        String[] titles = {"温度", "湿度", "烟雾", "甲烷", "火焰", "门状态"};
        String[] identifiers = {"temp_value", "humidity_value", "device_smog", "CH4", "flame", "dr_state"};

        for (int i = 0; i < identifiers.length; i++) {
            HistoryRealTimeSecurityDataFragment fragment = HistoryRealTimeSecurityDataFragment.newInstance(titles[i], "", "");
            fragmentMap.put(identifiers[i], fragment);
            fragmentList.add(fragment);
        }

        // 设置适配器
        adapter = new SecurityViewPagerAdapter(getChildFragmentManager(), getLifecycle(), fragmentList);
        viewPager.setAdapter(adapter);

        // 设置 Tab 名称并绑定 TabLayout 与 ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            Log.d(TAG, "into TabLayoutMediator");
            switch (position) {
                case 0:
                    tab.setText("温度");
                    break;
                case 1:
                    tab.setText("湿度");
                    break;
                case 2:
                    tab.setText("烟雾");
                    break;
                case 3:
                    tab.setText("甲烷");
                    break;
                case 4:
                    tab.setText("火焰");
                    break;
                case 5:
                    tab.setText("门状态");
                    break;
            }
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        currentSelectedIdentifier = "temp_value";
                        break;
                    case 1:
                        currentSelectedIdentifier = "humidity_value";
                        break;
                    case 2:
                        currentSelectedIdentifier = "device_smog";
                        break;
                    case 3:
                        currentSelectedIdentifier = "CH4";
                        break;
                    case 4:
                        currentSelectedIdentifier = "flame";
                        break;
                    case 5:
                        currentSelectedIdentifier = "dr_state";
                        break;
                }

                // 刷新当前Tab数据
                fetchRealTimeData(currentSelectedIdentifier);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });


    }

    private final Runnable tabDataAutoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            fetchRealTimeData(currentSelectedIdentifier); // 根据当前选中的标识符刷新
            mainHandler.postDelayed(this, TAB_REFRESH_INTERVAL_MS);
        }
    };
    @Override
    public void onClick(View v) { // 必须实现的方法
        if (v.getId() == R.id.buttonRefresh) {
            // 处理点击
            Log.d(TAG, "Refresh button clicked - start fetching data");
            fetchLatestRealTimeData();
            // fetchRealTimeData("device_smog");
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainHandler.removeCallbacks(autoRefreshRunnable); // 停止定时任务
    }


    //数据列表
    public void fetchLatestRealTimeData() {
        Log.d(TAG, "触发实时数据刷新，获取所有传感器的最新数据");

        // 创建一个线程池来并发处理多个请求
        ExecutorService executorService = Executors.newFixedThreadPool(products.size());

        for (ProductInfo product : products) {
            // 检查 product.getProductId() 是否等于 "32cemE3eW0"
            if ("32cemE3eW0".equals(product.getProductId())) {
                // 如果相等，跳过当前循环的执行
                Log.d(TAG, "跳过 productId: " + product.getProductId());
                continue;  // 跳过当前循环，继续下一个 product
            }

            executorService.submit(() -> {
                String url = "https://iot-api.heclouds.com/thingmodel/query-device-property?" +
                        "product_id=" + product.getProductId() +
                        "&device_name=" + product.getDeviceName();
                Log.d(TAG, url);

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", product.getAuthorization())
                        .build();

                httpClient.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(TAG, "刷新失败: " + product.getPropertyIdentifier() + e.getMessage(), e);

                    }
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String json = response.body().string();
                                Log.d(TAG, "响应 JSON: " + json);

                                JsonParser parser = new JsonParser();
                                JsonObject jsonObject = parser.parse(json).getAsJsonObject();
                                JsonArray dataArray = jsonObject.getAsJsonArray("data");

                                String identifier = null;
                                String value = null;

                                for (JsonElement element : dataArray) {
                                    JsonObject dataItem = element.getAsJsonObject();
                                    String id = dataItem.get("identifier").getAsString();

                                    if (id.equals(product.getPropertyIdentifier())) {
                                        identifier = id;
                                        value = dataItem.get("value").getAsString();
                                        break; // 找到就退出循环
                                    }
                                }

                                if (identifier != null && value != null) {
                                    // 处理 dr_state 特殊值转换
                                    if (identifier.equals("dr_state")) {
                                        value = value.equals("1") ? "打开" : "关闭";
                                    }

                                    Log.d(TAG, identifier + " value: " + value);

                                    // 更新 UI
                                    SensorData sensorData = new SensorData(); // 你可以根据需要填充
                                    updateUI(identifier, value);
                                } else {
                                    Log.w(TAG, "未找到匹配的 identifier：" + product.getPropertyIdentifier());
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "数据解析失败", e);
                        }
                    }

                });
            });
        }

        // 关闭线程池
        executorService.shutdown();
    }

    private void updateUI(final String propertyIdentifier, final String value) {
        // 确保在主线程中更新 UI
        mainHandler.post(() -> {
            switch (propertyIdentifier) {
                case "temp_value":
                    textTemperature.setText(value);
                    break;
                case "humidity_value":
                    textHumidity.setText(value);
                    break;
                case "device_smog":
                    textSmoke.setText(value);
                    break;
                case "flame":
                    textFlame.setText(value);
                    break;
                case "CH4":
                    textMethane.setText(value);
                    break;
                case "dr_state":
                    textDoorStatus.setText(value);
                    break;
                case "Door_Person":
                    textDoorStatus.setText(value);
                    break;
                default:
                    Log.e(TAG, "未知的产品类型: " + propertyIdentifier);
            }
        });
    }

    public void fetchRealTimeData(String productType) {
        Log.d("fetchRealTimeData", "开始获取实时数据，产品类型：" + productType);

        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000; // 7天前

        for (ProductInfo product : products) {
            if (product.getPropertyIdentifier().equals(productType)) {  // 判断是否是当前选中的产品
                String identifier = product.getPropertyIdentifier();
                String url = "https://iot-api.heclouds.com/thingmodel/query-device-property-history?" +
                        "product_id=" + product.getProductId() +
                        "&device_name=" + product.getDeviceName() +
                        "&identifier=" + identifier +
                        "&start_time=" + sevenDaysAgo +
                        "&end_time=" + now;

                Log.d("fetchRealTimeData", "开始发送请求，url：" + url);

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", product.getAuthorization())
                        .build();

                httpClient.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("fetchRealTimeData", "请求失败：" + e.getMessage());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String json = response.body().string();
                                Log.d("fetchRealTimeData", "接收json：" + json);

                                HistoryDeviceDataResponse deviceDataResponse = new Gson().fromJson(json, HistoryDeviceDataResponse.class);

                                // 如果响应成功
                                if (deviceDataResponse.isSuccess()) {
                                    // 获取所有的数据点
                                    List<HistoryDeviceDataResponse.DataPoint> dataPoints = deviceDataResponse.getData().getHistoryData();
                                    List<HistoryDataItem> initialData = new ArrayList<>();
                                    HistoryRealTimeSecurityDataFragment fragment = fragmentMap.get(identifier);
                                    // 遍历数据点并提取时间和值
                                    for (HistoryDeviceDataResponse.DataPoint point : dataPoints) {
                                        String formattedTime = point.getFormattedTime();
                                        String value = point.getValue();
                                        if (identifier.equals("dr_state")) {
                                            value = value.equals("1") ? "打开" : "关闭";
                                        }

                                        initialData.add(new HistoryDataItem(identifier, formattedTime, value));
                                    }
                                    if (fragment != null) {
                                        fragment.loadData(initialData);
                                        Log.d("fetchRealTimeData", "已添加数据到 fragment: " + identifier);
                                    } else {
                                        Log.e("fetchRealTimeData", "找不到对应 fragment: " + identifier);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("fetchRealTimeData", "处理数据时出现错误", e);
                        }
                    }
                });
            }
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

    class HistoryDeviceDataResponse {

        private int code;
        private String msg;
        private Data data;

        public boolean isSuccess() {
            return code == 0 && data != null;
        }

        public Data getData() {
            return data;
        }

        public String getMsg() {
            return msg;
        }

        public class Data {
            private List<DataPoint> list;

            public List<DataPoint> getHistoryData() {
                return list != null ? list : Collections.emptyList();
            }
        }

        public class DataPoint {
            private long time;
            private String value;

            public long getTime() {
                return time;
            }

            public String getValue() {
                return value;
            }

            public String getFormattedTime() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                return sdf.format(new Date(time));
            }
        }
    }

    class SensorData {
        private String temperature;
        private String humidity;
        private String smoke;
        private String methane;
        private String flame;
        private String doorStatus;

        // Getter 和 Setter 方法
        public String getTemperature() {
            return temperature;
        }

        public void setTemperature(String temperature) {
            this.temperature = temperature;
        }

        public String getHumidity() {
            return humidity;
        }

        public void setHumidity(String humidity) {
            this.humidity = humidity;
        }

        public String getSmoke() {
            return smoke;
        }

        public void setSmoke(String smoke) {
            this.smoke = smoke;
        }

        public String getMethane() {
            return methane;
        }

        public void setMethane(String methane) {
            this.methane = methane;
        }

        public String getFlame() {
            return flame;
        }

        public void setFlame(String flame) {
            this.flame = flame;
        }

        public String getDoorStatus() {
            return doorStatus;
        }

        public void setDoorStatus(String doorStatus) {
            this.doorStatus = doorStatus;
        }
    }

    class SecurityViewPagerAdapter extends FragmentStateAdapter {

        private final List<Fragment> fragments;

        public SecurityViewPagerAdapter(@NonNull FragmentManager fragmentManager,
                                        @NonNull Lifecycle lifecycle,
                                        List<Fragment> fragments) {
            super(fragmentManager, lifecycle);
            this.fragments = fragments;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}
