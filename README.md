# SecuritySystemForWomen

# 🛡️ 守望者 - 基于物联网与深度学习的独居女性智能安防系统

> **多模态融合安防解决方案** | 异常声音识别 + 暴力行为检测 + 传感器联动 | 边缘-云协同架构

**项目周期**：2025.03 - 2025.05 | **技术领域**：IoT × 深度学习 × 边缘计算

> **核心价值**：为独居女性提供全天候、多维度安全防护，实现异常事件秒级响应

## 🌟 系统亮点
- **三模态融合监测**：音频/视频/传感器数据协同分析
- **边缘智能优化**：YAMNet+MobileNetV2模型本地推理
- **工业级通信架构**：TCP+MQTT双协议保障数据传输
- **多终端支持**：Android APP + Web控制台 + PC监控
- **即插即用部署**：模块化设计支持快速安装

## 🧩 系统架构
```mermaid
graph TD
    A[传感层] -->|TCP音频流| B(边缘设备)
    C[视频层] -->|RTMP视频流| B
    D[传感器层] -->|WiFi数据| B
    B -->|MQTT| E[OneNET云平台]
    E --> F[Android APP]
    E --> G[Web控制台]
    E --> H[PC监控端]
    
    subgraph 边缘计算节点
        B --> I[音频处理]
        B --> J[视频分析]
        B --> K[传感器融合]
    end
