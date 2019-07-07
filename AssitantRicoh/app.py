# import required modules
import timeit

import numpy as np
from flask import Flask, render_template, Response, send_file
import requests
import cv2
from requests.auth import HTTPDigestAuth
import socket
import io


ip = "192.168.1.4"
url_base = "http://" + ip + "/osc/"
theta_id = "---.OSC"
theta_password = "****"

url = url_base + "commands/execute"

print(url)

payload = {"name": "camera.getLivePreview"}
response = requests.post(url, json=payload, stream=True,
                         auth=(HTTPDigestAuth(theta_id, theta_password)))

app = Flask(__name__)

@app.route('/')
def index():
    """Video streaming ."""
    return render_template('index.html')

@app.route('/download')
def download_file_assistant():
    # For windows you need to use drive name [ex: F:/Example.pdf]
    path = "static/assistant.zip"
    return send_file(path, as_attachment=True)


def gen():
    """Video streaming generator function."""
    if response.status_code == 200:
        bytes = b''
        t0 = timeit.default_timer()
        i = 0
        for block in response.iter_content(16384):
            bytes += block
            # Search the current block of bytes for the jpq start and end
            a = bytes.find(b'\xff\xd8')
            b = bytes.find(b'\xff\xd9')

            # If you have a jpg, write it to disk
            if a != - 1 and b != -1:
                jpg = bytes[a:b + 2]
                bytes = bytes[b + 2:]
                image = np.fromstring(jpg, dtype=np.uint8)
                img = cv2.imdecode(image, 1)
                cv2.imwrite('pic.jpg', img)
                yield (b'--frame\r\n'
                       b'Content-Type: image/jpeg\r\n\r\n' + open('pic.jpg', 'rb').read() + b'\r\n')
    else:
        print("Error", response.status_code)

    '''while True:
        frame = ricoh_camera_v.live_video_preview()
        
        
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + open('pic.jpg', 'rb').read() + b'\r\n')'''


@app.route('/video_feed')
def video_feed():
    """Video streaming route. Put this in the src attribute of an img tag."""
    return Response(gen(),
                    mimetype='multipart/x-mixed-replace; boundary=frame')


if __name__ == '__main__':
    app.run(host='192.168.1.3', port=80, debug=True)
