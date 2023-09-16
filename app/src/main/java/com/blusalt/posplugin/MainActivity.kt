package com.blusalt.posplugin

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.blusalt.posplugin.util.PrefConstant.DEFAULT_BLUETOOTH_DEVICE
import com.blusalt.posplugin.model.BtDevice
import com.blusalt.posplugin.util.AppPreferenceHelper
import com.blusalt.posplugin.util.PrefConstant
import com.google.gson.Gson
import timber.log.Timber
import java.util.*


class MainActivity : AppCompatActivity(){

    var navController: NavController? = null

    val TAG: String = MainActivity::class.java.getSimpleName()
    val INTENT_EXTRA_REQUEST = "transactionRequest"
    val INTENT_EXTRA_RESPONSE = "transactionResult"
    var mBluetoothAdapter: BluetoothAdapter? = null
    var appPreferenceHelper: AppPreferenceHelper? = null
    val REQUEST_ENABLE_BLUETOOTH = 1

    var mHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = Navigation.findNavController(this, R.id.appNavHostFragment)

    }

//    fun navigateToFragment() {
//        Log.e(TAG, "Connected")
//        val action =
//            BluetoothFragmentDirections.actionBluetoothFragmentToConnectedFragment() as NavDirections
//        Navigation.findNavController(this@MainActivity, R.id.appNavHostFragment).navigate(action)
//    }

    private fun requestBlePermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val ANDROID_12_BLE_PERMISSIONS = arrayOf<String>(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            ActivityCompat.requestPermissions(
                activity,
                ANDROID_12_BLE_PERMISSIONS,
                REQUEST_ENABLE_BLUETOOTH
            )
        }
    }

    private fun navigateToBluetooth() {
        setContentView(R.layout.activity_main)
        if (appPreferenceHelper!!.getSharedPreferenceBoolean(PrefConstant.IS_DEFAULT_DEVICE_SELECTED)) {
            Timber.tag(TAG).d("Navigate to Bluetooth Result")
            val btDevice: BtDevice = Gson().fromJson(
                appPreferenceHelper!!.getSharedPreferenceString(DEFAULT_BLUETOOTH_DEVICE),
                BtDevice::class.java
            )
//            bluetoothResult(btDevice, )
        } else {
            Timber.tag(TAG).d("Navigate to Bluetooth Fragment")
            navController = Navigation.findNavController(this, R.id.appNavHostFragment)
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                navigateToBluetooth()
            } else {
                Log.e("TAG", "Bluetooth not in device")
            }
        }
    }

    companion object {
        var isSending = false
        const val STATE_LISTENING = 1
        const val STATE_CONNECTING = 2
        const val STATE_CONNECTED = 3
        const val STATE_CONNECTION_FAILED = 4
        const val STATE_MESSAGE_RECEIVED = 5
        private const val APP_NAME = "MPOSPLUGIN"
        private val MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66")
    }
}