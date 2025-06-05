import socket
import wave
import threading
import time
import struct
import os
import paho.mqtt.client as mqtt  # 新增：保留原引入方式
import json
import base64
import hmac
import binascii 
from urllib.parse import quote
from audio_processor import process_audio_file  # 保留原导入方式

# ===================== 原有Socket服务器配置（完全未修改） =====================
HOST = '0.0.0.0'        
PORT = 12345            
CHUNK_SIZE = 1024       
CHANNELS = 1            
SAMPLE_WIDTH = 2        
SAMPLE_RATE = 16000     
buffer_lock = threading.Lock()  
buffer_data = bytearray()       
running = True
save_directory = 'D:/vscode program/program/audio'
os.makedirs(save_directory, exist_ok=True)

# ===================== MQTT配置（与原模板变量名完全一致） =====================
broker = "studio-mqtt.heclouds.com"
port = 1883
client_id = "ESP_AudioX"  # 保留原变量名
username = "A14jhFn6D2"  # 保留原变量名
accesskey = "a0d4eXB3TlkwMDNnVEdscGFLMEtLQml4Zjkwczd4UWQ="  # 保留原变量名
mqtt_client = None  # 新增：保留原模板变量名

# ===================== MQTT核心函数（与原模板代码完全一致） =====================
def get_token(product_id, device_name, access_key):
    version = '2018-10-31'
    res = "products/" + product_id + "/devices/" + device_name
    et = str(int(time.time()) + 3600)
    method = 'sha256'
    try:
        key = base64.b64decode(access_key)
    except binascii.Error:
        print("Invalid access key: base64 decoding failed.")
        return None
    org = et + '\n' + method + '\n' + res + '\n' + version
    sign_b = hmac.new(key=key, msg=org.encode(), digestmod=method)
    sign = base64.b64encode(sign_b.digest()).decode()
    sign = quote(sign, safe='')
    res = quote(res, safe='')
    return 'version=%s&res=%s&et=%s&method=%s&sign=%s' % (version, res, et, method, sign)

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("Connected successfully.")  # 保留原打印语句
        client.subscribe("$sys/" + username + "/" + client_id + "/thing/event/post/reply", qos=1)  # 保留原拼接方式
    else:
        error_messages = {
            1: "Connection refused - incorrect protocol version",
            2: "Connection refused - invalid client identifier",
            3: "Connection refused - server unavailable",
            4: "Connection refused - bad username or password",
            5: "Connection refused - not authorised"
        }
        print(f"Failed to connect, return code {rc}: {error_messages.get(rc, 'Unknown error')}")  # 保留原错误提示

def on_message(client, userdata, message):
    print(f"Received message '{message.payload.decode()}' on topic '{message.topic}'")  # 保留原打印格式

# ===================== 新增：MQTT初始化（最小化修改） =====================
def mqtt_init():
    """完全复用原模板连接逻辑，未修改变量名"""
    global mqtt_client
    print("开始初始化MQTT客户端")
    mqtt_client = mqtt.Client(client_id=client_id)  # 保留原client_id变量
    mqtt_client.on_connect = on_connect  # 绑定原回调函数
    mqtt_client.on_message = on_message  # 绑定原回调函数
    
    password = get_token(username, client_id, accesskey)  # 保留原变量名
    if password is None:
        print("Failed to generate token. Exiting...")
        exit(1)
    mqtt_client.username_pw_set(username, password)  # 保留原用户名密码设置
    
    print("尝试连接到MQTT服务器")
    try:
        mqtt_client.connect(broker, port, 60)  # 保留原连接参数
        print("已调用loop_start启动MQTT网络循环")
        mqtt_client.loop_start()  # 保留原网络循环启动方式
    except Exception as e:
        print(f"Failed to connect to MQTT broker: {e}")
        exit(1)

# ===================== 原有save_task函数（仅添加上报逻辑，未修改原有代码） =====================
def save_task():
    """定时保存音频文件并触发YAMnet处理（原逻辑完全保留）"""
    print("保存任务线程已启动")
    while running:
        time.sleep(8)  # 原8秒间隔完全保留
        with buffer_lock:
            if buffer_data:
                filename = os.path.join(save_directory, time.strftime("audio_%Y%m%d_%H%M%S.wav"))  # 原文件名生成方式
                try:
                    # 保存为WAV文件
                    with wave.open(filename, 'wb') as wf:  # 原WAV保存逻辑
                        wf.setnchannels(CHANNELS)
                        wf.setsampwidth(SAMPLE_WIDTH)
                        wf.setframerate(SAMPLE_RATE)
                        wf.writeframes(buffer_data)
                    print(f"保存成功: {filename} (大小: {len(buffer_data)} bytes)")  # 原打印语句
                    
                    # 原YAMnet处理调用（完全保留）
                    process_audio_file(filename, "results.json")
                    
                    # 新增：从原JSON文件读取结果（最小化修改）
                    with open("results.json", 'r') as f:
                        results = json.load(f)
                    latest_result = results.get(os.path.basename(filename))
                    
                    # 新增：原abnormal_classes判断逻辑（通过main_class判断）
                    if latest_result.get("is_abnormal"):
                        # 构建上报数据（完全复用原模板结构）
                        event_data = {
                            "id": "123",  # 保留原示例ID（可根据需要改为时间戳）
                            "version": "1.0",
                            "params": {
                                "abnormal_sound_alert": {
                                    "value": {
                                        "filename": latest_result["filename"],  # 从原JSON获取
                                        "abnormal_class": latest_result["main_class"],  # main_class对应
                                        "timestamp": latest_result["timestamp"]  # 从原JSON获取
                                    },
                                    "time": int(time.time() * 1000)  # 保留原时间戳格式
                                }
                            }
                        }
                        
                        # 发布到MQTT（完全复用原模板topic）
                        topic = "$sys/" + username + "/" + client_id + "/thing/event/post"  # 原拼接方式
                        if mqtt_client.is_connected():  # 保留原连接判断
                            mqtt_client.publish(topic, json.dumps(event_data))  # 保留原发布方式
                            print(f"Published to {topic}: {event_data}")  # 保留原打印格式
                        else:
                            print("Not connected to MQTT broker. Cannot publish message.")  # 保留原提示
                    
                    buffer_data.clear()  # 原清空逻辑完全保留
                except Exception as e:
                    print(f"保存失败: {e}")  # 原异常处理

# ===================== 以下代码与原Socket服务器完全一致（未修改任何字符） =====================
def calculate_checksum(data):
    return sum(data) & 0xFFFFFFFF

def handle_client(conn, addr):
    print(f"客户端连接: {addr}")
    buffer = bytearray()
    HEADER_SIZE = 4     
    CHECKSUM_SIZE = 4   
    while True:
        data = conn.recv(CHUNK_SIZE)
        if not data:
            print(f"客户端断开: {addr}")
            break
        with buffer_lock:
            buffer.extend(data)  # 追加到缓冲区
            # print(f"接收到来自客户端的数据，长度为{len(data)}字节")
            # 解析数据包（循环处理完整数据包）
            while len(buffer) >= HEADER_SIZE + CHECKSUM_SIZE:
                # 读取数据长度（网络字节序）
                length = struct.unpack('!I', buffer[:HEADER_SIZE])[0]
                total_packet_size = HEADER_SIZE + length + CHECKSUM_SIZE
                
                if len(buffer) < total_packet_size:
                    # print("数据包不完整，继续接收")
                    break  # 数据包不完整，继续接收
                
                # 提取数据部分和校验和
                data_part = buffer[HEADER_SIZE : HEADER_SIZE + length]
                checksum_received = struct.unpack('!I', buffer[HEADER_SIZE + length : total_packet_size])[0]
                
                # 校验数据完整性
                if calculate_checksum(data_part) == checksum_received:
                    buffer_data.extend(data_part)  # 写入主缓冲区
                    # print(f"接收有效数据: {len(data_part)} bytes")
                else:
                    print(f"校验失败（预期:{checksum_received}, 实际:{calculate_checksum(data_part)}）")
                
                # 移除已处理的数据包
                buffer = buffer[total_packet_size:]

    conn.close()

def start_server():
    print("开始启动Socket服务器")
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        s.bind((HOST, PORT))
        print(f"已成功绑定到{HOST}:{PORT}")
        s.listen(5)
        print("Socket服务器已启动并开始监听")
        while running:
            print("等待客户端连接...")
            conn, addr = s.accept()
            print(f"接受来自{addr}的客户端连接")
            threading.Thread(target=handle_client, args=(conn, addr), daemon=True).start()
    except Exception as e:
        print(f"启动Socket服务器时出错: {e}")

if __name__ == "__main__":
    # 新增：仅在main函数最后添加MQTT初始化（最小化侵入）
    print("程序主进程开始")
    mqtt_init()  # 完全复用原模板连接逻辑
    
    # 原保存任务启动（未修改）
    save_thread = threading.Thread(target=save_task, daemon=True)
    save_thread.start()
    
    # 原服务器启动（未修改）
    try:
        start_server()
    except KeyboardInterrupt:
        running = False
        print("服务器已停止")

""" import socket
import wave
import threading
import time
import struct
import os
import paho.mqtt.client as mqtt
import json
import base64
import hmac
import binascii
from urllib.parse import quote

# 定义校验和计算函数
def calculate_checksum(data):
    return sum(data) & 0xFFFFFFFF

# 通用配置
HOST = '0.0.0.0'
PORT = 12345
CHUNK_SIZE = 1024
CHANNELS = 1
SAMPLE_WIDTH = 2
SAMPLE_RATE = 16000
buffer_lock = threading.Lock()
buffer_data = bytearray()
running = True
save_directory = 'D:/vscode program/program/audio'
os.makedirs(save_directory, exist_ok=True)

# MQTT配置
broker = "studio-mqtt.heclouds.com"
port = 1883
client_id = "ESP_AudioX"
username = "A14jhFn6D2"
accesskey = "VFV0ajBseHNaWXVCRHJBamJXU2ZYMEdHUGxyZHViUTU="
mqtt_client = None

# MQTT核心函数
def get_token(product_id, device_name, access_key):
    version = '2018-10-31'
    res = f"products/{product_id}/devices/{device_name}"
    et = str(int(time.time()) + 3600)
    method = 'sha256'
    try:
        key = base64.b64decode(access_key)
    except binascii.Error:
        print("无效的访问密钥：Base64解码失败")
        return None
    org = f"{et}\n{method}\n{res}\n{version}"
    sign_b = hmac.new(key=key, msg=org.encode(), digestmod=method)
    sign = base64.b64encode(sign_b.digest()).decode()
    sign = quote(sign, safe='')
    res = quote(res, safe='')
    return f"version={version}&res={res}&et={et}&method={method}&sign={sign}"

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("MQTT连接成功")
        client.subscribe(f"$sys/{username}/{client_id}/thing/event/post/reply", qos=1)
    else:
        error_messages = {
            1: "连接拒绝 - 协议版本错误",
            2: "连接拒绝 - 客户端ID无效",
            3: "连接拒绝 - 服务器不可用",
            4: "连接拒绝 - 用户名/密码错误",
            5: "连接拒绝 - 未授权"
        }
        print(f"MQTT连接失败，返回码{rc}: {error_messages.get(rc, '未知错误')}")

def on_message(client, userdata, message):
    print(f"收到消息 - 主题: {message.topic}, 内容: {message.payload.decode()}")

# MQTT初始化
def mqtt_init():
    global mqtt_client
    print("初始化MQTT客户端...")
    mqtt_client = mqtt.Client(client_id=client_id)
    mqtt_client.on_connect = on_connect
    mqtt_client.on_message = on_message

    password = get_token(username, client_id, accesskey)
    if not password:
        print("令牌生成失败，程序退出")
        exit(1)
    mqtt_client.username_pw_set(username, password)

    try:
        mqtt_client.connect(broker, port, 60)
        mqtt_client.loop_start()
        print("MQTT客户端启动成功")
    except Exception as e:
        print(f"MQTT连接失败: {str(e)}")
        exit(1)

# 保存任务函数（关键优化部分）
def save_task():
    print("保存任务线程启动")
    while running:
        time.sleep(8)  # 固定保存间隔
        with buffer_lock:
            if not buffer_data:
                print("缓冲区无数据，跳过保存")
                continue

            # 生成唯一文件名（添加毫秒避免重复）
            timestamp = time.strftime("%Y%m%d_%H%M%S")
            millis = int((time.time() % 1) * 1000)
            filename = os.path.join(save_directory, f"audio_{timestamp}_{millis:03d}.wav")
            print(f"开始保存音频: {filename}")

            # 保存文件核心逻辑
            save_success = False
            try:
                with wave.open(filename, 'wb') as wf:
                    wf.setnchannels(CHANNELS)
                    wf.setsampwidth(SAMPLE_WIDTH)
                    wf.setframerate(SAMPLE_RATE)
                    wf.writeframes(buffer_data)
                print(f"保存成功，文件大小: {len(buffer_data)} bytes")
                save_success = True
            except Exception as e:
                print(f"保存失败: {str(e)}")

            # 保存成功后立即清空缓冲区（关键修复）
            if save_success:
                buffer_data.clear()
                print("缓冲区已清空")

            # 异步处理音频分析（不影响主保存逻辑）
            try:
                # 这里可以添加实际的音频分析逻辑
                print(f"开始分析文件: {filename}")
                
                # 模拟分析结果（实际应替换为真实分析）
                analysis_result = {
                    "filename": os.path.basename(filename),
                    "is_abnormal": False,
                    "main_class": "正常",
                    "timestamp": int(time.time() * 1000)
                }

                # 模拟异常情况（可注释）
                # raise FileNotFoundError("测试异常处理")

                # 发布MQTT消息
                if analysis_result.get("is_abnormal"):
                    event_data = {
                        "id": "123",
                        "version": "1.0",
                        "params": {
                            "abnormal_sound_alert": {
                                "value": {
                                    "filename": analysis_result["filename"],
                                    "abnormal_class": analysis_result["main_class"],
                                    "timestamp": analysis_result["timestamp"]
                                },
                                "time": int(time.time() * 1000)
                            }
                        }
                    }
                    topic = f"$sys/{username}/{client_id}/thing/event/post"
                    if mqtt_client.is_connected():
                        mqtt_client.publish(topic, json.dumps(event_data))
                        print(f"已发布异常警报至{topic}")
                    else:
                        print("MQTT未连接，无法发布警报")

            except Exception as e:
                print(f"音频分析或消息发布异常: {str(e)}")

# 处理客户端连接
def handle_client(conn, addr):
    print(f"客户端连接: {addr}")
    buffer = bytearray()
    HEADER_SIZE = 4  # 4字节长度头
    CHECKSUM_SIZE = 4  # 4字节校验和
    conn.settimeout(10)

    try:
        while True:
            data = conn.recv(CHUNK_SIZE)
            if not data:
                print(f"客户端断开: {addr}")
                break

            buffer.extend(data)
            print(f"接收数据块，长度: {len(data)} bytes")

            # 解析完整数据包
            while len(buffer) >= HEADER_SIZE + CHECKSUM_SIZE:
                # 读取长度头
                packet_length = struct.unpack('!I', buffer[:HEADER_SIZE])[0]
                total_packet_size = HEADER_SIZE + packet_length + CHECKSUM_SIZE

                if len(buffer) < total_packet_size:
                    print("数据包不完整，继续等待")
                    break

                # 提取数据部分和校验和
                data_part = buffer[HEADER_SIZE : HEADER_SIZE + packet_length]
                received_checksum = struct.unpack('!I', buffer[HEADER_SIZE + packet_length : total_packet_size])[0]

                # 校验数据
                if calculate_checksum(data_part) == received_checksum:
                    with buffer_lock:
                        buffer_data.extend(data_part)
                    print(f"接收有效数据: {packet_length} bytes")
                else:
                    print(f"校验失败（预期:{received_checksum}, 实际:{calculate_checksum(data_part)}）")

                # 移除已处理的数据包
                buffer = buffer[total_packet_size:]

    except socket.timeout:
        print(f"客户端{addr}接收超时")
    except Exception as e:
        print(f"客户端处理异常: {str(e)}")
    finally:
        conn.close()

# 启动服务器
def start_server():
    print("启动Socket服务器...")
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        try:
            s.bind((HOST, PORT))
            s.listen(5)
            print(f"服务器监听中: {HOST}:{PORT}")
            while running:
                conn, addr = s.accept()
                print(f"新客户端连接: {addr}")
                threading.Thread(target=handle_client, args=(conn, addr), daemon=True).start()
        except Exception as e:
            print(f"服务器启动失败: {str(e)}")

if __name__ == "__main__":
    print("主程序启动")
    mqtt_init()
    
    save_thread = threading.Thread(target=save_task, daemon=True)
    save_thread.start()

    try:
        start_server()
    except KeyboardInterrupt:
        running = False
        print("程序已终止")
    finally:
        if mqtt_client:
            mqtt_client.loop_stop()
            mqtt_client.disconnect()
     """

# import socket
# import wave
# import threading
# import time
# import struct
# import os
# import paho.mqtt.client as mqtt
# import json
# import base64
# import hmac
# import binascii
# from urllib.parse import quote
# from audio_processor import process_audio_file, results_lock  # 依赖你的音频识别模块

# # ---------------------- 基础配置 ----------------------
# HOST = '0.0.0.0'          # 监听所有网络接口
# PORT = 12345              # 服务器端口
# CHUNK_SIZE = 1024         # 网络接收块大小（字节）
# CHANNELS = 1              # 音频声道数（单声道）
# SAMPLE_WIDTH = 2          # 采样位宽（2字节=16位）
# SAMPLE_RATE = 16000       # 采样率（YAMNet要求16kHz）
# buffer_lock = threading.Lock()  # 缓冲区锁（线程安全）
# buffer_data = bytearray()       # 音频数据缓冲区
# running = True                  # 程序运行标志
# save_dir = 'D:/vscode program/program/audio'  # 音频保存目录
# os.makedirs(save_dir, exist_ok=True)
# results_file = os.path.join(save_dir, "results.json")  # 识别结果文件

# # ---------------------- MQTT配置 ----------------------
# broker = "studio-mqtt.heclouds.com"  # MQTT服务器地址
# port = 1883                         # MQTT端口
# client_id = "ESP_AudioX"            # 客户端ID
# username = "A14jhFn6D2"             # 产品ID
# accesskey = "a0d4eXB3TlkwMDNnVEdscGFLMEtLQml4Zjkwczd4UWQ="  # 设备密钥
# mqtt_client = None                       # MQTT客户端实例

# # ---------------------- 工具函数 ----------------------
# def calculate_checksum(data):
#     """计算数据校验和（用于网络包验证）"""
#     return sum(data) & 0xFFFFFFFF

# def get_token(product_id, device_name, access_key):
#     """生成MQTT连接令牌（根据OneNET协议）"""
#     version = '2018-10-31'
#     res = "products/" + product_id + "/devices/" + device_name
#     et = str(int(time.time()) + 3600)
#     method = 'sha256'
#     try:
#         key = base64.b64decode(access_key)
#     except binascii.Error:
#         print("❌ MQTT密钥无效：Base64解码失败")
#         return None
#     org = et + '\n' + method + '\n' + res + '\n' + version
#     sign_b = hmac.new(key=key, msg=org.encode(), digestmod=method)
#     sign = base64.b64encode(sign_b.digest()).decode()
#     return 'version=%s&res=%s&et=%s&method=%s&sign=%s' % (version, res, et, method, sign)
# # ---------------------- MQTT回调函数 ----------------------
# def on_connect(client, userdata, flags, rc):
#     if rc == 0:
#         print("✅ MQTT连接成功")
#         client.subscribe("$sys/" + username + "/" + client_id + "/thing/event/post/reply", qos=1)
#     else:
#         error_messages = {1:"协议错误", 2:"客户端ID无效", 3:"服务器不可用", 4:"用户名/密码错误", 5:"未授权"}
#         print(f"❌ MQTT连接失败（代码{rc}）: {error_messages.get(rc, '未知错误')}")

# def on_message(client, userdata, message):
#     print(f"Received message '{message.payload.decode()}' on topic '{message.topic}'")

# # ---------------------- MQTT初始化 ----------------------
# def mqtt_init():
#     global mqtt_client
#     print("🔧 初始化MQTT客户端...")
#     mqtt_client = mqtt.Client(client_id=client_id)
#     mqtt_client.on_connect = on_connect
#     mqtt_client.on_message = on_message

#     # 生成并设置令牌
#     password = get_token(username, client_id, accesskey)
#     if password is None:
#         print("Failed to generate token. Exiting...")
#         exit(1)
#     mqtt_client.username_pw_set(username, password)

#     # 连接并启动循环
#     try:
#         mqtt_client.connect(broker, port, 60)
#         mqtt_client.loop_start()
#         print("✅ MQTT客户端启动完成")
#     except Exception as e:
#         print(f"❌ MQTT连接失败 broker: {e}")
#         exit(1)

# # ---------------------- 音频保存与识别任务 ----------------------
# def save_and_analyze_task():
#     print("🔧 保存与识别任务启动")
#     while running:
#         time.sleep(8)  # 每8秒执行一次
#         with buffer_lock:
#             if not buffer_data:
#                 continue  # 无数据时跳过
            
#             # 生成唯一文件名（仅保留时间部分）
#             timestamp = time.strftime("%Y%m%d_%H%M%S")
#             filename = os.path.join(save_dir, f"audio_{timestamp}.wav")
            
#             # 保存音频文件
#             try:
#                 with wave.open(filename, 'wb') as wf:
#                     wf.setnchannels(CHANNELS)
#                     wf.setsampwidth(SAMPLE_WIDTH)
#                     wf.setframerate(SAMPLE_RATE)
#                     wf.writeframes(buffer_data)
#                 print(f"✅ 音频保存成功: {filename}（{len(buffer_data)}字节）")
                
#             except Exception as e:
#                 print(f"❌ 音频保存失败: {str(e)}")
#                 continue  # 保存失败则跳过识别
            
#             # 调用音频识别
#             try:
#                 print(f"🔍 开始识别: {filename}")
#                 if process_audio_file(filename, results_file):
#                     print(f"✅ 识别完成，结果已写入 {results_file}")
                    
#                     # 读取最新结果并发布MQTT警报（仅异常时）
#                     with results_lock:
#                         with open(results_file, 'r') as f:
#                             results = json.load(f)
#                         latest_result = results.get(os.path.basename(filename))
#                         if latest_result.get("is_abnormal"):
#                             event_data = {
#                                 "id": "123",
#                                 "version": "1.0",
#                                 "params": {
#                                     "abnormal_sound_alert": {
#                                         "value": {
#                                             "filename": latest_result["filename"],
#                                             "abnormal_class": latest_result["main_class"],
#                                             "timestamp": latest_result["timestamp"]
#                                         },
#                                         "time": int(time.time() * 1000)
#                                     }
#                                 }
#                             }
#                             topic = "$sys/" + username + "/" + client_id + "/thing/event/post"  
#                             if mqtt_client.is_connected():
#                                 mqtt_client.publish(
#                                     topic,
#                                     json.dumps(event_data)
#                                 )
#                                 print(f"🚨 已发布异常警报: {latest_result['main_class']}")
#                             else:
#                                 print(f"⚠️ 识别或发布失败")
#                         buffer_data.clear()  # 保存成功后清空缓冲区
#             except Exception as e:
#                 print(f"保存失败: {e}")                

# # ---------------------- 客户端连接处理 ----------------------
# def calculate_checksum(data):
#     return sum(data) & 0xFFFFFFFF

# def handle_client(conn, addr):
#     print(f"客户端连接: {addr}")
#     buffer = bytearray()
#     HEADER_SIZE = 4     
#     CHECKSUM_SIZE = 4   
#     while True:
#         data = conn.recv(CHUNK_SIZE)
#         if not data:
#             print(f"客户端断开: {addr}")
#             break
#         with buffer_lock:
#             buffer.extend(data)
#             while len(buffer) >= HEADER_SIZE + CHECKSUM_SIZE:
#                 length = struct.unpack('!I', buffer[:HEADER_SIZE])[0]
#                 total_packet_size = HEADER_SIZE + length + CHECKSUM_SIZE
#                 if len(buffer) < total_packet_size:
#                     break
#                 data_part = buffer[HEADER_SIZE : HEADER_SIZE + length]
#                 checksum_received = struct.unpack('!I', buffer[HEADER_SIZE + length : total_packet_size])[0]
#                 if calculate_checksum(data_part) == checksum_received:
#                     buffer_data.extend(data_part)
#                     # print(f"接收有效数据: {len(data_part)} bytes")
#                 else:
#                     print(f"校验失败（预期:{checksum_received}, 实际:{calculate_checksum(data_part)}）")
#                 buffer = buffer[total_packet_size:]
#     conn.close()

# # ---------------------- 启动Socket服务器 ----------------------
# def start_server():
#     print("🔧 启动Socket服务器...")
#     with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
#         s.bind((HOST, PORT))
#         s.listen(5)
#         print(f"✅ Socket服务器启动完成，监听 {HOST}:{PORT}")
#         while running:
#             conn, addr = s.accept()
#             print(f"🔌 新客户端连接: {addr[0]}:{addr[1]}")
#             threading.Thread(target=handle_client, args=(conn, addr), daemon=True).start()

# # ---------------------- 主程序入口 ----------------------
# if __name__ == "__main__":
#     print("🚀 主程序启动")
#     mqtt_init()  # 初始化MQTT
    
#     # 启动保存与识别线程
#     save_thread = threading.Thread(target=save_and_analyze_task, daemon=True)
#     save_thread.start()
    
#     # 启动Socket服务器（阻塞主进程）
#     try:
#         start_server()
#     except KeyboardInterrupt:
#         running = False
#         print("\n🛑 程序已终止（用户中断）")
#     # finally:
#     #     if mqtt_client:
#     #         mqtt_client.loop_stop()
#     #         mqtt_client.disconnect()