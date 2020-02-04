package com.dynamsoft.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private Camera mCamera;
    private AutoFitTextureView mTextureView;
    //private AutoFitTextureView mTextureView2;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    TextureView.SurfaceTextureListener listener1 = new TextureView.SurfaceTextureListener() {
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(surface);
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
            }

            return true;
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    TextureView.SurfaceTextureListener listener2 = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //openCamera(surface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getSurfaceTexture());
        } else {
            mTextureView.setSurfaceTextureListener(listener1);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextureView = findViewById(R.id.surfaceView);
        //mTextureView2 = findViewById(R.id.surfaceView2);

        mTextureView.setSurfaceTextureListener(listener1);
        //mTextureView2.setSurfaceTextureListener(listener2);

    }

    //For debug purpose
    private int frameCount=0;

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            frameCount++;
            Log.d("frameCount","f:"+frameCount+" size:"+data.length+" bytes"+" random bit "+data[1]);
            int a=camera.getParameters().getPictureFormat();
            Log.d("format:",""+ a);


            //HERE WE GET FRAME DATA

            //SAMPLE PROCESS
            Camera.Parameters parameters = camera.getParameters();
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;

            YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

            byte[] bytes = out.toByteArray();
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    ((ImageView) findViewById(R.id.imageView)).setImageBitmap(bitmap);
                }
            });

        }
    };

    private void openCamera(SurfaceTexture surface) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }

        mCamera = Camera.open(0);
        try {
            mCamera.setPreviewCallback(previewCallback);

            mCamera.setPreviewTexture(surface);

            int rotation = getWindowManager().getDefaultDisplay()
                    .getRotation();
            mCamera.setDisplayOrientation(ORIENTATIONS.get(rotation));
            Camera.Parameters params =  mCamera.getParameters();

            //Set preview format. NV21 is default
            params.setPreviewFormat(ImageFormat.NV21);

            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            Camera.Size size = params.getPreviewSize();

            int orientation = getResources().getConfiguration().orientation;
            int previewWidth = size.width;
            int previewHeight = size.height;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(
                        previewWidth, previewHeight);
            } else {
                mTextureView.setAspectRatio(
                        previewHeight, previewWidth);
            }

            mCamera.setParameters(params);
            mCamera.startPreview();
        } catch (IOException ioe) {
        }
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
        }
    }
}
