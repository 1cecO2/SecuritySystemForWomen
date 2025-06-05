package com.example.onenet_dateshow.fragments.audio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.example.onenet_dateshow.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AudioPlayFragment extends Fragment {

    private WebView webView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_audio_play, container, false);

        // 直接通过 findViewById 获取 WebView
        webView = root.findViewById(R.id.webView);
        setupWebView();
        loadAudioWebPage();

        return root;
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // 若需启用 JS（注意安全风险）
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(true); // 支持缩放（可选）
        webSettings.setBuiltInZoomControls(true); // 显示缩放按钮（可选）

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadData("音频监控页面加载失败，请检查网络", "text/html", "utf-8");
            }
        });
    }

    private void loadAudioWebPage() {
        String audioMonitorUrl = "https://2eeb9331.r19.vip.cpolar.cn/";
        webView.loadUrl(audioMonitorUrl);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
            webView.destroy(); // 销毁 WebView 避免内存泄漏
            webView = null;
        }
    }
}