package com.example.onenet_dateshow.onenet.pojo;

import com.google.gson.annotations.SerializedName;

public class AudioData {
    // JSON字段与Java字段映射（若字段名完全一致可省略@SerializedName）
    @SerializedName("filename")
    private String filename;

    @SerializedName("abnormal_class")
    private String abnormalClass;

    @SerializedName("timestamp")
    private String timestamp;

    // Getter 和 Setter 方法
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getAbnormalClass() {
        return abnormalClass;
    }

    public void setAbnormalClass(String abnormalClass) {
        this.abnormalClass = abnormalClass;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // 可选：重写toString方便调试
    @Override
    public String toString() {
        return "AudioData{" +
                "filename='" + filename + '\'' +
                ", abnormalClass='" + abnormalClass + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}