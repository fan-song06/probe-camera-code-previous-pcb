package com.lightresearch.probecamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.lightresearch.probecamera.databinding.ProbeCameraFragmentContainerBinding

fun createProbeFragmentArguments(dirPath: String, singleCapture: Boolean, useTimestampPrefix: Boolean, showGalleryButton: Boolean, site: String): Bundle {
    val args = Bundle()
    args.putString("path", dirPath)
    args.putString("site", site)
    args.putBoolean("singleCapture", singleCapture)
    args.putBoolean("useTimestampPrefix", useTimestampPrefix)
    args.putBoolean("showGalleryButton", showGalleryButton)
    return args
}

fun createProbeFragment(dirPath: String, singleCapture: Boolean, useTimestampPrefix: Boolean, showGalleryButton: Boolean, site: String): ProbeCameraFragment {
    val fragment = ProbeCameraFragment()

    fragment.arguments = createProbeFragmentArguments(dirPath, singleCapture, useTimestampPrefix, showGalleryButton, site)
    return fragment
}

@Composable
fun ProbeCameraPreview(path: String, singleCapture: Boolean = false, useTimestampPrefix: Boolean = false, showGalleryButton: Boolean = false, site: String) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasReadExternalStoragePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
//    var hasWriteExternalStoragePermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        val updatedCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val updatedReadExternalStoragePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
//        val updatedWriteExternalStoragePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        hasCameraPermission = updatedCameraPermission
        hasReadExternalStoragePermission = updatedReadExternalStoragePermission
//        hasWriteExternalStoragePermission = updatedWriteExternalStoragePermission
    }

    val missingPermissions = remember(hasCameraPermission, hasReadExternalStoragePermission) {
        var missing = listOf<String>()
        if (!hasCameraPermission) missing = missing + listOf(Manifest.permission.CAMERA)
        if (!hasReadExternalStoragePermission) missing =
            missing + listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
//        if(!hasWriteExternalStoragePermission) missing = missing + listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        missing
    }

    val missingPermissionsString = remember(missingPermissions) {
        "Missing permissions: ${
            missingPermissions.map { it -> permissionNames[it] }.joinToString(", ")
        }"
    }

    fun requestMissingPermissions() {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
        else if (!hasReadExternalStoragePermission) permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
//        else if(!hasWriteExternalStoragePermission) permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    val asAppCompat = context as AppCompatActivity

    if (hasCameraPermission && hasReadExternalStoragePermission) {
        FragmentContainer(
            fragmentManager = asAppCompat.supportFragmentManager,
            commit = { add(it, createProbeFragment(path, singleCapture, useTimestampPrefix, showGalleryButton, site)) },
            modifier = Modifier.fillMaxSize()
        )

    } else {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(missingPermissionsString)
            Button(::requestMissingPermissions) {
                Text("Request missing permissions")
            }
        }

    }

}