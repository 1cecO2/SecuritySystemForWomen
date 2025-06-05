#include <Wire.h>
#include <WiFi.h>
#include <driver/i2s.h>

// TCP服务器配置
const char* serverIP = "172.20.10.4";
const uint16_t serverPort = 12345;

// Wi-Fi配置（根据你的实际情况修改）
//const char* ssid = "MiFi-5A4E";
//const char* password = "1234567890";

//const char* ssid = "ZIHAN";
//const char* password = "11111111";

//const char* ssid = "kkkkk";
//const char* password = "1244759937";
const char* ssid = "hyuni";
const char* password = "Juyeon0115";

// I2S配置（INMP441麦克风参数）
#define I2S_PORT I2S_NUM_0
#define SAMPLE_RATE 16000
#define BITS_PER_SAMPLE 16
#define CHANNELS 1  // 单声道
#define SAMPLE_BUFFER_SIZE 256  // 256个16位样本（512字节）

WiFiClient client;

// 计算校验和函数（与服务器一致）
uint32_t calculate_checksum(const uint8_t* data, size_t length) {
    uint32_t checksum = 0;
    for (size_t i = 0; i < length; i++) {
        checksum += data[i];
    }
    return checksum & 0xFFFFFFFF;
}

// 将uint32_t转换为网络字节序（大端）
void uint32_to_network_bytes(uint32_t value, uint8_t* bytes) {
    bytes[0] = (value >> 24) & 0xFF;
    bytes[1] = (value >> 16) & 0xFF;
    bytes[2] = (value >> 8) & 0xFF;
    bytes[3] = value & 0xFF;
}

void setup() {
    Serial.begin(115200);
    
    // 连接Wi-Fi
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println("\nWiFi连接成功，IP地址: " + WiFi.localIP());
    
    // 初始化I2S麦克风
    i2s_config_t i2s_config = {
       .mode = (i2s_mode_t)(I2S_MODE_MASTER | I2S_MODE_RX),
       .sample_rate = SAMPLE_RATE,
       .bits_per_sample = (i2s_bits_per_sample_t)BITS_PER_SAMPLE,
       .channel_format = I2S_CHANNEL_FMT_ONLY_LEFT,
       .communication_format = I2S_COMM_FORMAT_I2S,
       .intr_alloc_flags = ESP_INTR_FLAG_LEVEL1,
       .dma_buf_count = 8,
       .dma_buf_len = SAMPLE_BUFFER_SIZE,  // DMA缓冲区样本数
       .use_apll = false
    };

    i2s_pin_config_t pin_config = {
        .bck_io_num = 13,    // BCLK引脚
        .ws_io_num = 14,     // LRCLK引脚
        .data_out_num = I2S_PIN_NO_CHANGE,
        .data_in_num = 15     // DATA引脚
    };

    i2s_driver_install(I2S_PORT, &i2s_config, 0, NULL);
    i2s_set_pin(I2S_PORT, &pin_config);
    i2s_zero_dma_buffer(I2S_PORT);

    // 连接TCP服务器
    connectToServer();
}

void loop() {
    if (!client.connected()) {
        Serial.println("TCP连接断开，尝试重连...");
        connectToServer();
        delay(1000);
        return;
    }

    int16_t audio_buffer[SAMPLE_BUFFER_SIZE];
    size_t bytes_read = sizeof(audio_buffer);  // 固定512字节（256样本×2字节）
    esp_err_t result = i2s_read(
        I2S_PORT,
        audio_buffer,
        bytes_read,
        &bytes_read,
        pdMS_TO_TICKS(100)
    );

    if (result == ESP_OK && bytes_read > 0) {
        // 构建数据包：4字节长度头 + 数据 + 4字节校验和
        uint8_t packet[4 + bytes_read + 4];
        
        // 设置长度头（网络字节序）
        uint32_t length = bytes_read;
        uint32_to_network_bytes(length, packet);
        
        // 复制音频数据到数据包
        memcpy(packet + 4, audio_buffer, bytes_read);
        
        // 计算校验和并设置（网络字节序）
        uint32_t checksum = calculate_checksum(packet + 4, bytes_read);
        uint32_to_network_bytes(checksum, packet + 4 + bytes_read);
        
        // 发送数据包
        client.write(packet, sizeof(packet));
        Serial.printf("已发送完整数据包：长度=%d，校验和=0x%08X\n", length, checksum);
    } else {
        Serial.println("I2S读取失败或超时");
    }
}

void connectToServer() {
    while (!client.connect(serverIP, serverPort)) {
        Serial.println("连接服务器失败，重试...");
        delay(1000);
    }
    Serial.println("TCP连接成功！");
}
