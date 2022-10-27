package com.example.scanner;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import io.flutter.embedding.android.FlutterActivity;

public class MainActivity extends FlutterActivity {
  private static final String CHANNEL = "example.flutter.dev/scannerJava";

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
  super.configureFlutterEngine(flutterEngine);
    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
        .setMethodCallHandler(
          (call, result) -> {
            if (call.method.equals("scannerJava")) {
                String formElement = "fillEnviarFromQr";
                String operator = "CFE";

                Scanner scanner = new Scanner();
                ImageAnalysis imageAnalysis = scanner.openBarcode(formElement);
                ArrayList qrRes = scanner.getImageAnalysisObj(scanner.getBarcodeScannerObj(formElement));


              if (qrRes.get(0) != "" || qrRes.get(0) != null) {
                result.success(qrRes.get(0));
              } else {
                result.error("UNAVAILABLE", "Battery level not available.", null);
              }
            } else {
              result.notImplemented();
            }
          }
        );
  }



  public class Scanner extends AppCompatActivity implements View.OnClickListener {

    private ImageView line, center;
    //private SurfaceView cameraView;
    private String codebarType;
    private BarcodeDetector barcodeDetector;
    private TextView scannerText;
    float unit, x, y1;
    private static final int PERMISSIONS_REQUEST_CAPTURE_IMAGE = 1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Log.d("Scanner", "Entered function onCreate");
            setContentView(R.layout.activity_scanner);
            Toolbar mToolbar = findViewById(R.id.toolbar);
            scannerText = findViewById(R.id.scannerText);
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
            // color consistent with back button on payment page
            mToolbar.getNavigationIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Scanner.this, MainActivity.class);
                    startActivityGracefully(intent);
                    // stop returning and rebounding between activities
                    finish();
                }
            });
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            Context context = getApplicationContext();
            Bundle extra_info;
            extra_info = this.getIntent().getExtras();
            codebarType = extra_info.getString("inputElement");
            if(codebarType.equals("fillEnviarFromQr")){
                scannerText.setText(R.string.QR_scanner_display);
            }else{
                scannerText.setText(R.string.barcode_scanner_display);
            }
            final WindowManager w = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            final Display d = w.getDefaultDisplay();
            final DisplayMetrics m = new DisplayMetrics();
            d.getMetrics(m);
            int wp = m.widthPixels;
            int hp = m.heightPixels;
            int widthInDP = Math.round(m.widthPixels / m.density);
//        unit = m.heightPixels / heightInDP;
            x = m.widthPixels / 2;
            y1 = m.heightPixels / 2;
            WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(m);
            center = findViewById(R.id.centerImage);
            line = findViewById(R.id.line);
            Log.d("tamanio", "height" + m.heightPixels + " weigth " + m.widthPixels);
            Log.d("tamanio", " units  " + unit + "----");
            checkPermissionsAndScanIfGranted();
        }catch(Exception e){
            logExceptionAndReturnToMainActivity(e);
        }
    }

    private void logExceptionAndReturnToMainActivity(Exception e) {
        Log.d("fail Scanner", " error:  " + e.getMessage() + "----");
        Intent MainIntent = new Intent(Scanner.this, MainActivity.class);
        this.startActivityGracefully(MainIntent);
        // stop returning and rebounding between activities
        finish();
    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus) {
        final WindowManager w = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        final Display d = w.getDefaultDisplay();
        final DisplayMetrics m = new DisplayMetrics();
        d.getMetrics(m);
        int heightInDP = Math.round(m.heightPixels/m.density);
        unit = m.heightPixels / heightInDP;

        x = m.widthPixels/2;
        int yi = center.getTop();
        int yf = center.getBottom();
        TranslateAnimation animation = new TranslateAnimation(x-(line.getWidth()/2), x-(line.getWidth()/2), yi, yf);
        animation.setDuration(800);
        animation.setRepeatCount(50000);
        animation.setRepeatMode(2);
        line.startAnimation(animation);
    }

    /*
     * The object returned is what shows the user what the camera is seeing
    */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Preview getPreviewObj() {
        PreviewView cameraView = findViewById(R.id.scanner_preview_view);
        cameraView.setImplementationMode(
                PreviewView.ImplementationMode.PERFORMANCE); // uses SurfaceView under the hood
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraView.getSurfaceProvider());

        return preview;
    }

    public BarcodeScanner getBarcodeScannerObj(String codebarType) {
        BarcodeScannerOptions scannerOptions;
        if(codebarType.equals("fillEnviarFromQr")) {
            scannerOptions = new BarcodeScannerOptions
                    .Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build();
        }else{
            scannerOptions = new BarcodeScannerOptions
                    .Builder()
                    .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E, Barcode.FORMAT_CODE_39, Barcode.FORMAT_CODE_93, Barcode.FORMAT_CODE_93, Barcode.FORMAT_CODE_128, Barcode.FORMAT_ITF, Barcode.FORMAT_CODABAR)
                    .build();
        }

        return BarcodeScanning.getClient(scannerOptions);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ArrayList getImageAnalysisObj(BarcodeScanner barcodeScanner) {
        ArrayList arr = new ArrayList();
        String[] qrRes = new String[1];
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 1024))
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build();
        imageAnalysis.setAnalyzer(
            newSingleThreadExecutor(),
            new ImageAnalysis.Analyzer() {
                @Override
                public void analyze(ImageProxy imageProxy) {
                    @SuppressLint("UnsafeOptInUsageError") Image mediaImage = imageProxy.getImage();
                    if (mediaImage != null) {
                        InputImage image =
                        InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                        barcodeScanner.process(image).addOnSuccessListener( barcodes-> {
                            if (barcodes != null && barcodes.size() > 0) {
                                Barcode barcode = barcodes.get(0);
                                 qrRes[0] = afterReadingQR(barcode.getRawValue());
                                 arr.add(qrRes[0]);
                            }
                            imageProxy.close();
                        }).addOnFailureListener(ex -> {
                            Log.e("Scanner",
                                    "Exception occurred in setAnalyzer: " + ex.getMessage() +
                                            " Stack Trace: " + Arrays.toString(ex.getStackTrace()));
                            imageProxy.close();
                        });
                    }
                }
            }
        );
        arr.add(imageAnalysis);
        return arr;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ImageAnalysis openBarcode(String codebarType) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
        cameraProviderFuture = ProcessCameraProvider.getInstance(getApplicationContext());  // Used to bind to lifecycle
        final ImageAnalysis[] imageAnalysis = new ImageAnalysis[1];

        cameraProviderFuture.addListener(() -> {
            ProcessCameraProvider cameraProvider = null;
            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (Exception e) {
                logExceptionAndReturnToMainActivity(e);
            }
            // Show user what the camera sees
            Preview preview = getPreviewObj();
            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
            BarcodeScanner barcodeScanner = getBarcodeScannerObj(codebarType);
            // Used to pass images to the BarcodeScanner from the camera feed
            // and defines what to do with the detections
            imageAnalysis[0] = (ImageAnalysis) getImageAnalysisObj(barcodeScanner).get(1);
            try {
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis[0]);
            } catch(Exception e) {
                logExceptionAndReturnToMainActivity(e);
            }
        }, ContextCompat.getMainExecutor(getApplicationContext()));

        return imageAnalysis[0];
    }

    public String afterReadingQR(String result){
        if (result != null) {
            //we have a result
            String scanContent;
            if(!codebarType.equals("fillEnviarFromQr")){
                scanContent = result.replaceAll("[^a-zA-Z0-9]", "");
            }else{
                scanContent = result;
            }

            Log.d("Scanner", "scanContent: " + scanContent);
            Bundle bundle = getIntent().getExtras();
            String operator = bundle.getString("operator");

            if (Objects.equals(operator, "SACMEX")) {
                scanContent = processSACMEXBarcode(scanContent);
                if (scanContent.isEmpty()) return null;
                Log.d("Scanner", "scanContent after SACMEX: " + scanContent);
            }
            String formElement = bundle.getString("inputElement");
            String scanFormat = "QR_CODE";

            return scanContent;

            /*
            Intent MainIntent = new Intent(Scanner.this, MainActivity.class);
            //MainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            MainIntent.putExtra("purpose", "scan");
            MainIntent.putExtra("inputElement", formElement);
            MainIntent.putExtra("FORMAT", scanFormat);
            MainIntent.putExtra("CONTENT", scanContent);
            Log.d("SCAN_QR_11", scanContent);

            this.startActivityGracefully(MainIntent);
            // stop returning and rebounding between activities
            finish();
             */
        }else{
            Toast toast = Toast.makeText(getApplicationContext(),
            "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();

            /*
            Intent MainIntent = new Intent(Scanner.this,
                    MainActivity.class);
            MainIntent.putExtra("purpose", "scan");
            MainIntent.putExtra("FORMAT", "NA");
            MainIntent.putExtra("CONTENT", "NA");
            this.startActivityGracefully(MainIntent);
            // stop returning and rebounding between activities
            finish();
             */
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void InformForCameraAndBack(){
        if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // User may have declined earlier, ask Android if we should show him a reason

            new AlertDialog.Builder(this)
                    .setTitle("Permiso Denegado")
                    .setMessage("Negaste el permiso para abrir tu cámara, no nos será posible abrirla, Necesitamos permiso para abrir tu cámara y poder escanear tu código.")
                    .setPositiveButton("Okay, entendido", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent MainIntent = new Intent(Scanner.this,
                                    MainActivity.class);
                            startActivityGracefully(MainIntent);
                            // stop returning and rebounding between activities
                            finish();
                        }
                    }).show();
        } else {
            openBarcode(codebarType);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void checkPermissionsAndScanIfGranted(){
        if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // User may have declined earlier, ask Android if we should show him a reason

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(this)
                        .setTitle("Solicitud de permisos")
                        .setMessage("Necesitamos permiso para abrir tu cámara y poder escanear tu código.")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(Scanner.this, new String[]{android.Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAPTURE_IMAGE);
                            }
                        })
                        .setNegativeButton("No ahora", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                InformForCameraAndBack();
                            }
                        })
                        .show();


            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAPTURE_IMAGE);
            }
        } else {
            openBarcode(codebarType);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        InformForCameraAndBack();
    }

    @Override
    public void onClick(View v) {

    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Scanner.this, MainActivity.class);

        this.startActivityGracefully(intent);
        // stop returning and rebounding between activities
        finish();
    }


    /**
     * starts activity gracefully
     * @param intent intent on which app is started
     */
    private void startActivityGracefully(Intent intent) {
        // skip onCreate for already existing main activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityIfNeeded(intent, 0);
    }

    private String processSACMEXBarcode(String scanContent) {
        if (scanContent.length() < 24) {
            Log.w("Scanner", "Detection too small for SACMEX");
            return "";
        }
        String lineaDeCaptura = scanContent.substring(0,20);
        String restOfBarcode = scanContent.substring(20);
        String endOfBarcode = restOfBarcode.replaceFirst("^0+", "");
        String amount = endOfBarcode.substring(0, endOfBarcode.length() - 3);
        if (! Pattern.matches("^[1-9][0-9]*$", amount)){
            Log.w("Scanner", "Amount not found or zero");
            return "";
        }
        scanContent = lineaDeCaptura + amount;

        return scanContent;
    }
}




}
