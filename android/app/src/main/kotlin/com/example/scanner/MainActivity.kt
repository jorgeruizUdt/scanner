package com.example.scanner

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant


class MainActivity: FlutterActivity() {
    private val CHANNEL = "example.flutter.dev/scanner"
    public var resultScan = ""
    //private val permissions = Permissions()

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);

         val methodChannel = MethodChannel(flutterEngine.dartExecutor, CHANNEL)
        methodChannel.setMethodCallHandler{
            call, result ->
            if(call.method == "openScanner") {
                openScanner()
                result.success(resultScan)
            }else{
                result.notImplemented()
            }
        }
    }

    private fun openScanner() {
        var formElement: String = "fillEnviarFromQr"
        //var formElement: String = "BarCode"
        var operator: String = "CFE"

        val intentS = Intent(this, Scanner::class.java)
        intentS.putExtra("inputElement", formElement)
        intentS.putExtra("operator", operator)
        startActivity(intentS)
    }

    private fun getCode(): String {
        val batteryLevel: Int
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            val intent = ContextWrapper(applicationContext).registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            batteryLevel = intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        }

        return batteryLevel.toString()
    }

    override fun onResume() {
        super.onResume()
        val bundle = this.intent.extras
        val CONTENT = bundle?.getString("CONTENT")
        val FORMAT = bundle?.getString("FORMAT")

        resultScan = CONTENT.toString()
        Log.d("SCAN_QR", resultScan)
        if (resultScan != "" && resultScan != null)
            return;
    }

    

}
