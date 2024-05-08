package com.lightresearch.probecamera

import android.os.Bundle
import android.os.PowerManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jiangdg.ausbc.utils.Utils
import java.io.File

class SingleCaptureActivity : AppCompatActivity() {

    private var mWakeLock: PowerManager.WakeLock? = null

    override fun onStart() {
        super.onStart()
        mWakeLock = Utils.wakeLock(this)
    }

    override fun onStop() {
        super.onStop()
        mWakeLock?.apply {
            Utils.wakeUnLock(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path: String? = intent.getStringExtra("path")
        val site: String? = intent.getStringExtra("site")
        setContent {
            if(path == null) Text("Path from intent is null", Modifier.padding(8.dp))
            else ProbeCameraPreview(path, true, false, false, site.toString())
        }
    }
}