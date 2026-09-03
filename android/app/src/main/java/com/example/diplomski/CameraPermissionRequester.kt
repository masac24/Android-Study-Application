package com.example.diplomski

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

object CameraPermissionRequester {

    fun hasPermissions(context: Context): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        )
    }

    fun requestPermissions(
        context: ComponentActivity,
        onCameraGranted: () -> Unit = {},
        onCameraNotGranted: () -> Unit = {}
    ) {
        val permissionRequest = context.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.CAMERA, false) -> {
                    Log.d(TAG, "Granted")
                    onCameraGranted()
                }

                else -> {
                    Log.d(TAG, "Not granted")
                    onCameraNotGranted()
                }
            }
        }
        permissionRequest.launch(arrayOf(Manifest.permission.CAMERA))
    }

    private const val TAG = "CameraPermissionRequester"
}