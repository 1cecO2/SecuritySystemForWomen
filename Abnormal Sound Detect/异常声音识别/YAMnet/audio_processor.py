# import os
# import json
# import tensorflow as tf
# import tensorflow_hub as hub
# import numpy as np
# import csv
# from scipy.io import wavfile
# import scipy.signal
# from threading import Lock

# # 全局锁（防止多线程写入结果文件冲突）
# results_lock = Lock()

# # ====================== 关键修改：显式设置模型缓存目录 ======================
# # 指定模型缓存路径（建议放在项目目录下，避免被系统清理）
# TFHUB_CACHE_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "tfhub_cache")
# os.makedirs(TFHUB_CACHE_DIR, exist_ok=True)  # 自动创建目录
# os.environ["TFHUB_CACHE_DIR"] = TFHUB_CACHE_DIR  # 覆盖默认缓存路径
# # ==========================================================================

# # 初始化YAMnet模型（添加异常处理）
# try:
#     model = hub.load('https://tfhub.dev/google/yamnet/1')
# except Exception as e:
#     raise RuntimeError(
#         f"加载YAMNet模型失败！请检查：\n"
#         f"1. 网络是否可访问 tfhub.dev（可能需要科学上网）\n"
#         f"2. 缓存目录是否有写入权限：{TFHUB_CACHE_DIR}\n"
#         f"3. 若手动下载模型，需将模型文件放入 {TFHUB_CACHE_DIR} 目录下\n"
#         f"错误详情：{str(e)}"
#     )

# # 加载声音类别名称（添加异常处理）
# class_names = []
# try:
#     with open(model.class_map_path().numpy(), 'r') as csvfile:
#         class_names = [row['display_name'] for row in csv.DictReader(csvfile)]
# except Exception as e:
#     raise RuntimeError(f"加载声音类别名称失败！错误详情：{str(e)}")

# # 异常声音类别列表（根据需求调整）
# abnormal_classes = ['Scream', 'Clap', 'Glass breaking', 'Crying', 'Slosh',
#                     'Explosion', 'Crack', 'Crackle', 'Slap, smack',
#                     'Crying, sobbing', 'Wail, moan', 'Screaming',
#                     'Chink, clink', 'Glass']

# def process_audio_file(filename, results_file):
#     """处理单个音频文件并保存识别结果"""
#     try:
#         # 读取音频文件
#         sample_rate, wav_data = wavfile.read(filename)
        
#         # 重采样到16kHz（YAMnet要求）
#         if sample_rate != 16000:
#             target_length = int(len(wav_data) * 16000 / sample_rate)
#             wav_data = scipy.signal.resample(wav_data, target_length)
#             sample_rate = 16000
        
#         # 归一化波形数据
#         waveform = wav_data / np.iinfo(np.int16).max
        
#         # 运行YAMnet模型
#         scores, _, _ = model(waveform)
#         scores_np = scores.numpy()
        
#         # 获取主要声音类别
#         main_class_idx = np.argmax(scores_np.mean(axis=0))
#         main_class = class_names[main_class_idx]
        
#         # 构建结果字典
#         result = {
#             "filename": os.path.basename(filename),
#             "main_class": main_class,
#             "is_abnormal": main_class in abnormal_classes,
#             "duration": len(wav_data) / sample_rate,  # 时长（秒）
#             "timestamp": os.path.basename(filename)[6:-4]  # 从文件名提取时间戳
#         }
        
#         # 写入结果文件（加锁防止多线程冲突）
#         with results_lock:
#             try:
#                 with open(results_file, 'r') as f:
#                     results = json.load(f)
#             except (FileNotFoundError, json.JSONDecodeError):
#                 results = {}  # 文件不存在或为空时初始化
            
#             results[result["filename"]] = result  # 更新结果
            
#             with open(results_file, 'w') as f:
#                 json.dump(results, f, indent=2)
        
#         return True
    
#     except Exception as e:
#         print(f"处理文件 {filename} 失败: {str(e)}")
#         return False

# def process_audio_directory(audio_dir, results_file):
#     """处理目录下所有未处理的音频文件"""
#     for fname in os.listdir(audio_dir):
#         if fname.startswith('audio_') and fname.endswith('.wav'):
#             full_path = os.path.join(audio_dir, fname)
#             # 检查是否已处理（通过结果文件判断）
#             with results_lock:
#                 try:
#                     with open(results_file, 'r') as f:
#                         if fname in json.load(f):
#                             continue  # 已处理过则跳过
#                 except:
#                     pass  # 文件不存在时继续处理
#             process_audio_file(full_path, results_file)

import os
import json
import tensorflow as tf
import tensorflow_hub as hub
import numpy as np
import csv
from scipy.io import wavfile
import scipy.signal
from threading import Lock

# 全局锁（防止多线程写入结果文件冲突）
results_lock = Lock()

# 模型缓存目录（自动定位到脚本同级目录的tfhub_cache）
TFHUB_CACHE_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "tfhub_cache")
os.makedirs(TFHUB_CACHE_DIR, exist_ok=True)
os.environ["TFHUB_CACHE_DIR"] = TFHUB_CACHE_DIR  # 告知TensorFlow Hub缓存位置

# ---------------------- 关键修改：加载本地模型（替换URL为本地路径） ----------------------
# 本地模型路径（与TF Hub模型URL路径严格对应：https://tfhub.dev/google/yamnet/1 → google/yamnet/1）
LOCAL_MODEL_PATH = os.path.join(TFHUB_CACHE_DIR, "google/yamnet/1")

# 检查本地模型文件是否存在（确保路径正确）
if not os.path.exists(os.path.join(LOCAL_MODEL_PATH, "saved_model.pb")):
    raise FileNotFoundError(
        f"❌ 本地模型文件未找到！\n"
        f"请确认模型已解压到：\n{LOCAL_MODEL_PATH}\n"
        f"（需包含saved_model.pb和variables文件夹）"
    )

# 加载本地模型（不再使用网络URL，直接指向本地路径）
try:
    print(f"🔧 从本地加载模型：{LOCAL_MODEL_PATH}")
    model = hub.load(LOCAL_MODEL_PATH)  # 核心修改：替换为本地路径
    print("✅ YAMNet模型加载成功（本地模式）")
except Exception as e:
    raise RuntimeError(
        f"❗️加载本地模型失败！\n"
        f"错误详情：{str(e)}\n"
        f"请检查模型路径是否正确：{LOCAL_MODEL_PATH}"
    )
# --------------------------------------------------------------------------------------

# 加载声音类别名称（从模型中获取类别映射文件路径）
class_names = []
try:
    class_map_path = model.class_map_path().numpy()  # 获取模型自带的类别映射文件
    with open(class_map_path, 'r') as csvfile:
        class_names = [row['display_name'] for row in csv.DictReader(csvfile)]
    print(f"📚 加载{len(class_names)}个声音类别名称")
except Exception as e:
    raise RuntimeError(f"加载类别名称失败：{str(e)}")

# 异常声音类别列表（根据需求调整）
abnormal_classes = ['Scream', 'Clap', 'Glass breaking', 'Crying', 'Slosh',
                    'Explosion', 'Crack', 'Crackle', 'Slap, smack',
                    'Crying, sobbing', 'Wail, moan', 'Screaming',
                    'Chink, clink', 'Glass','Cap Gun','Gasp','Sigh','Whimper']

def process_audio_file(filename, results_file):
    print(f"开始处理文件: {filename}")
    try:
        # 读取音频文件
        sample_rate, wav_data = wavfile.read(filename)
        
        # 重采样到16kHz（YAMnet要求）
        if sample_rate != 16000:
            target_length = int(len(wav_data) * 16000 / sample_rate)
            wav_data = scipy.signal.resample(wav_data, target_length)
            sample_rate = 16000
        
        # 归一化波形数据
        waveform = wav_data / np.iinfo(np.int16).max
        
        # 运行YAMnet模型
        scores, _, _ = model(waveform)
        scores_np = scores.numpy()
        
        # 获取主要声音类别
        main_class_idx = np.argmax(scores_np.mean(axis=0))
        main_class = class_names[main_class_idx]
        
        # 构建结果字典
        result = {
            "filename": os.path.basename(filename),
            "main_class": main_class,
            "is_abnormal": main_class in abnormal_classes,
            "duration": len(wav_data) / sample_rate,  # 时长（秒）
            "timestamp": os.path.basename(filename)[6:-4]  # 从文件名提取时间戳
        }
        
        # 写入结果文件（加锁防止多线程冲突）
        with results_lock:
            try:
                # 保存到原始路径
                with open(results_file, 'r') as f:
                    results = json.load(f)
            except (FileNotFoundError, json.JSONDecodeError):
                results = {}  # 文件不存在或为空时初始化
            
            results[result["filename"]] = result  # 更新结果
            
            # 保存到audio目录下的results.json
            audio_results_file = os.path.join(os.path.dirname(filename), "results.json")
            try:
                with open(audio_results_file, 'r') as f:
                    audio_results = json.load(f)
            except (FileNotFoundError, json.JSONDecodeError):
                audio_results = {}
            audio_results[result["filename"]] = result
            with open(audio_results_file, 'w') as f:
                json.dump(audio_results, f, indent=2)
            
            # 保存到YAMnet目录下的results.json
            yamnet_results_file = os.path.join("D:/vscode program/program/YAMnet", "results.json")
            try:
                with open(yamnet_results_file, 'r') as f:
                    yamnet_results = json.load(f)
            except (FileNotFoundError, json.JSONDecodeError):
                yamnet_results = {}
            yamnet_results[result["filename"]] = result
            with open(yamnet_results_file, 'w') as f:
                json.dump(yamnet_results, f, indent=2)
            
            with open(results_file, 'w') as f:
                json.dump(results, f, indent=2)
        
        print(f"处理文件 {filename} 成功")
        return True
    
    except Exception as e:
        print(f"处理文件 {filename} 失败: {str(e)}")
        return False

def process_audio_directory(audio_dir, results_file):
    print(f"开始处理目录: {audio_dir}")
    if not os.path.exists(audio_dir):
        print(f"音频目录 {audio_dir} 不存在")
        return
    save_dir = "D:/vscode program/program/YAMnet"  # 结果保存目录（根据你的实际路径修改）
    os.makedirs(save_dir, exist_ok=True)
    full_results_file = os.path.join(save_dir, results_file)
    
    for fname in os.listdir(audio_dir):
        if fname.startswith('audio_') and fname.endswith('.wav'):
            full_path = os.path.join(audio_dir, fname)
            
            # 检查是否已处理（通过结果文件判断）
            with results_lock:
                try:
                    with open(full_results_file, 'r') as f:
                        if fname in json.load(f):
                            print(f"文件 {fname} 已处理，跳过")
                            continue  # 已处理过则跳过
                except (FileNotFoundError, json.JSONDecodeError):
                    pass  # 文件不存在或格式不正确时继续处理
            
            process_audio_file(full_path, full_results_file)
    
    print(f"目录 {audio_dir} 处理完成")

# 示例调用
if __name__ == "__main__":
    audio_directory = "D:/vscode program/program/audio"  # 音频文件目录（根据实际修改）
    results_file_name = "results.json"
    process_audio_directory(audio_directory, results_file_name)