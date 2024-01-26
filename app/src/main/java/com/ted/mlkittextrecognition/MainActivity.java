package com.ted.mlkittextrecognition;

import static androidx.camera.core.impl.utils.TransformUtils.getExifTransform;

import static java.lang.Math.abs;
import static java.lang.Math.round;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraProvider;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.video.OutputFileOptions;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.ted.mlkittextrecognition.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;
    boolean isPermissionGranted = true;
    String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };
    ExecutorService cameraExecutor;
    ImageCapture imageCapture;

    String TAG = "CameraXApp";
    String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private final ActivityResultLauncher<String[]> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
        permissions.forEach((key, value) -> {
            for (String permission : PERMISSIONS) {
                isPermissionGranted = !key.equalsIgnoreCase(permission) || value;
            }
            if (isPermissionGranted) {
                startCamera();
            } else {
                showToast("All permissions not granted");
            }
        });
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            PERMISSIONS[2] = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        }

        imageCapture = new ImageCapture.Builder().build();

        if (checkCameraPermissions(this, PERMISSIONS)) {
            startCamera();
        } else {
            requestPermission();
        }

        setListeners();

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void setListeners() {
        activityMainBinding.btnTakePhoto.setOnClickListener(view -> {
            activityMainBinding.btnTakePhoto.setEnabled(false);
            takePhoto();
        });
    }

    private void takePhoto() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FILENAME_FORMAT, Locale.US);
        String date = simpleDateFormat.format(System.currentTimeMillis());

        ImageCapture.OutputFileOptions outputFileOptions = getOutputFileOptions(date);
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                activityMainBinding.btnTakePhoto.setEnabled(true);
                Bitmap bitmap = null;
                try {
                    bitmap = loadBitmapWithExifApplied(outputFileResults.getSavedUri());
                    showLog(bitmap.toString());
                    showToast("Image successfully saved");
                    switchToTextProcessingScreen(bitmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                activityMainBinding.btnTakePhoto.setEnabled(true);
                showToast(exception.getMessage());
            }
        });
    }

    private void switchToTextProcessingScreen(Bitmap bitmap) {
        Intent intent = new Intent(this, TextProcessingActivity.class);
        BitmapTransfer.bitmap = bitmap;
        startActivity(intent);
        finish();
    }

    @NonNull
    private ImageCapture.OutputFileOptions getOutputFileOptions(String date) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, date);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
        }

        return new ImageCapture.OutputFileOptions.Builder(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build();
    }

    private void startCamera() {
        ListenableFuture cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(activityMainBinding.viewFinder.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                showToast(e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void requestPermission() {
        activityResultLauncher.launch(PERMISSIONS);
    }

    private boolean checkCameraPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }

    Bitmap loadBitmapWithExifApplied(Uri uri) throws IOException {
        // Loads bitmap.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap original;
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            original = BitmapFactory.decodeStream(inputStream, /*outPadding*/ null, options);
        }

        // Reads exif orientation.
        int exifOrientation;
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            ExifInterface exifInterface = new ExifInterface(inputStream);
            exifOrientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        }
        Matrix matrix = getExifTransform(exifOrientation, original.getWidth(),
                original.getHeight());

        // Calculate the bitmap size with exif applied.
        float[] sizeVector = new float[]{original.getWidth(), original.getHeight()};
        matrix.mapVectors(sizeVector);

        // Create a new bitmap with exif applied.
        Bitmap bitmapWithExif = Bitmap.createBitmap(
                round(abs(sizeVector[0])),
                round(abs(sizeVector[1])),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapWithExif);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawBitmap(original, matrix, paint);
        return bitmapWithExif;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLog(String message) {
        Log.e(TAG, "showLog: " + message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}