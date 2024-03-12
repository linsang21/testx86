package com.example.x86example

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wificall.ChimeSDKMeeting
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    private val WEBRTC_PERM = arrayOf(
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )
    private val WEBRTC_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        joinMeeting()
    }

    private fun  joinMeeting() {
        if (hasPermissionsAlready()) {
            ChimeSDKMeeting().startMeeting(applicationContext)
        } else {
            ActivityCompat.requestPermissions(this, WEBRTC_PERM, WEBRTC_PERMISSION_REQUEST_CODE)
        }
    }

    private fun hasPermissionsAlready(): Boolean {
        return WEBRTC_PERM.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissionsList: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissionsList, grantResults)
        when (requestCode) {
            WEBRTC_PERMISSION_REQUEST_CODE -> {
                val isMissingPermission: Boolean =
                    grantResults.isEmpty() || grantResults.any { PackageManager.PERMISSION_GRANTED != it }

                if (isMissingPermission) {
                    return
                }
                ChimeSDKMeeting().startMeeting(applicationContext)
            }
        }
    }
}