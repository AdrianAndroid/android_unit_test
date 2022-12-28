package com.joyy.android_project

import android.Manifest
import android.os.Bundle
import android.provider.Settings
import android.telephony.CellLocation
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var listView = findViewById<ListView>(R.id.listView)
        var arrayOf = arrayOf(
            getCallState(),
            getCellLocation(),
            getDeviceId(),
            getDeviceSoftwareVersion(),
            getLine1Number(),
            getNeighboringCellInfo(),
            getNetworkOperator(),
            getNetworkOperatorName(),
            getNetworkType(),
            getSimCountryIso(),
            getSimSerialNumber(),
            getSubscriberId(),
            getPhoneType()
        )
        listView.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayOf)
        var permmission = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        ActivityCompat.requestPermissions(this, permmission, 1000)
    }

    fun getSubscriberId(): String {
        return "IMSI(国际移动用户识别码):"// + getTelephonyManager().subscriberId
    }

    fun getSimSerialNumber(): String {
        return "SIM卡的序列号"// + getTelephonyManager().simSerialNumber
    }

    fun getSimCountryIso(): String {
        return "ISO:" + getTelephonyManager().simCountryIso
    }

    fun getPhoneType(): String {
        return "PhoneType:" + when (getTelephonyManager().phoneType) {
            TelephonyManager.PHONE_TYPE_NONE -> "PHONE_TYPE_NONE"
            TelephonyManager.PHONE_TYPE_GSM -> "PHONE_TYPE_GSM"
            TelephonyManager.PHONE_TYPE_CDMA -> "PHONE_TYPE_CDMA  "
            else -> ""
        }
    }

    fun getNetworkType(): String {
        return "NetworkType:" + when (getTelephonyManager().networkType) {
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> "NETWORK_TYPE_UNKNOWN"
            TelephonyManager.NETWORK_TYPE_GPRS -> "NETWORK_TYPE_GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> "NETWORK_TYPE_EDGE"
            TelephonyManager.NETWORK_TYPE_UMTS -> "NETWORK_TYPE_UMTS"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "NETWORK_TYPE_HSDPA "
            TelephonyManager.NETWORK_TYPE_HSUPA -> "NETWORK_TYPE_HSUPA    "
            TelephonyManager.NETWORK_TYPE_HSPA -> "NETWORK_TYPE_HSPA     "
            TelephonyManager.NETWORK_TYPE_CDMA -> "NETWORK_TYPE_CDMA     "
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> "NETWORK_TYPE_EVDO_0   "
            TelephonyManager.NETWORK_TYPE_EVDO_A -> "NETWORK_TYPE_EVDO_A   "
            TelephonyManager.NETWORK_TYPE_1xRTT -> "NETWORK_TYPE_1xRTT    "
            else -> ""
        }
    }

    fun getTelephonyManager(): TelephonyManager {
        return getSystemService(TELEPHONY_SERVICE) as TelephonyManager
    }

    fun getCallState(): String {
        return "callState:" + when (getTelephonyManager().callState) {
            TelephonyManager.CALL_STATE_IDLE -> "CALL_STATE_IDLE"
            TelephonyManager.CALL_STATE_RINGING -> "CALL_STATE_RINGING"
            TelephonyManager.CALL_STATE_OFFHOOK -> "CALL_STATE_OFFHOOK"
            else -> ""
        }
    }

    fun getCellLocation(): String {
        // var cellLocation: CellLocation = getTelephonyManager().cellLocation
        return "CellLocation"
    }

    fun getDeviceId(): String {
        return "deviceId:"// + getTelephonyManager().deviceId
    }


    fun getDeviceSoftwareVersion(): String {
        return "IMEI/SV = " //+ getTelephonyManager().deviceSoftwareVersion
    }

    fun getLine1Number(): String {
        return "GSM手机的 MSISDN="// + getTelephonyManager().line1Number
    }

    fun getNeighboringCellInfo(): String {
        var networkCountryIso: String = getTelephonyManager().networkCountryIso
        return "国际长途区号:" + networkCountryIso
    }

    fun getNetworkOperator(): String {
        return "MCC+MNC:" + getTelephonyManager().networkOperator
    }

    fun getNetworkOperatorName(): String {
        return "(当前已注册的用户)的名字:" + getTelephonyManager().networkOperatorName
    }


    private fun log(msg: String) {
        Log.e("MainActivity", msg)
    }

}