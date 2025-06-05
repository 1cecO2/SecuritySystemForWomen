package com.example.onenet_dateshow.fragments.video;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.example.onenet_dateshow.R;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.video.ListGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

public class VideoPlayFragment extends Fragment implements FlvFileAdapter.OnItemClickListener {

    private static final String TAG = "VideoPlayFragment";
    private NormalGSYVideoPlayer rtmpPlayer;
    private OrientationUtils orientationUtils;
    private ListGSYVideoPlayer flvListPlayer;

    // 配置项（需替换为实际地址）
    // private final String RTMP_LIVE_URL = "rtmp://liteavapp.qcloud.com/live/liteavdemoplayerstreamid";
    private final String RTMP_LIVE_URL = "rtmp://47.94.236.247/live/stream";
    private final String FLV_FOLDER_URL = "https://47.94.236.247/rec/";

    private List<String> flvVideoUrls = new ArrayList<>(); // 完整视频URL列表
    private List<String> flvFileNames = new ArrayList<>(); // 显示用文件名列表

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 加载布局文件
        return inflater.inflate(R.layout.layout_video_play, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化RTMP直播播放器
        initRTMPPlayer(view);
        // 初始化FLV列表播放器
        flvListPlayer = view.findViewById(R.id.detail_player);
        // 异步获取FLV文件列表
        new FetchFlvFileTask().execute(FLV_FOLDER_URL);

        // 设置历史视频按钮点击事件
        Button historyButton = view.findViewById(R.id.btn_show_history_dialog);
        historyButton.setOnClickListener(v -> showHistoryVideoDialog());
    }

    /**
     * 初始化RTMP直播播放器
     */
    private void initRTMPPlayer(View view) {
        rtmpPlayer = view.findViewById(R.id.video_player);
        orientationUtils = new OrientationUtils(requireActivity(), rtmpPlayer);

        rtmpPlayer.setUp(
                RTMP_LIVE_URL,
                false,
                "实时监控画面"
        );
        rtmpPlayer.getFullscreenButton().setOnClickListener(v ->
                orientationUtils.resolveByClick()
        );
        rtmpPlayer.startPlayLogic();
    }

    /**
     * 显示历史视频列表弹窗
     */
    private void showHistoryVideoDialog() {
        if (flvFileNames.isEmpty()) {
            Toast.makeText(requireContext(), "没有可用的历史视频", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_history_video, null);
        builder.setView(dialogView);

        // 初始化RecyclerView
        RecyclerView historyRecyclerView = dialogView.findViewById(R.id.rv_history_list);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        FlvFileAdapter dialogAdapter = new FlvFileAdapter(flvFileNames, this);
        historyRecyclerView.setAdapter(dialogAdapter);

        // 处理退出按钮
        Button exitButton = dialogView.findViewById(R.id.btn_exit);
        exitButton.setOnClickListener(v -> ((AlertDialog) v.getTag()).dismiss());

        AlertDialog dialog = builder.create();
        exitButton.setTag(dialog);
        dialog.show();
    }

    /**
     * 异步获取FLV文件列表任务
     */
    private class FetchFlvFileTask extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... params) {
            try {
                // 信任所有SSL证书（生产环境需移除）
                trustAllCertificates();

                Document doc = Jsoup.connect(params[0]).timeout(20000).get();
                Elements links = doc.select("a[href$=.flv]"); // 匹配FLV文件

                flvVideoUrls.clear();
                flvFileNames.clear();

                for (Element link : links) {
                    String href = link.attr("href");
                    String fullUrl = buildFullUrl(params[0], href);
                    String fileName = extractFileName(href);

                    if (isValidFlvFile(fullUrl, fileName)) {
                        flvVideoUrls.add(fullUrl);
                        flvFileNames.add(fileName);
                    }
                }
                return flvFileNames;

            } catch (Exception e) {
                Log.e(TAG, "文件列表获取失败", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> result) {
            if (result != null && !flvFileNames.isEmpty()) {
                Log.d(TAG, "成功获取" + flvFileNames.size() + "个历史视频");
            } else {
                Toast.makeText(requireContext(), "未找到历史视频文件", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 文件路径处理工具方法
    private String buildFullUrl(String baseUrl, String href) {
        return baseUrl.endsWith("/") ? baseUrl + href : baseUrl + "/" + href;
    }

    private String extractFileName(String href) {
        return href.substring(href.lastIndexOf("/") + 1);
    }

    private boolean isValidFlvFile(String url, String name) {
        return !name.isEmpty() && name.toLowerCase().endsWith(".flv") && !url.contains("..");
    }

    // 信任所有SSL证书（仅测试环境使用）
    private void trustAllCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] chain, String authType) { /* 不验证 */ }
                    public void checkServerTrusted(X509Certificate[] chain, String authType) { /* 不验证 */ }
                }
        };
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    // RecyclerView点击事件处理
    @Override
    public void onItemClick(int position) {
        if (position < 0 || position >= flvVideoUrls.size()) return;

        String videoUrl = flvVideoUrls.get(position);
        String videoTitle = flvFileNames.get(position);

        // 释放当前资源并播放新视频
        releasePlayer();
        initFlvPlayer(videoUrl, videoTitle);
    }

    private void initFlvPlayer(String url, String title) {
        flvListPlayer.setUp(
                url,
                false,
                "历史视频 - " + title
        );
        flvListPlayer.startPlayLogic();

        // 设置播放完成回调
        flvListPlayer.setVideoAllCallBack(new GSYSampleCallBack() {
            @Override
            public void onAutoComplete(String url, Object... objects) {
                int currentIndex = flvVideoUrls.indexOf(url);
                if (currentIndex < flvVideoUrls.size() - 1) {
                    onItemClick(currentIndex + 1); // 播放下一个
                }
            }
        });
    }

    private void releasePlayer() {
        if (flvListPlayer != null) {
            flvListPlayer.release();
        }
    }

    // 生命周期处理
    @Override
    public void onPause() {
        super.onPause();
        if (rtmpPlayer != null) rtmpPlayer.onVideoPause();
        if (flvListPlayer != null) flvListPlayer.onVideoPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rtmpPlayer != null) rtmpPlayer.onVideoResume();
        if (flvListPlayer != null) flvListPlayer.onVideoResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (orientationUtils != null) orientationUtils.releaseListener();
        releasePlayer();
        if (rtmpPlayer != null) rtmpPlayer.release();
    }
}