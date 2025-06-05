from flask import Flask, render_template, jsonify, send_from_directory
from flask_cors import CORS
import os
import json
from datetime import datetime
from threading import Thread
import time

app = Flask(__name__)
CORS(app)
# 修正：results.json与音频在同一目录（D:\vscode program\program\audio）
app.config['AUDIO_FOLDER'] = 'D:/vscode program/program/audio'
app.config['RESULTS_FILE'] = os.path.join(app.config['AUDIO_FOLDER'], 'results.json')  # 关键路径修正

# 初始化results.json（如果不存在）
if not os.path.exists(app.config['RESULTS_FILE']):
    with open(app.config['RESULTS_FILE'], 'w') as f:
        json.dump({}, f)

def background_processor():
    while True:
        # 注意：移除test_data，仅保留空循环（实际应替换为你的真实音频处理逻辑）
        # 例如：如果你的音频处理脚本会自动生成results.json，此线程可保留用于触发处理
        time.sleep(30)  # 每30秒检查一次（根据实际需求调整）

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/audio/<path:filename>')
def serve_audio(filename):
    # 确保音频文件存在
    audio_path = os.path.join(app.config['AUDIO_FOLDER'], filename)
    if not os.path.exists(audio_path):
        return "文件不存在", 404
    return send_from_directory(app.config['AUDIO_FOLDER'], filename)

@app.route('/api/audio-list')
def get_audio_list():
    try:
        if not os.path.isdir(app.config['AUDIO_FOLDER']):
            return jsonify({"error": "音频目录不存在", "path": app.config['AUDIO_FOLDER']}), 404

        audio_files = []
        # 按时间倒序排序（最新的音频在前）
        for fname in sorted(os.listdir(app.config['AUDIO_FOLDER']), key=lambda x: os.path.getctime(os.path.join(app.config['AUDIO_FOLDER'], x)), reverse=True):
            if fname.endswith('.wav'):
                try:
                    # 解析文件名中的时间戳（假设文件名格式为audio_时间戳.wav，如audio_20250512_145922_112.wav）
                    timestamp_str = fname.split('_')[-1].split('.')[0]  # 提取时间戳部分（如20250512_145922）
                    dt = datetime.strptime(timestamp_str, "%Y%m%d_%H%M%S")
                except:
                    # 解析失败时使用文件创建时间
                    dt = datetime.fromtimestamp(os.path.getctime(os.path.join(app.config['AUDIO_FOLDER'], fname)))
                audio_files.append({
                    "filename": fname,  # 与results.json中的键一致（含.wav）
                    "timestamp": dt.strftime("%Y-%m-%d %H:%M:%S"),  # 格式化时间戳
                    "url": f"/audio/{fname}"  # 音频文件访问路径
                })
        return jsonify(audio_files)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/results')
def get_results():
    try:
        with open(app.config['RESULTS_FILE'], 'r') as f:
            raw_data = json.load(f)
            
            processed_data = {}
            for filename, info in raw_data.items():
                processed_data[filename] = {
                    "main_class": info["main_class"],  # 直接使用main_class
                    "status": "异常" if info["is_abnormal"] else "正常",  # 转换状态
                    "duration": info["duration"],
                    "timestamp": info["timestamp"]
                }
            return jsonify(processed_data)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    # 启动后台线程（仅用于示例，实际需替换为你的音频处理逻辑）
    processor_thread = Thread(target=background_processor, daemon=True)
    processor_thread.start()
    app.run(host='0.0.0.0', port=5000, debug=True)