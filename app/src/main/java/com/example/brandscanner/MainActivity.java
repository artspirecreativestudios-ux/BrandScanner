package com.example.brandscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private TextRecognizer recognizer;
    private TextView brandsResult;
    private TextView fullTextResult;
    private Button captureButton;
    private Button galleryButton;

    private static final Set<String> COMMON_WORDS = new HashSet<>();

    static {
        // Common words to filter out from brands
        COMMON_WORDS.add("THE");
        COMMON_WORDS.add("AND");
        COMMON_WORDS.add("WITH");
        COMMON_WORDS.add("FOR");
        COMMON_WORDS.add("MADE");
        COMMON_WORDS.add("FROM");
        COMMON_WORDS.add("BEST");
        COMMON_WORDS.add("QUALITY");
        COMMON_WORDS.add("PURE");
        COMMON_WORDS.add("NET");
        COMMON_WORDS.add("WEIGHT");
        COMMON_WORDS.add("CONTENTS");
        COMMON_WORDS.add("NUTRITION");
        COMMON_WORDS.add("INGREDIENTS");
        COMMON_WORDS.add("MFG");
        COMMON_WORDS.add("EXP");
        COMMON_WORDS.add("USE");
        COMMON_WORDS.add("BEFORE");
        COMMON_WORDS.add("KEEP");
        COMMON_WORDS.add("STORE");
        COMMON_WORDS.add("AVOID");
        COMMON_WORDS.add("DO");
        COMMON_WORDS.add("NOT");
        COMMON_WORDS.add("IN");
        COMMON_WORDS.add("ON");
        COMMON_WORDS.add("OF");
        COMMON_WORDS.add("BY");
        COMMON_WORDS.add("AT");
        COMMON_WORDS.add("CONTACT");
        COMMON_WORDS.add("US");
        COMMON_WORDS.add("OR");
        COMMON_WORDS.add("CALL");
        COMMON_WORDS.add("VISIT");
        COMMON_WORDS.add("WWW");
        COMMON_WORDS.add("COM");
        COMMON_WORDS.add("ORG");
        COMMON_WORDS.add("NET");
        COMMON_WORDS.add("INC");
        COMMON_WORDS.add("LLC");
        COMMON_WORDS.add("LTD");
        COMMON_WORDS.add("CO");
        COMMON_WORDS.add("INDIA");
        COMMON_WORDS.add("MADE");
        COMMON_WORDS.add("IN");
        COMMON_WORDS.add("PRODUCT");
        COMMON_WORDS.add("SIZE");
        COMMON_WORDS.add("QTY");
        COMMON_WORDS.add("PCS");
        COMMON_WORDS.add("PKT");
        COMMON_WORDS.add("BOX");
        COMMON_WORDS.add("PACK");
        COMMON_WORDS.add("MANUFACTURED");
        COMMON_WORDS.add("DISTRIBUTED");
        COMMON_WORDS.add("BY");
    }

    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        previewView = findViewById(R.id.previewView);
        captureButton = findViewById(R.id.captureButton);
        galleryButton = findViewById(R.id.galleryButton);
        brandsResult = findViewById(R.id.brandsResult
