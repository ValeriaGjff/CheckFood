package com.example.checkfood;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;

    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;

    private boolean scannedOnce = false;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::handlePickedImage);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        ImageButton btnHistory = findViewById(R.id.btnHistory);
        ImageButton btnCapture = findViewById(R.id.btnCapture);
        ImageButton btnGallery = findViewById(R.id.btnGallery);

        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        btnCapture.setOnClickListener(v -> captureAndScan());

        btnGallery.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 33) {
                pickImage.launch("image/*");
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    pickImage.launch("image/*");
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 200);
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Разрешение на камеру отклонено", Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage.launch("image/*");
            } else {
                Toast.makeText(this, "Нет доступа к галерее", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
                    @Override
                    @ExperimentalGetImage
                    public void analyze(@NonNull ImageProxy imageProxy) {
                        if (scannedOnce) { imageProxy.close(); return; }
                        scanImageProxy(imageProxy);
                    }
                });

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis,
                        imageCapture
                );

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка запуска камеры", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureAndScan() {
        if (imageCapture == null) return;

        imageCapture.takePicture(cameraExecutor, new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                scanImageProxy(image);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(ScanActivity.this, "Не удалось сделать снимок", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @ExperimentalGetImage
    private void scanImageProxy(@NonNull ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) { imageProxy.close(); return; }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        BarcodeScanner scanner = BarcodeScanning.getClient(options);

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty() && !scannedOnce) {
                        scannedOnce = true;
                        String raw = barcodes.get(0).getRawValue();
                        goToResult(raw);
                    }
                    imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    Log.e("BARCODE", "Ошибка сканирования", e);
                    imageProxy.close();
                });
    }


    private void handlePickedImage(Uri uri) {
        if (uri == null) return;
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            Bitmap bmp = BitmapFactory.decodeStream(is);
            if (bmp == null) {
                Toast.makeText(this, "Не удалось открыть изображение", Toast.LENGTH_SHORT).show();
                return;
            }
            InputImage img = InputImage.fromBitmap(bmp, 0);
            BarcodeScanner scanner = BarcodeScanning.getClient(
                    new BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                            .build());

            scanner.process(img)
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty()) {
                            String raw = barcodes.get(0).getRawValue();
                            goToResult(raw);
                        } else {
                            Toast.makeText(this, "Штрих-код не найден на фото", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Ошибка распознавания", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка чтения изображения", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToResult(String raw) {
        if (raw == null || raw.isEmpty()) return;
        Intent i = new Intent(this, ProductInfoActivity.class);
        i.putExtra("barcode", raw);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}