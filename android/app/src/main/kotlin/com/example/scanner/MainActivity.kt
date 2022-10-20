package com.example.scanner

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import com.example.scanner.Permissions


class MainActivity: FlutterFragmentActivity() {
    private val CHANNEL = "example.flutter.dev/scanner"
    //private val permissions = Permissions()

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);
        
        // 1. Establezca un teléfono fijo para prepararse para la comunicación, el nombre de CHANNEL es exmaple.flutter.dev/scanner, y tenga en cuenta que debe ser el mismo que el final de flutter
         val methodChannel = MethodChannel(flutterEngine.dartExecutor, CHANNEL)
        
        // 2. Recibe el mensaje y reacciona de acuerdo con el mensaje recibido 
        methodChannel.setMethodCallHandler{
            call, result ->
            if(call.method == "getCode") {
                val intent = Intent(this, Scanner::class.java)
                startActivity(intent)

                // val batteryLevel1 = getCode()
            }else{
                result.notImplemented()

            }
        }
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

    

}
