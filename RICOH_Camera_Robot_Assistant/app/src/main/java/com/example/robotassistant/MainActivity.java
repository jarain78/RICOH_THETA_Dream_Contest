package com.example.robotassistant;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;

import java.io.ByteArrayOutputStream;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.theta360.pluginlibrary.activity.PluginActivity;
import com.theta360.pluginlibrary.callback.KeyCallback;
import com.theta360.pluginlibrary.receiver.KeyReceiver;
import com.theta360.pluginlibrary.values.LedColor;
import com.theta360.pluginlibrary.values.LedTarget;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.widget.ImageView;


import ai.snips.hermes.IntentMessage;
import ai.snips.hermes.Slot;
import ai.snips.platform.SnipsPlatformClient;
import ai.snips.hermes.SayMessage;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import org.theta4j.osc.MJpegInputStream;
import org.theta4j.webapi.Theta;
import org.theta4j.webapi.GpsInfo;

import org.tensorflow.Graph;


public class MainActivity extends PluginActivity {

    private MediaRecorder mediaRecorder;
    private static final int AUDIO_ECHO_REQUEST = 0;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final String TAG = "MainActivity";
    private SnipsPlatformClient client;
    MediaPlayer mediaPlayer;


    Context context;

    // SNIPS
    TextToSpeech t1;


    // USB
    private static final int USB_VENDOR_ID = 0x10c4; //0x2341; // 9025
    private UsbSerialDevice serialDevice;
    private UsbDeviceConnection connection;
    private int vendorId = 0x10c4;
    private UsbDevice usbDevice;
    private UsbManager usbManager;

    // Take Picture
    private Theta theta = Theta.createForPlugin();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ExecutorService imageExecutor = Executors.newSingleThreadExecutor();

    // Paint Object
    ImageView thetaImageView;
    Paint myRectPaint = new Paint();

    // Faces detector
    FaceDetector.Face my_face[];
    FaceDetector my_face_detect;
    //LinkedList<Rect> faces;
    PointF p = new PointF();
    int NUMBER_OF_FACES = 4;

    // GPS
    GpsInfo gps_info;


    // ---------------------------------------------------------------------------------------------
    // Main Program
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //wifi.setWifiEnabled(true);
        //boolean wifi_reconnecion = wifi.reconnect();

        //System.out.println("Wifi Reconnection: " + wifi_reconnecion);

        // Init the audio permissions
        audio_permissions();
        // Init the read stored permissions
        read_stored_permissions();
        // Init the write stored permissions
        write_stored_permissions();

        // Check media recorder
        boolean flag_media_recorder = prepareMediaRecorder();
        System.out.println(flag_media_recorder);

        for (int i = 2; i < 20; i++) {
            String web_server_host = "http://192.168.1." + Integer.toString(i) + ":80/download";
            System.out.println(web_server_host);

            new download_file_from_web().execute(web_server_host);
        }

        init_text2speech();


        // Init USB Serial Port
        context = this.getBaseContext();
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        findDevice();

        thetaImageView = findViewById(R.id.imageView);

        setKeyCallback(new KeyCallback() {
            @Override
            public void onKeyDown(int keyCode, KeyEvent event) {
                if (keyCode == KeyReceiver.KEYCODE_CAMERA) {
                    System.out.println("theta debug: pressed camera mode button down");
                }
            }

            @Override
            public void onKeyUp(int keyCode, KeyEvent event) {
                notificationLedShow(LedTarget.LED6);
                notificationLed3Show(LedColor.YELLOW);
                //get_gps_info();

                //playSound("error.wav");
                //playSound("start_of_input.wav");

                System.out.println("theta debug: camera now in plug-in mode  :-)");
                File assistantDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), "assistant");
                System.out.println("Path: " + assistantDir);
                startSnips(assistantDir);

                //write_data();
            }

            @Override
            public void onKeyLongPress(int keyCode, KeyEvent event) {

            }
        });
    }

    private void init_text2speech() {
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                    t1.setPitch((float) (0.1 / 100.0));
                 }
            }
        });
    }

    private void text2speech(String text){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            t1.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    // ---------------------------------------------------------------------------------------------
    // GPS Info
    // https://javadoc.io/doc/org.theta4j/theta-web-api/1.3.0
    // ---------------------------------------------------------------------------------------------

    private void get_gps_info() {
        BigDecimal latitude = gps_info.getLatitude();
        BigDecimal longitude = gps_info.getLongitude();
        BigDecimal altitude = gps_info.getAltitude();
        System.out.println("=====================================================================");
        System.out.println("Latitude: " + latitude);
        System.out.println("Longitude: " + longitude);
        System.out.println("Altitude: " + altitude);
        System.out.println("=====================================================================");
    }

    // ---------------------------------------------------------------------------------------------
    // Check the Permissions associated to the APP
    // ---------------------------------------------------------------------------------------------
    private boolean audio_permissions() {

        String[] permissions = {Manifest.permission.RECORD_AUDIO};

        int status = ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO);

        System.out.println("RECORD AUDIO: " + status);

        if (status != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, AUDIO_ECHO_REQUEST);
            return false;
        }

        return true;
    }

    //Manifest.permission.INTERNET,

    private boolean write_stored_permissions() {

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        int status = ActivityCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE);

        System.out.println("WRITE EXTERNAL STORAGE: " + status);

        if (status != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    private boolean read_stored_permissions() {

        String[] permissions = {READ_EXTERNAL_STORAGE};

        int status = ActivityCompat.checkSelfPermission(MainActivity.this, READ_EXTERNAL_STORAGE);

        System.out.println("READ EXTERNAL STORAGE: " + status);

        if (status != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    //----------------------------------------------------------------------------------------------
    // Star Snip Assistant
    // Ref: https://stackoverflow.com/questions/7908193/how-to-access-downloads-folder-in-android
    //----------------------------------------------------------------------------------------------
    private void startSnips(File snipsDir) {
        SnipsPlatformClient client = createClient(snipsDir);
        client.connect(this.getApplicationContext());
    }

    private SnipsPlatformClient createClient(File assistantLocation) {


        final SnipsPlatformClient client =
                new SnipsPlatformClient.Builder(assistantLocation)
                        .enableDialogue(true)
                        .enableHotword(true)
                        .enableSnipsWatchHtml(false)
                        .enableLogs(true)
                        .withHotwordSensitivity(0.9f)
                        .enableStreaming(false)
                        .enableInjection(false)
                        .build();

        client.setOnPlatformReady(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                Log.d(TAG, "Snips is ready. Say the wake word!");
                return null;
            }
        });

        client.setOnPlatformError(
                new Function1<SnipsPlatformClient.SnipsPlatformError, Unit>() {
                    @Override
                    public Unit invoke(final SnipsPlatformClient.SnipsPlatformError
                                               snipsPlatformError) {

                        playSound("error.wav");
                        // Handle error
                        Log.d(TAG, "Error: " + snipsPlatformError.getMessage());
                        return null;
                    }
                });

        client.setOnHotwordDetectedListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {

                playSound("start_of_input.wav");

                // Wake word detected, start a dialog session
                Log.d(TAG, "Wake word detected!");

                notificationLedHide(LedTarget.LED3);
                notificationLed3Show(LedColor.GREEN);

                try {
                    serialDevice.write("Wake word".getBytes());
                } catch (Exception e) {
                    Log.d(TAG, "No Robot Connected...");
                }
                ;


                client.startSession(null, new ArrayList<String>(),
                        false, null);
                return null;
            }
        });

        client.setOnIntentDetectedListener(new Function1<IntentMessage, Unit>() {
            @Override
            public Unit invoke(final IntentMessage intentMessage) {
                // Intent detected, so the dialog session ends here
                client.endSession(intentMessage.getSessionId(), null);
                Log.d(TAG, "Intent detected: " +
                        intentMessage.getIntent().getIntentName());


                List<Slot> slot_value = intentMessage.getSlots();

                System.out.println(":------------------------------------------------------------:");
                System.out.println(intentMessage.getIntent().getIntentName());
                //System.out.println(intentMessage.getSlots());
                System.out.println(slot_value.size());
                System.out.println(":------------------------------------------------------------:");

                int list_size = slot_value.size();

                if (list_size == 1) {
                    String action = slot_value.get(0).getRawValue();
                    System.out.println(action);

                    serialDevice.write(action.getBytes());

                } else if (list_size == 2) {

                    String action = slot_value.get(0).getRawValue();
                    String intent = slot_value.get(1).getRawValue();

                    if (action.equals("tomar") && intent.equals("foto")) {
                        System.out.println("Taking Picture...");
                        take_picture();

                        text2speech("Hi, I am Jaime");

                        //get_live_preview();
                        image_processing();
                        notificationLedHide(LedTarget.LED3);
                        notificationLed3Show(LedColor.YELLOW);

                    }
                }


                return null;
            }
        });

        client.setOnSnipsWatchListener(new Function1<String, Unit>() {
            public Unit invoke(final String s) {
                Log.d(TAG, "Log: " + s);
                return null;
            }
        });

        return client;
    }

    //----------------------------------------------------------------------------------------------
    // Download Assistant From Url
    //----------------------------------------------------------------------------------------------

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.DIRECTORY_DOWNLOADS.equals(state)) {
            return true;
        }
        return false;
    }


    class download_file_from_web extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                String path = Environment.getExternalStorageDirectory().toString() + File.separator +
                        Environment.DIRECTORY_DOWNLOADS + File.separator; // + "assistant.zip";

                System.out.println("--------------------------> " + path);
                File my_file = new File(path, "assistant.zip");

                OutputStream output = new FileOutputStream(my_file);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

                unzip_assistant(path, "assistant.zip");

                System.out.println("File unziped...");

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

    }

    private boolean unzip_assistant(String path, String zipname) {
        InputStream is;
        ZipInputStream zis;
        try {
            String filename;

            //File directory = new File(path + zipname);
            //directory.mkdirs();

            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path + filename);

                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //----------------------------------------------------------------------------------------------
    // USB
    // https://community.theta360.guide/t/m5stack-esp32-arduino-and-ricoh-theta/4102
    // https://github.com/theta360developers/theta-plugin-m5-serial-remote-sample
    // https://github.com/felHR85/UsbSerial
    // https://developer.android.com/studio/write/java8-support.html
    // Ref: https://www.programcreek.com/java-api-examples/?code=dh-28/ModbusRtuConnect/ModbusRtuConnect-master/app/src/main/java/com/modbusconnect/rtuwrapper/connection/ModbusRtuConnection.java#
    // https://developer.android.com/guide/topics/connectivity/usb/host?fbclid=IwAR1ETS2BlhAyEtUqSE4sxP-HPpdVHVmHmXGCI9ut7teWwyYpQoERfKZJ9Rs#java
    //----------------------------------------------------------------------------------------------

    private void findDevice() {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        System.out.println("---------------------------------------: " + usbDevices);
        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                if (entry.getValue().getVendorId() == vendorId) { //vendor ID check
                    if (App.IS_DEBUG) System.out.println("USB - VENDOR ID MATCH");
                    usbDevice = entry.getValue();
                    startSerialConnection(usbDevice);
                    break;
                }
            }
        } else if (App.IS_DEBUG) {
            System.out.println("USB DEVICES LIST IS EMPTY");
        }
    }


    private void startSerialConnection(UsbDevice device) {
        Log.i(TAG, "Ready to open USB device connection");
        connection = usbManager.openDevice(device);
        serialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection);
        if (serialDevice != null) {
            if (serialDevice.open()) {
                serialDevice.setBaudRate(115200);
                serialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
                serialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);
                serialDevice.setParity(UsbSerialInterface.PARITY_NONE);
                serialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                serialDevice.write("Hi Camera".getBytes());
                uart_led_notification();
                //serialDevice.read(mCallback);
                Log.i(TAG, "Serial connection opened");
            } else {
                Log.w(TAG, "Cannot open serial connection");
            }
        } else {
            Log.w(TAG, "Could not create Usb Serial Device");
        }
    }

    UsbSerialInterface.UsbReadCallback mCallback = (data) -> {
        String dataStr = null;
        //try {
        dataStr = new String(data); //, "UTF-8");
        Log.i(TAG, "Data received: " + dataStr);
        //} catch (UnsupportedEncodingException e) {
        //    e.printStackTrace();
        //}
    };

    void uart_led_notification() {
        notificationLedShow(LedTarget.LED5);
        notificationLed3Show(LedColor.YELLOW);
        System.out.println("Led OK");
    }
    //----------------------------------------------------------------------------------------------
    // Take Picture
    // https://community.theta360.guide/t/build-a-ricoh-theta-plug-in-from-scratch-in-80-minutes/4216/2
    //----------------------------------------------------------------------------------------------

    private void take_picture() {

        executor.submit(() -> {
            try {

                theta.takePicture();

                Thread.sleep(4000);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void get_live_preview() {

        executor.submit(() -> {
            try {
                MJpegInputStream camera_preview = theta.getLivePreview();
                InputStream image = camera_preview.nextFrame();
                Bitmap bitmap = BitmapFactory.decodeStream(image);
                thetaImageView.setImageBitmap(bitmap);

            } catch (IOException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
    //----------------------------------------------------------------------------------------------
    // Audio
    //----------------------------------------------------------------------------------------------


    private boolean prepareMediaRecorder() {

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setParameters("RicUseBFormat=false");

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        //mediaRecorder.setOutputFile(soundFilePath);

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            Log.e(TAG, "Exception preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }


    private void playSound(String sound) {
        String soundFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/assistant/custom_dialogue/sound/" + sound;

        System.out.println("Sound Path: " + soundFilePath);

        File file = new File(soundFilePath);
        if (!file.exists()) {
            return;
        }
        file = null;

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING); // 2019/1/21追記
        audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVol, 0); // 2019/1/21追記

        mediaPlayer = new MediaPlayer();
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setLegacyStreamType(AudioManager.STREAM_RING) // 2019/1/21追記
                .build();
        try {
            mediaPlayer.setAudioAttributes(attributes);
            mediaPlayer.setDataSource(soundFilePath);
            mediaPlayer.setVolume(100.0f, 100.0f);

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });

            mediaPlayer.prepare();
            Log.d(TAG, "Start");
        } catch (Exception e) {
            Log.e(TAG, "Exception starting MediaPlayer: " + e.getMessage());
            mediaPlayer.release();
            notificationError("");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }


    /*private void playSound(String sound) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/assistant/custom_dialogue/sound/" + sound;
        System.out.println(root);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        final MediaPlayer mediaPlayer =
                MediaPlayer.create(
                        getApplicationContext(),
                        Uri.fromFile(
                                new File(root)));

        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
        mediaPlayer.setVolume(1.0f, 1.0f);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.start();
    }*/


    //----------------------------------------------------------------------------------------------
    // Image Processing
    //----------------------------------------------------------------------------------------------
    // native functions
    public native String version();

    public native byte[] rgba2bgra(int width, int height, byte[] src);

    // Jarain78
    public native byte[] processing(int width, int height, byte[] src);

    // Jarain78
    public native byte[] flipimage(int width, int height, byte[] src);

    private void image_processing() {

        String path = Environment.getExternalStorageDirectory().toString() + File.separator +
                Environment.DIRECTORY_DCIM + "/100RICOH/";

        //delet_image(path);

        String[] thetaImageFiles = get_image_list(path);
        System.out.println(thetaImageFiles.length);


        if (thetaImageFiles.length > 0) {
            Bitmap image_loaded = read_image(path, thetaImageFiles);

            //thetaImageView.setImageBitmap(image_loaded);
            //Bitmap new_image = resize_image(image_loaded, 800, 400);
            //change_compress_format(new_image, path);
            face_detection(image_loaded);
        }
    }

    // change the compress format
    private void change_compress_format(Bitmap bitmap, String basepath) {
        File myExternalFile = new File(basepath + "new_image.png");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // bitmap.compress should be put on different thread
        imageExecutor.submit(() -> {
            // you can change the compress format to WEBP in the line below
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);

            try {
                Log.d(TAG, "New File Url: " + myExternalFile);
                FileOutputStream fos = new FileOutputStream(myExternalFile);
                fos.write(byteArrayOutputStream.toByteArray());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Bitmap resize_image(Bitmap imgTheta, int width, int heigh) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        ByteBuffer byteBufferTheta = ByteBuffer.allocate(imgTheta.getByteCount());
        imgTheta.copyPixelsToBuffer(byteBufferTheta);
        Bitmap bmpTheta = Bitmap.createScaledBitmap(imgTheta, width, heigh, true);
        return bmpTheta;
    }

    private Bitmap read_image(String path, String thetaImageFiles[]) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        String image_file = get_last_image(path);

        Log.d(TAG, path + image_file);
        Bitmap img = BitmapFactory.decodeFile(path + image_file, options);
        return img;

    }

    private void delet_image(String path) {
        String[] thetaImageFiles = new String[100];
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);
        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i].delete());
            thetaImageFiles[i] = files[i].getName();
        }
    }

    private String[] get_image_list(String path) {
        String[] thetaImageFiles = new String[100];
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);

        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i].getName());
            thetaImageFiles[i] = files[i].getName();
        }

        return thetaImageFiles;
    }

    private String get_last_image(String path) {
        String[] thetaImageFiles = new String[100];
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);
        int i = files.length - 1;
        return files[i].getName();
    }


    private void face_detection(Bitmap my_bitmap) {

        //faces = new LinkedList<Rect>();
        int height = my_bitmap.getHeight();
        int width = my_bitmap.getWidth();

        //my_face = new FaceDetector.Face[NUMBER_OF_FACES];//acha ateh 4 faces numa imagem
        //my_face_detect = new FaceDetector(width, height, NUMBER_OF_FACES);
        //int number_of_face_detected = my_face.length;

        Bitmap mutableBitmap = my_bitmap.copy(Bitmap.Config.RGB_565, true);
        FaceDetector.Face[] faces = new FaceDetector.Face[NUMBER_OF_FACES];
        // initialize the face detector, and look for only one face...
        FaceDetector fd;

        Canvas canvas = new Canvas(mutableBitmap);
        Paint myPaint = new Paint();
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(3);

        try {
            fd = new FaceDetector(mutableBitmap.getWidth(), mutableBitmap.getHeight(), NUMBER_OF_FACES);
            //int count = fd.findFaces(mutableBitmap, faces);

            for (int i_face = 0; i_face < faces.length; i_face++) {
                int num_found = fd.findFaces(mutableBitmap, faces);
                if (num_found > 0) {
                    Face face = faces[i_face];

                    if (face != null) {
                        double my_eyes_distance = face.eyesDistance();
                        System.out.println("Num Faces Detected: " + num_found + " " + " Eye Distance: " + my_eyes_distance);
                        System.out.println("=====================================================================");

                        PointF my_mid_point = new PointF();
                        face.getMidPoint(my_mid_point);
                        my_eyes_distance = face.eyesDistance();

                        System.out.println("Euler_X: " + face.pose(FaceDetector.Face.EULER_X) +
                                " Euler_Y: " + face.pose(FaceDetector.Face.EULER_Y) +
                                " Euler_Z: " + face.pose(FaceDetector.Face.EULER_Z));

                        canvas.drawRect((int) (my_mid_point.x - my_eyes_distance * 2),
                                (int) (my_mid_point.y - my_eyes_distance * 2),
                                (int) (my_mid_point.x + my_eyes_distance * 2),
                                (int) (my_mid_point.y + my_eyes_distance * 2), myPaint);

                        int x = (int) (my_mid_point.x - my_eyes_distance);
                        int y = (int) (my_mid_point.y - my_eyes_distance);

                        int w = (int) (my_mid_point.y + my_eyes_distance) + x;
                        int h = (int) (my_mid_point.x + my_eyes_distance) + y;


                        System.out.println(x + " " + y + " " + w + " " + h);
                        System.out.println("=====================================================================");
                        Bitmap crop_image = Bitmap.createBitmap(mutableBitmap, x, y, w, h);


                        thetaImageView.setImageBitmap(crop_image);
                    }
                }
            }
        } catch (
                Exception e) {
            Log.e(TAG, "setFace(): " + e.toString());
            return;
        }
    }

    private Bitmap crop_image(Bitmap image, PointF my_mid_point, double my_eyes_distance) {

        Bitmap crop_image = Bitmap.createBitmap(image, (int) (my_mid_point.x - my_eyes_distance * 2),
                (int) (my_mid_point.y - my_eyes_distance * 2),
                (int) (my_mid_point.x + my_eyes_distance * 2),
                (int) (my_mid_point.y + my_eyes_distance * 2));

        return crop_image;
    }
}
