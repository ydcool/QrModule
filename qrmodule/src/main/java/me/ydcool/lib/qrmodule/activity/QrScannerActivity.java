package me.ydcool.lib.qrmodule.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.Vector;

import me.ydcool.lib.qrmodule.R;
import me.ydcool.lib.qrmodule.camera.CameraManager;
import me.ydcool.lib.qrmodule.decoding.CaptureActivityHandler;
import me.ydcool.lib.qrmodule.decoding.InactivityTimer;
import me.ydcool.lib.qrmodule.view.ViewfinderView;

/**
 * Initial the camera
 * override {@link #onQrResult(String)} to handle qr result if you want,
 * or we will default do {@link Activity#setResult(int, Intent)}
 * with string extras "result" then call{@link Activity#finish()}.
 *
 * @author Ryan.Tang
 *         modify: ydcool 2015-11-18 09:48:06 add method {@link #onQrResult(String)}
 */
public class QrScannerActivity extends Activity implements Callback {

    private static final float BEEP_VOLUME = 0.10f;
    private static final long VIBRATE_DURATION = 200L;
    public static final String QR_RESULT_STR = "result";
    public static final int QR_REQUEST_CODE = 200;
    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private boolean vibrate;

    private boolean isFlashLightOn;
    private ImageView mFlashBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {

//        requestWindowFeature(Window.FEATURE_NO_TITLE);

//        getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        if (getActionBar() != null)
            getActionBar().hide();

        setUpButtons();
        setUpCamera();
    }

    private void setUpButtons() {
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QrScannerActivity.this.finish();
            }
        });

        mFlashBtn = (ImageView) findViewById(R.id.flash_btn);
        mFlashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFlashLightOn)
                    CameraManager.get().offLight();
                else
                    CameraManager.get().openLight();

                mFlashBtn.setImageResource(isFlashLightOn
                        ? R.drawable.ic_flash_off_white_36dp
                        : R.drawable.ic_flash_on_white_36dp);

                isFlashLightOn = !isFlashLightOn;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpSurfaceAndSound();
    }

    protected void setUpCamera() {
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setVisibility(View.VISIBLE);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(QrScannerActivity.this);
    }

    protected void setUpSurfaceAndSound() {
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        surfaceView.setVisibility(View.VISIBLE);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(QrScannerActivity.this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }

        if (isFlashLightOn) {
            isFlashLightOn = false;
            CameraManager.get().offLight();
        }

        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * Handler scan result
     *
     * @param result  qr scan result string.
     * @param barcode original qr code
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();

        onQrResult(result.getText());
    }

    public void resetScanner() {
        if (handler != null)//containue scan
            handler.restartPreviewAndDecode();
    }

    public void resetScannerDelayed(long millionSeconds) {
        mFlashBtn.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetScanner();
            }
        }, millionSeconds);
    }

    /**
     * override this to handle qr result if you want,
     * or we will default do {@link Activity#setResult(int, Intent)}
     * with string extras "result" then call{@link Activity#finish()}.
     *
     * @param resultString qr scan result string.
     */
    public void onQrResult(String resultString) {
        if (resultString.equals("")) {
            Toast.makeText(QrScannerActivity.this, "Scan Result Empty!", Toast.LENGTH_SHORT).show();
            this.setResult(RESULT_CANCELED);
        } else {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString(QR_RESULT_STR, resultString);
            resultIntent.putExtras(bundle);
            this.setResult(RESULT_OK, resultIntent);
        }
        QrScannerActivity.this.finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(QrScannerActivity.this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

}