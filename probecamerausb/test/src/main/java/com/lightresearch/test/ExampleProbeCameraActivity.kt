package com.lightresearch.test

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.os.PowerManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jiangdg.ausbc.utils.Utils
import com.lightresearch.probecamera.ProbeCameraPreview
import java.io.File


class ExampleProbeCameraActivity : AppCompatActivity() {

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

        var patientid = intent.getStringExtra("PATIENT_ID")
        var location_tag = intent.getStringExtra("LOCATION")
        var sub_dir : String = "/intraoral/patientPhoto/"+patientid+"/usb_camera"

        val externalFilesDir = getExternalFilesDir(null)
        val file = if(externalFilesDir != null) File(getExternalFilesDir(null), sub_dir) else null
        val externalStorageDir = getExternalStorageDirectory()
        val filex = File(externalStorageDir.toString()  + sub_dir)
        //filex.mkdirs()



        //Toast.makeText(this, file.toString(), Toast.LENGTH_SHORT).show()
        //Toast.makeText(this, location_tag.toString(), Toast.LENGTH_SHORT).show()
        setContent {
            if(file == null) Text("External files dir is null", Modifier.padding(8.dp))
            else ProbeCameraPreview(file.absolutePath, false, true, true, location_tag.toString())
        }
    }

}