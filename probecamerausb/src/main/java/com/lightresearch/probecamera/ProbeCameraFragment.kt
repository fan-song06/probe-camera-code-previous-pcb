/*
 * Copyright 2017-2022 Jiangdg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lightresearch.probecamera

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.hardware.usb.UsbDevice
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.SeekBar
import androidx.appcompat.content.res.AppCompatResources
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.jiangdg.ausbc.utils.Logger
import com.jiangdg.ausbc.utils.ToastUtils
import com.jiangdg.ausbc.utils.bus.BusKey
import com.jiangdg.ausbc.utils.bus.EventBus
import com.jiangdg.ausbc.widget.CaptureMediaView
import com.jiangdg.ausbc.widget.IAspectRatio
import com.jiangdg.ausbc.widget.TipView
import com.lightresearch.probecamera.ausbc.GlobalIOIO
import com.lightresearch.probecamera.ausbc.ModifiedCameraFragment
import com.lightresearch.probecamera.ausbc.callback.ICameraStateCallBack
import com.lightresearch.probecamera.ausbc.callback.ICaptureCallBack
import com.lightresearch.probecamera.ausbc.utils.CameraUVC
import com.lightresearch.probecamera.ausbc.utils.CameraUtils
import com.lightresearch.probecamera.ausbc.utils.MultiCameraClient
import com.lightresearch.probecamera.databinding.ProbeCameraFragmentViewBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Timer
import java.util.TimerTask


/** CameraFragment Usage Demo
 *
 * @author Created by jiangdg on 2022/1/28
 */
class ProbeCameraFragment : ModifiedCameraFragment(), View.OnClickListener,
    CaptureMediaView.OnViewClickListener {
    private var mMoreMenu: PopupWindow? = null
    private var isCapturingVideoOrAudio: Boolean = false
    private var mRecTimer: Timer? = null
    private var path: String? = null
    private var site: String? = null
    private var mRecSeconds = 0
    private var mRecMinute = 0
    private var mRecHours = 0
    private var stopCaptureDrawable: Drawable? = null
    private var videoCaptureDrawable: Drawable? = null
    private var globalIoio: GlobalIOIO? = null
    private var timeStamp: String? = null
    private var singleCapture = false
    private var useTimestampPrefix = false
    private var showGalleryButton = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        path = arguments?.getString("path")
        singleCapture = arguments?.getBoolean("singleCapture") ?: false
        useTimestampPrefix = arguments?.getBoolean("useTimestampPrefix") ?: false
        showGalleryButton = arguments?.getBoolean("showGalleryButton") ?: false
        site = arguments?.getString("site")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val ret = super.onCreateView(inflater, container, savedInstanceState)
        val ctx = activity
        Log.w(TAG, if (context != null) "CONTEXT IS NOT NULL" else "CONTEXT IS NULL")
        if (ctx != null) {
            stopCaptureDrawable = AppCompatResources.getDrawable(ctx, R.drawable.baseline_stop_24)
            videoCaptureDrawable =
                AppCompatResources.getDrawable(ctx, R.drawable.baseline_videocam_24)
            globalIoio = GlobalIOIO(this, ctx)
        }

        return ret
    }

    private val mTakePictureTipView: TipView by lazy {
        mViewBinding.takePictureTipViewStub.inflate() as TipView
    }

    private val mMainHandler: Handler by lazy {
        Handler(Looper.getMainLooper()) {
            when (it.what) {
                WHAT_START_TIMER -> {
                    if (mRecSeconds % 2 != 0) {
                        mViewBinding.recStateIv.visibility = View.VISIBLE
                    } else {
                        mViewBinding.recStateIv.visibility = View.INVISIBLE
                    }
                    mViewBinding.recTimeTv.text = calculateTime(mRecSeconds, mRecMinute)
                }

                WHAT_STOP_TIMER -> {
                    mViewBinding.toolbarGroup.visibility = View.VISIBLE
                    mViewBinding.recTimerLayout.visibility = View.GONE
                    mViewBinding.recTimeTv.text = calculateTime(0, 0)
                }
            }
            true
        }
    }


    private lateinit var mViewBinding: ProbeCameraFragmentViewBinding

    override fun initView() {
        super.initView()
//        mViewBinding.resolutionBtn.setOnClickListener(this)
        mViewBinding.usbInfoBtn.setOnClickListener(this)
        mViewBinding.btnImageCapture.setOnClickListener(this)
        mViewBinding.btnVideoCapture.setOnClickListener(this)
        mViewBinding.btnGalleryOpen.visibility = if (showGalleryButton) View.VISIBLE else View.GONE
        if (showGalleryButton) mViewBinding.btnGalleryOpen.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()
        EventBus.with<Int>(BusKey.KEY_FRAME_RATE).observe(viewLifecycleOwner, {
            mViewBinding.frameRateTv.text = "frame rate:  $it fps"
        })
    }

    override fun onCameraState(
        self: MultiCameraClient.ICamera, code: ICameraStateCallBack.State, msg: String?
    ) {
        when (code) {
            ICameraStateCallBack.State.OPENED -> handleCameraOpened()
            ICameraStateCallBack.State.CLOSED -> handleCameraClosed()
            ICameraStateCallBack.State.ERROR -> handleCameraError(msg)
        }
    }

    private fun handleCameraError(msg: String?) {
        mViewBinding.frameRateTv.visibility = View.GONE
        ToastUtils.show("camera opened error: $msg")
    }

    private fun handleCameraClosed() {

        globalIoio?.stop()
        globalIoio?.destroy()
        mViewBinding.frameRateTv.visibility = View.GONE
        ToastUtils.show("camera closed success")
    }

    private fun handleCameraOpened() {
        val cam = getCurrentCamera() as? CameraUVC

        cam?.setAutoWhiteBalance(false)
        cam?.setWhiteBalance(15)
        cam?.setAutoFocus(true)
        //cam?.setFocus(160)

        (getCurrentCamera() as? CameraUVC)?.setExposureMode(0)
        (getCurrentCamera() as? CameraUVC)?.setExposure(15)
        //cam?.setExposure(800)

        cam?.setSharpness(80)
        cam?.setContrast(50)

        mViewBinding.frameRateTv.visibility = View.VISIBLE
        mViewBinding.brightnessSb.max = 100
        mViewBinding.brightnessSb.progress = cam?.getExposure() ?: 0
        mViewBinding.contrastSb.max = 100
        mViewBinding.contrastSb.progress = cam?.getContrast() ?: 0
        Logger.i(
            TAG,
            "max = ${mViewBinding.brightnessSb.max}, progress = ${mViewBinding.brightnessSb.progress}"
        )

        globalIoio?.create()
        globalIoio?.start()
        globalIoio?.led_7(true, 2)

        mViewBinding.brightnessSb.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                (getCurrentCamera() as? CameraUVC)?.setExposure(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        mViewBinding.contrastSb.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                (getCurrentCamera() as? CameraUVC)?.setContrast(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }

    override fun getCameraView(): IAspectRatio {
        return mViewBinding.tvCameraView as IAspectRatio
    }

    override fun getCameraViewContainer(): ViewGroup {
        return mViewBinding.cameraViewContainer
    }

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View {
        mViewBinding = ProbeCameraFragmentViewBinding.inflate(inflater, container, false)
        return mViewBinding.root
    }

    override fun getGravity(): Int = Gravity.CENTER

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    override fun onViewClick(mode: CaptureMediaView.CaptureMode?) {
        if (!isCameraOpened()) {
            ToastUtils.show("camera not worked!")
            return
        }
        when (mode) {
            CaptureMediaView.CaptureMode.MODE_CAPTURE_PIC -> {
                captureImageWL()
            }

            else -> {
                captureVideo()
            }
        }
    }

    private fun getTimestampPrefixFilename(fileName: String, ext: String): String {
        //timeStamp = System.currentTimeMillis()
        //MediaActionSound sound = new MediaActionSound();
        //sound.play(MediaActionSound.SHUTTER_CLICK);

        timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "${timeStamp}_${fileName}.$ext"
        return "$path/$fileName"
    }


    private fun captureVideo() {
        if (isCapturingVideoOrAudio) {
            captureVideoStop()
            if (singleCapture) {
                activity?.setResult(1)
                activity?.finish()
            }
            return
        }
        val dirPath = path ?: throw IllegalArgumentException("Path is null")
        //timeStamp = System.currentTimeMillis()
        timeStamp= SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val filePath = "$path/$timeStamp"
        val dirFile = File(dirPath)
        if(singleCapture && dirFile.exists()) dirFile.deleteRecursively()
        CameraUtils.createDirectoryIfNotExists(dirPath)
        captureVideoStart(object : ICaptureCallBack {
            override fun onBegin() {
                isCapturingVideoOrAudio = true
                mViewBinding.btnVideoCapture.setImageDrawable(stopCaptureDrawable)
                mViewBinding.toolbarGroup.visibility = View.GONE
                mViewBinding.layoutCaptureButton.visibility = View.GONE
                if (showGalleryButton) mViewBinding.btnGalleryOpen.visibility = View.GONE
                mViewBinding.recTimerLayout.visibility = View.VISIBLE
                startMediaTimer()
            }

            override fun onError(error: String?) {
                ToastUtils.show(error ?: "未知异常")
                isCapturingVideoOrAudio = false
                mViewBinding.btnVideoCapture.setImageDrawable(videoCaptureDrawable)
                stopMediaTimer()
            }

            override fun onComplete(path: String?) {
                ToastUtils.show(path ?: "")
                isCapturingVideoOrAudio = false
                mViewBinding.btnVideoCapture.setImageDrawable(videoCaptureDrawable)
                mViewBinding.toolbarGroup.visibility = View.VISIBLE
                mViewBinding.layoutCaptureButton.visibility = View.VISIBLE
                if(showGalleryButton) mViewBinding.btnGalleryOpen.visibility = View.VISIBLE
                mViewBinding.recTimerLayout.visibility = View.GONE
                stopMediaTimer()
            }

        }, filePath)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (view != null) {
            val parentViewGroup = requireView().parent as ViewGroup?
            parentViewGroup?.removeAllViews();
        }
    }

    fun captureImageWL(callback: (() -> Unit)? = null) {

        var name_tag: String  = site + "_probe_WL"
        val filePath =
            if (useTimestampPrefix) getTimestampPrefixFilename(name_tag, "jpg") else "$path/WL.jpg"
        val dirPath =
            CameraUtils.getParentDirectory(filePath) ?: throw Exception("parent directory is null")
        val dirFile = File(dirPath)
        if(singleCapture && dirFile.exists()) dirFile.deleteRecursively()
        CameraUtils.createDirectoryIfNotExists(dirPath)
        captureImage(object : ICaptureCallBack {
            override fun onBegin() {
                mTakePictureTipView.show("", 100)
            }

            override fun onError(error: String?) {
                ToastUtils.show(error ?: "未知异常")
            }

            override fun onComplete(path: String?) {
                onCaptureWLComplete(path, callback)
            }
        }, filePath)
    }

    override fun onClick(v: View?) {
        clickAnimation(v!!, object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                when (v) {
//                    mViewBinding.resolutionBtn -> {
//                        if (!isCameraOpened()) {
//                            ToastUtils.show("camera not worked!")
//                            return
//                        }
//                        showResolutionDialog()
//                    }

                    mViewBinding.usbInfoBtn -> {
                        getCurrentCamera()?.let { strategy ->
                            if (strategy is CameraUVC) {
                                showUsbDevicesDialog(getDeviceList(), strategy.getUsbDevice())
                                return
                            }
                        }
                        return
                    }

                    mViewBinding.btnVideoCapture -> {
                        captureVideo()
                    }

                    mViewBinding.btnImageCapture -> {
                        captureImageWL()
                    }

                    mViewBinding.btnGalleryOpen -> {
                        if(showGalleryButton){
                            goToGalley()
                        }
                    }

                    else -> {
                    }
                }
            }
        })
    }

    @SuppressLint("CheckResult")
    private fun showUsbDevicesDialog(
        usbDeviceList: List<UsbDevice>?, curDevice: UsbDevice?
    ) {
        if (usbDeviceList.isNullOrEmpty()) {
            ToastUtils.show("Get usb device failed")
            return
        }
        val list = arrayListOf<String>()
        var selectedIndex: Int = -1
        for (index in (0 until usbDeviceList.size)) {
            val dev = usbDeviceList[index]
            val devName =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !dev.productName.isNullOrEmpty()) {
                    "${dev.productName}(${curDevice?.deviceId})"
                } else {
                    dev.deviceName
                }
            val curDevName =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !curDevice?.productName.isNullOrEmpty()) {
                    "${curDevice!!.productName}(${curDevice.deviceId})"
                } else {
                    curDevice?.deviceName
                }
            if (devName == curDevName) {
                selectedIndex = index
            }
            list.add(devName)
        }
        MaterialDialog(requireContext()).show {
            listItemsSingleChoice(
                items = list, initialSelection = selectedIndex
            ) { dialog, index, text ->
                if (selectedIndex == index) {
                    return@listItemsSingleChoice
                }
                switchCamera(usbDeviceList[index])
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun showResolutionDialog() {
        mMoreMenu?.dismiss()
        getAllPreviewSizes().let { previewSizes ->
            if (previewSizes.isNullOrEmpty()) {
                ToastUtils.show("Get camera preview size failed")
                return
            }
            val list = arrayListOf<String>()
            var selectedIndex: Int = -1
            for (index in (0 until previewSizes.size)) {
                val w = previewSizes[index].width
                val h = previewSizes[index].height
                getCurrentPreviewSize()?.apply {
                    if (width == w && height == h) {
                        selectedIndex = index
                    }
                }
                list.add("$w x $h")
            }
            MaterialDialog(requireContext()).show {
                listItemsSingleChoice(
                    items = list, initialSelection = selectedIndex
                ) { dialog, index, text ->
                    if (selectedIndex == index) {
                        return@listItemsSingleChoice
                    }
                    updateResolution(previewSizes[index].width, previewSizes[index].height)
                }
            }
        }

    }

    /*
    public void onBackPressed() {
        //

        globalIoio.stop();
        globalIoio.destroy();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        String packageName = "com.example.scanner";
        String className = "com.example.scanner.SelectCameraModel";
        intent.setClassName(packageName, className);
        intent.putExtra("PATIENT_ID", patientid);
        startActivity(intent);

        System.exit(0);
    }
    */

    private fun saveImage(bitmapImage: Bitmap, path: String, callback: (() -> Unit)? = null) {

        // path to /data/data/yourapp/app_data/imageDir
        //File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        val mypath = File(path)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
                if (callback != null) callback()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        //return directory.getAbsolutePath();
    }

    private fun loadImage(path: String): Bitmap? {
        var b: Bitmap? = null
        try {
            val f = File(path)
            b = BitmapFactory.decodeStream(FileInputStream(f))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return b
    }

    fun onCaptureFLComplete(filePathFL: String?, callback: (() -> Unit)?){
        //val cam = getCurrentCamera() as? CameraUVC
        //cam?.setExposureMode(0)

        if (filePathFL == null) throw Exception("file path WL should not be null")
        var bitmap: Bitmap = loadImage(filePathFL) ?: throw Exception("bit map should not be null")
        val width = bitmap.width
        val height = bitmap.height
        val pix = IntArray(width * height)
        val gPix = IntArray(width * height)
        bitmap.getPixels(
            pix, 0, width, 0, 0, width, height
        )

        for (i in 0 until height) {
            for (j in 0 until width) {
                //color的8字节分别为alpha通道，red通道，green通道，blue通道，各占2字节
                val color: Int = pix.get(width * i + j)
                val alphaValue = color shr 24 and 0xff
                var redValue = color shr 16 and 0xff
                var greenValue = color shr 8 and 0xff
                var blueValue = color and 0xff
                redValue = (redValue * 0.8).toInt() // changed 08/12/2019
                greenValue = (greenValue * 1.0).toInt() // changed 08/12/2019
                blueValue = blueValue - (blueValue * 0.7).toInt()
                if (redValue > 255) {
                    redValue = 255
                } else if (redValue < 0) {
                    redValue = 0
                }
                if (greenValue > 255) {
                    greenValue = 255
                } else if (greenValue < 0) {
                    greenValue = 0
                }
                if (blueValue > 255) {
                    blueValue = 255
                } else if (blueValue < 0) {
                    blueValue = 0
                }
                gPix[width * i + j] =
                    alphaValue shl 24 or (redValue shl 16) or (greenValue shl 8) or blueValue
            }
        }

        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        bitmap.setPixels(
            gPix, 0, width, 0, 0, width, height
        )
//        val file = File(filePathFL)
//        file.delete()
        saveImage(bitmap, filePathFL) {
            Thread.sleep(500)

            //val cam = getCurrentCamera() as? CameraUVC
            //cam?.setExposureMode(0)
            //cam?.setExposure(1)
            (getCurrentCamera() as? CameraUVC)?.setExposure(15)
            //(getCurrentCamera() as? CameraUVC)?.setExposureMode(0)
            globalIoio?.led_6(false, 2);
            globalIoio?.led_7(true, 2);
            if (callback != null) callback()
            if (singleCapture) {
                activity?.setResult(1)
                activity?.finish()
            }
        }
        //val cam = getCurrentCamera() as? CameraUVC
        //cam?.setExposureMode(0)
        //cam?.setExposure(1)
    }
    fun onCaptureWLComplete(filePathWL: String?, callback: (() -> Unit)?) {
        if (filePathWL == null) throw Exception("file path WL should not be null")

        globalIoio?.led_7(false, 3);

        //val cam = getCurrentCamera() as? CameraUVC
        //cam?.setExposureMode(1)
        //cam?.setExposure(80)
        //(getCurrentCamera() as? CameraUVC)?.setExposureMode(1)
        (getCurrentCamera() as? CameraUVC)?.setExposure(36)
        Thread.sleep(50)
        globalIoio?.led_6(true, 3);
        Thread.sleep(500)

        var name_tag: String  = site + "_probe_FL"
        var filePathFL: String = filePathWL.replace("WL","FL")
        //val filePathFL =
        //    if (useTimestampPrefix) getTimestampPrefixFilename(name_tag, "jpg") else "$path/FL.jpg"
        val file = File(filePathFL)
        file.delete()

        captureImage(object : ICaptureCallBack {
            override fun onBegin() {
                mTakePictureTipView.show("", 100)
            }

            override fun onError(error: String?) {
                ToastUtils.show(error ?: "未知异常")
            }

            override fun onComplete(path: String?) {
                onCaptureFLComplete(path, callback)
            }
        }, filePathFL)
    }


    private fun getVersionName(): String? {
        context ?: return null
        val packageManager = requireContext().packageManager
        try {
            val packageInfo = packageManager?.getPackageInfo(requireContext().packageName, 0)
            return packageInfo?.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    private fun goToGalley() {
        //val cam = getCurrentCamera() as? CameraUVC
        //cam?.setExposureMode(1)
        //cam?.setExposure(1000)
        try {
            val intent = Intent(context, GalleryActivity::class.java)
            intent.putExtra("directory_path", path)
            context?.startActivity(intent)
        } catch (e: Exception) {
            ToastUtils.show("open error: ${e.localizedMessage}")
        }
    }

    private fun clickAnimation(v: View, listener: Animator.AnimatorListener) {
        val scaleXAnim: ObjectAnimator = ObjectAnimator.ofFloat(v, "scaleX", 1.0f, 0.7f, 1.0f)
        val scaleYAnim: ObjectAnimator = ObjectAnimator.ofFloat(v, "scaleY", 1.0f, 0.7f, 1.0f)
        val alphaAnim: ObjectAnimator = ObjectAnimator.ofFloat(v, "alpha", 1.0f, 0.7f, 1.0f)
        val animatorSet = AnimatorSet()
        animatorSet.duration = 150
        animatorSet.addListener(listener)
        animatorSet.playTogether(scaleXAnim, scaleYAnim, alphaAnim)
        animatorSet.start()
    }

    private fun startMediaTimer() {
        val pushTask: TimerTask = object : TimerTask() {
            override fun run() {
                //秒
                mRecSeconds++
                //分
                if (mRecSeconds >= 60) {
                    mRecSeconds = 0
                    mRecMinute++
                }
                //时
                if (mRecMinute >= 60) {
                    mRecMinute = 0
                    mRecHours++
                    if (mRecHours >= 24) {
                        mRecHours = 0
                        mRecMinute = 0
                        mRecSeconds = 0
                    }
                }
                mMainHandler.sendEmptyMessage(WHAT_START_TIMER)
            }
        }
        if (mRecTimer != null) {
            stopMediaTimer()
        }
        mRecTimer = Timer()
        //执行schedule后1s后运行run，之后每隔1s运行run
        mRecTimer?.schedule(pushTask, 1000, 1000)
    }

    private fun stopMediaTimer() {
        if (mRecTimer != null) {
            mRecTimer?.cancel()
            mRecTimer = null
        }
        mRecHours = 0
        mRecMinute = 0
        mRecSeconds = 0
        mMainHandler.sendEmptyMessage(WHAT_STOP_TIMER)
    }

    private fun calculateTime(seconds: Int, minute: Int, hour: Int? = null): String {
        val mBuilder = java.lang.StringBuilder()
        //时
        if (hour != null) {
            if (hour < 10) {
                mBuilder.append("0")
                mBuilder.append(hour)
            } else {
                mBuilder.append(hour)
            }
            mBuilder.append(":")
        }
        // 分
        if (minute < 10) {
            mBuilder.append("0")
            mBuilder.append(minute)
        } else {
            mBuilder.append(minute)
        }
        //秒
        mBuilder.append(":")
        if (seconds < 10) {
            mBuilder.append("0")
            mBuilder.append(seconds)
        } else {
            mBuilder.append(seconds)
        }
        return mBuilder.toString()
    }

    companion object {
        private const val TAG = "ProbeCameraFragment"
        private const val WHAT_START_TIMER = 0x00
        private const val WHAT_STOP_TIMER = 0x01
    }
}

val permissionNames = mapOf(
    Manifest.permission.CAMERA to "Camera",
    Manifest.permission.READ_EXTERNAL_STORAGE to "Read external storage",
    Manifest.permission.WRITE_EXTERNAL_STORAGE to "Write external storage"
)
