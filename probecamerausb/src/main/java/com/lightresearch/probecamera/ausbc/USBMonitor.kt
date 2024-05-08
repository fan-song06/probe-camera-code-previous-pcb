/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */
package com.lightresearch.probecamera.ausbc

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import androidx.annotation.RequiresApi
import com.jiangdg.ausbc.utils.ToastUtils
import com.lightresearch.probecamera.ausbc.utils.BuildCheck
import com.lightresearch.probecamera.ausbc.utils.DeviceFilter
import com.lightresearch.probecamera.ausbc.utils.HandlerThreadHandler
import com.lightresearch.probecamera.ausbc.utils.USBVendorId
import com.lightresearch.probecamera.ausbc.utils.XLogWrapper
import java.io.UnsupportedEncodingException
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

private const val USB_DIR_OUT = 0
private const val USB_DIR_IN = 0x80
private const val USB_TYPE_MASK = 0x03 shl 5
private const val USB_TYPE_STANDARD = 0x00 shl 5
private const val USB_TYPE_CLASS = 0x01 shl 5
private const val USB_TYPE_VENDOR = 0x02 shl 5
private const val USB_TYPE_RESERVED = 0x03 shl 5
private const val USB_RECIP_MASK = 0x1f
private const val USB_RECIP_DEVICE = 0x00
private const val USB_RECIP_INTERFACE = 0x01
private const val USB_RECIP_ENDPOINT = 0x02
private const val USB_RECIP_OTHER = 0x03
private const val USB_RECIP_PORT = 0x04
private const val USB_RECIP_RPIPE = 0x05
private const val USB_REQ_GET_STATUS = 0x00
private const val USB_REQ_CLEAR_FEATURE = 0x01
private const val USB_REQ_SET_FEATURE = 0x03
private const val USB_REQ_SET_ADDRESS = 0x05
private const val USB_REQ_GET_DESCRIPTOR = 0x06
private const val USB_REQ_SET_DESCRIPTOR = 0x07
private const val USB_REQ_GET_CONFIGURATION = 0x08
private const val USB_REQ_SET_CONFIGURATION = 0x09
private const val USB_REQ_GET_INTERFACE = 0x0A
private const val USB_REQ_SET_INTERFACE = 0x0B
private const val USB_REQ_SYNCH_FRAME = 0x0C
private const val USB_REQ_SET_SEL = 0x30
private const val USB_REQ_SET_ISOCH_DELAY = 0x31
private const val USB_REQ_SET_ENCRYPTION = 0x0D
private const val USB_REQ_GET_ENCRYPTION = 0x0E
private const val USB_REQ_RPIPE_ABORT = 0x0E
private const val USB_REQ_SET_HANDSHAKE = 0x0F
private const val USB_REQ_RPIPE_RESET = 0x0F
private const val USB_REQ_GET_HANDSHAKE = 0x10
private const val USB_REQ_SET_CONNECTION = 0x11
private const val USB_REQ_SET_SECURITY_DATA = 0x12
private const val USB_REQ_GET_SECURITY_DATA = 0x13
private const val USB_REQ_SET_WUSB_DATA = 0x14
private const val USB_REQ_LOOPBACK_DATA_WRITE = 0x15
private const val USB_REQ_LOOPBACK_DATA_READ = 0x16
private const val USB_REQ_SET_INTERFACE_DS = 0x17

private const val USB_REQ_STANDARD_DEVICE_SET =
    USB_DIR_OUT or USB_TYPE_STANDARD or USB_RECIP_DEVICE // 0x10

private const val USB_REQ_STANDARD_DEVICE_GET =
    USB_DIR_IN or USB_TYPE_STANDARD or USB_RECIP_DEVICE // 0x90

private const val USB_REQ_STANDARD_INTERFACE_SET =
    USB_DIR_OUT or USB_TYPE_STANDARD or USB_RECIP_INTERFACE // 0x11

private const val USB_REQ_STANDARD_INTERFACE_GET =
    USB_DIR_IN or USB_TYPE_STANDARD or USB_RECIP_INTERFACE // 0x91

private const val USB_REQ_STANDARD_ENDPOINT_SET =
    USB_DIR_OUT or USB_TYPE_STANDARD or USB_RECIP_ENDPOINT // 0x12

private const val USB_REQ_STANDARD_ENDPOINT_GET =
    USB_DIR_IN or USB_TYPE_STANDARD or USB_RECIP_ENDPOINT // 0x92


private const val USB_REQ_CS_DEVICE_SET = USB_DIR_OUT or USB_TYPE_CLASS or USB_RECIP_DEVICE // 0x20

private const val USB_REQ_CS_DEVICE_GET = USB_DIR_IN or USB_TYPE_CLASS or USB_RECIP_DEVICE // 0xa0

private const val USB_REQ_CS_INTERFACE_SET =
    USB_DIR_OUT or USB_TYPE_CLASS or USB_RECIP_INTERFACE // 0x21

private const val USB_REQ_CS_INTERFACE_GET =
    USB_DIR_IN or USB_TYPE_CLASS or USB_RECIP_INTERFACE // 0xa1

private const val USB_REQ_CS_ENDPOINT_SET =
    USB_DIR_OUT or USB_TYPE_CLASS or USB_RECIP_ENDPOINT // 0x22

private const val USB_REQ_CS_ENDPOINT_GET =
    USB_DIR_IN or USB_TYPE_CLASS or USB_RECIP_ENDPOINT // 0xa2


private const val USB_REQ_VENDER_DEVICE_SET =
    USB_DIR_OUT or USB_TYPE_CLASS or USB_RECIP_DEVICE // 0x40

private const val USB_REQ_VENDER_DEVICE_GET =
    USB_DIR_IN or USB_TYPE_CLASS or USB_RECIP_DEVICE // 0xc0

private const val USB_REQ_VENDER_INTERFACE_SET =
    USB_DIR_OUT or USB_TYPE_CLASS or USB_RECIP_INTERFACE // 0x41

private const val USB_REQ_VENDER_INTERFACE_GET =
    USB_DIR_IN or USB_TYPE_CLASS or USB_RECIP_INTERFACE // 0xc1

private const val USB_REQ_VENDER_ENDPOINT_SET =
    USB_DIR_OUT or USB_TYPE_CLASS or USB_RECIP_ENDPOINT // 0x42

private const val USB_REQ_VENDER_ENDPOINT_GET =
    USB_DIR_IN or USB_TYPE_CLASS or USB_RECIP_ENDPOINT // 0xc2


private const val USB_DT_DEVICE = 0x01
private const val USB_DT_CONFIG = 0x02
private const val USB_DT_STRING = 0x03
private const val USB_DT_INTERFACE = 0x04
private const val USB_DT_ENDPOINT = 0x05
private const val USB_DT_DEVICE_QUALIFIER = 0x06
private const val USB_DT_OTHER_SPEED_CONFIG = 0x07
private const val USB_DT_INTERFACE_POWER = 0x08
private const val USB_DT_OTG = 0x09
private const val USB_DT_DEBUG = 0x0a
private const val USB_DT_INTERFACE_ASSOCIATION = 0x0b
private const val USB_DT_SECURITY = 0x0c
private const val USB_DT_KEY = 0x0d
private const val USB_DT_ENCRYPTION_TYPE = 0x0e
private const val USB_DT_BOS = 0x0f
private const val USB_DT_DEVICE_CAPABILITY = 0x10
private const val USB_DT_WIRELESS_ENDPOINT_COMP = 0x11
private const val USB_DT_WIRE_ADAPTER = 0x21
private const val USB_DT_RPIPE = 0x22
private const val USB_DT_CS_RADIO_CONTROL = 0x23
private const val USB_DT_PIPE_USAGE = 0x24
private const val USB_DT_SS_ENDPOINT_COMP = 0x30
private const val USB_DT_CS_DEVICE = USB_TYPE_CLASS or USB_DT_DEVICE
private const val USB_DT_CS_CONFIG = USB_TYPE_CLASS or USB_DT_CONFIG
private const val USB_DT_CS_STRING = USB_TYPE_CLASS or USB_DT_STRING
private const val USB_DT_CS_INTERFACE = USB_TYPE_CLASS or USB_DT_INTERFACE
private const val USB_DT_CS_ENDPOINT = USB_TYPE_CLASS or USB_DT_ENDPOINT
private const val USB_DT_DEVICE_SIZE = 18


class USBMonitor(context: Context, listener: OnDeviceConnectListener?) {
    private val ACTION_USB_PERMISSION = ACTION_USB_PERMISSION_BASE + hashCode()

    /**
     * openしているUsbControlBlock
     */
    private val mCtrlBlocks = ConcurrentHashMap<UsbDevice?, UsbControlBlock>()
    private val mHasPermissions = SparseArray<WeakReference<UsbDevice?>?>()
    private val mWeakContext: WeakReference<Context>
    protected lateinit var mInfo: UsbDeviceInfo
    private lateinit var mUsbManager: UsbManager
    private lateinit var mOnDeviceConnectListener: OnDeviceConnectListener
    private var mPermissionIntent: PendingIntent? = null
    private val mDeviceFilters: MutableList<DeviceFilter> = ArrayList<DeviceFilter>()

    /**
     * コールバックをワーカースレッドで呼び出すためのハンドラー
     */
    private lateinit var mAsyncHandler: Handler

    @Volatile
    private var destroyed: Boolean = true

    /**
     * USB機器の状態変更時のコールバックリスナー
     */
    interface OnDeviceConnectListener {
        /**
         * called when device attached
         * @param device
         */
        fun onAttach(device: UsbDevice?)

        /**
         * called when device dettach(after onDisconnect)
         * @param device
         */
        fun onDetach(device: UsbDevice?)

        /**
         * called after device opend
         * @param device
         * @param ctrlBlock
         * @param createNew
         */
        fun onConnect(device: UsbDevice?, ctrlBlock: UsbControlBlock?, createNew: Boolean)

        /**
         * called when USB device removed or its power off (this callback is called after device closing)
         * @param device
         * @param ctrlBlock
         */
        fun onDisconnect(device: UsbDevice?, ctrlBlock: UsbControlBlock?)

        /**
         * called when canceled or could not get permission from user
         * @param device
         */
        fun onCancel(device: UsbDevice?)
    }

    /**
     * Release all related resources,
     * never reuse again
     */
    fun destroy() {
        if (DEBUG) XLogWrapper.i(TAG, "destroy:")
        unregister()
        if (!destroyed) {
            destroyed = true
            // モニターしているUSB機器を全てcloseする
            val keys: Set<UsbDevice?> = mCtrlBlocks.keys
            if (keys != null) {
                var ctrlBlock: UsbControlBlock?
                try {
                    for (key in keys) {
                        ctrlBlock = mCtrlBlocks.remove(key)
                        ctrlBlock?.close()
                    }
                } catch (e: Exception) {
                    XLogWrapper.e(TAG, "destroy:", e)
                }
            }
            mCtrlBlocks.clear()
            try {
                mAsyncHandler.looper.quit()
            } catch (e: Exception) {
                XLogWrapper.e(TAG, "destroy:", e)
            }
        }
    }

    /**
     * register BroadcastReceiver to monitor USB events
     * @throws IllegalStateException
     */
    @SuppressLint("UnspecifiedImmutableFlag", "WrongConstant")
    @Synchronized
    @Throws(
        IllegalStateException::class
    )
    fun register() {
        check(!destroyed) { "already destroyed" }
        if (mPermissionIntent == null) {
            if (DEBUG) XLogWrapper.i(TAG, "register:")
            val context = mWeakContext.get()
            if (context != null) {
                mPermissionIntent = if (Build.VERSION.SDK_INT >= 31) {
                    // avoid acquiring intent data failed in receiver on Android12
                    // when using PendingIntent.FLAG_IMMUTABLE
                    // because it means Intent can't be modified anywhere -- jiangdg/20220929
                    val PENDING_FLAG_IMMUTABLE = 1 shl 25
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(ACTION_USB_PERMISSION),
                        PENDING_FLAG_IMMUTABLE
                    )
                } else {
                    PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)
                }
                val filter = IntentFilter(ACTION_USB_PERMISSION)
                // ACTION_USB_DEVICE_ATTACHED never comes on some devices so it should not be added here
                filter.addAction(ACTION_USB_DEVICE_ATTACHED)
                filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
                context.registerReceiver(mUsbReceiver, filter)
            }
            // start connection check
            mDeviceCounts = 0
            mAsyncHandler.postDelayed(mDeviceCheckRunnable, 1000)
        }
    }

    /**
     * unregister BroadcastReceiver
     * @throws IllegalStateException
     */
    @Synchronized
    @Throws(IllegalStateException::class)
    fun unregister() {
        // 接続チェック用Runnableを削除
        mDeviceCounts = 0
        if (!destroyed) {
            mAsyncHandler.removeCallbacks(mDeviceCheckRunnable)
        }
        if (mPermissionIntent != null) {
//			if (DEBUG) XLogWrapper.i(TAG, "unregister:");
            val context = mWeakContext.get()
            try {
                context?.unregisterReceiver(mUsbReceiver)
            } catch (e: Exception) {
                XLogWrapper.w(TAG, e)
            }
            mPermissionIntent = null
        }
    }

    @get:Synchronized
    val isRegistered: Boolean
        get() = !destroyed && mPermissionIntent != null

    /**
     * set device filter
     * @param filter
     * @throws IllegalStateException
     */
    @Throws(IllegalStateException::class)
    fun setDeviceFilter(filter: DeviceFilter) {
        check(!destroyed) { "already destroyed" }
        mDeviceFilters.clear()
        mDeviceFilters.add(filter)
    }

    /**
     * デバイスフィルターを追加
     * @param filter
     * @throws IllegalStateException
     */
    @Throws(IllegalStateException::class)
    fun addDeviceFilter(filter: DeviceFilter) {
        check(!destroyed) { "already destroyed" }
        mDeviceFilters.add(filter)
    }

    /**
     * デバイスフィルターを削除
     * @param filter
     * @throws IllegalStateException
     */
    @Throws(IllegalStateException::class)
    fun removeDeviceFilter(filter: DeviceFilter) {
        check(!destroyed) { "already destroyed" }
        mDeviceFilters.remove(filter)
    }

    /**
     * set device filters
     * @param filters
     * @throws IllegalStateException
     */
    @Throws(IllegalStateException::class)
    fun setDeviceFilter(filters: List<DeviceFilter>?) {
        check(!destroyed) { "already destroyed" }
        mDeviceFilters.clear()
        mDeviceFilters.addAll(filters!!)
    }

    /**
     * add device filters
     * @param filters
     * @throws IllegalStateException
     */
    @Throws(IllegalStateException::class)
    fun addDeviceFilter(filters: List<DeviceFilter>?) {
        check(!destroyed) { "already destroyed" }
        mDeviceFilters.addAll(filters!!)
    }

    /**
     * remove device filters
     * @param filters
     */
    @Throws(IllegalStateException::class)
    fun removeDeviceFilter(filters: List<DeviceFilter>?) {
        check(!destroyed) { "already destroyed" }
        mDeviceFilters.removeAll(filters!!)
    }

    /**
     * return the number of connected USB devices that matched device filter
     * @return
     * @throws IllegalStateException
     */
    @get:Throws(IllegalStateException::class)
    val deviceCount: Int
        get() {
            check(!destroyed) { "already destroyed" }
            return deviceList.size
        }

    /**
     * return device list, return empty list if no device matched
     * @return
     * @throws IllegalStateException
     */
    @get:Throws(IllegalStateException::class)
    val deviceList: List<UsbDevice>
        get() {
            check(!destroyed) { "already destroyed" }
            return getDeviceList(mDeviceFilters)
        }

    /**
     * return device list, return empty list if no device matched
     * @param filters
     * @return
     * @throws IllegalStateException
     */
    @Throws(IllegalStateException::class)
    fun getDeviceList(filters: List<DeviceFilter?>?): List<UsbDevice> {
        check(!destroyed) { "already destroyed" }
        // get detected devices
        val deviceList = mUsbManager.deviceList
        val result: MutableList<UsbDevice> = ArrayList()
        if (deviceList != null) {
            if (filters == null || filters.isEmpty()) {
                result.addAll(deviceList.values)
            } else {
                for (device in deviceList.values) {
                    // match devices
                    for (filter in filters) {
                        if (filter != null && filter.matches(device) || filter != null && filter.mSubclass === device.deviceSubclass) {
                            // when filter matches
                            if (!filter.isExclude) {
                                result.add(device)
                            }
                            break
                        }
                    }
                }
            }
        }
        return result
    }

    /**
     * return device list, return empty list if no device matched
     * @param filter
     * @return
     * @throws IllegalStateException
     */
    @Throws(IllegalStateException::class)
    fun getDeviceList(filter: DeviceFilter?): List<UsbDevice> {
        check(!destroyed) { "already destroyed" }
        val deviceList = mUsbManager.deviceList
        val result: MutableList<UsbDevice> = ArrayList()
        if (deviceList != null) {
            for (device in deviceList.values) {
                if (filter == null || filter.matches(device) && !filter.isExclude) {
                    result.add(device)
                }
            }
        }
        return result
    }

    /**
     * get USB device list, without filter
     * @return
     * @throws IllegalStateException
     */
    @get:Throws(IllegalStateException::class)
    val devices: Iterator<UsbDevice>?
        get() {
            check(!destroyed) { "already destroyed" }
            var iterator: Iterator<UsbDevice>? = null
            val list = mUsbManager.deviceList
            if (list != null) iterator = list.values.iterator()
            return iterator
        }

    /**
     * output device list to XLogWrapperCat
     */
    fun dumpDevices() {
        val list = mUsbManager.deviceList
        if (list != null) {
            val keys: Set<String> = list.keys
            if (keys != null && keys.size > 0) {
                val sb = StringBuilder()
                for (key in keys) {
                    val device = list[key]
                    val num_interface = device?.interfaceCount ?: 0
                    sb.setLength(0)
                    for (i in 0 until num_interface) {
                        sb.append(
                            String.format(
                                Locale.US,
                                "interface%d:%s",
                                i,
                                device!!.getInterface(i).toString()
                            )
                        )
                    }
                    XLogWrapper.i(TAG, "key=$key:$device:$sb")
                }
            } else {
                XLogWrapper.i(TAG, "no device")
            }
        } else {
            XLogWrapper.i(TAG, "no device")
        }
    }

    /**
     * return whether the specific Usb device has permission
     * @param device
     * @return true: 指定したUsbDeviceにパーミッションがある
     * @throws IllegalStateException
     */
    @Throws(IllegalStateException::class)
    fun hasPermission(device: UsbDevice?): Boolean {
        if (destroyed) {
            XLogWrapper.w(TAG, "hasPermission failed, camera destroyed!")
            return false
        }
        return updatePermission(device, device != null && mUsbManager.hasPermission(device))
    }

    /**
     * 内部で保持しているパーミッション状態を更新
     * @param device
     * @param hasPermission
     * @return hasPermission
     */
    private fun updatePermission(device: UsbDevice?, hasPermission: Boolean): Boolean {
        // fix api >= 29, permission SecurityException
        try {
            val deviceKey = getDeviceKey(device, true)
            synchronized(mHasPermissions) {
                if (hasPermission) {
                    if (mHasPermissions[deviceKey] == null) {
                        mHasPermissions.put(
                            deviceKey,
                            WeakReference(device)
                        )
                    }
                } else {
                    mHasPermissions.remove(deviceKey)
                }
            }
        } catch (e: SecurityException) {
            XLogWrapper.w("jiangdg", e.localizedMessage)
        }
        return hasPermission
    }

    /**
     * request permission to access to USB device
     * @param device
     * @return true if fail to request permission
     */
    @Synchronized
    fun requestPermission(device: UsbDevice?): Boolean {
//		if (DEBUG) XLogWrapper.v(TAG, "requestPermission:device=" + device);
        var result = false
        if (isRegistered) {
            if (device != null) {
                if (DEBUG) XLogWrapper.i(
                    TAG,
                    "request permission, has permission: " + mUsbManager.hasPermission(device)
                )
                if (mUsbManager.hasPermission(device)) {
                    // call onConnect if app already has permission
                    processConnect(device)
                } else {
                    try {
                        // パーミッションがなければ要求する
                        if (DEBUG) XLogWrapper.i(TAG, "start request permission...")
                        mUsbManager.requestPermission(device, mPermissionIntent)
                    } catch (e: Exception) {
                        // Android5.1.xのGALAXY系でandroid.permission.sec.MDM_APP_MGMTという意味不明の例外生成するみたい
                        XLogWrapper.w(
                            TAG,
                            "request permission failed, e = " + e.localizedMessage,
                            e
                        )
                        processCancel(device)
                        result = true
                    }
                }
            } else {
                if (DEBUG) XLogWrapper.w(TAG, "request permission failed, device is null?")
                processCancel(device)
                result = true
            }
        } else {
            if (DEBUG) XLogWrapper.w(TAG, "request permission failed, not registered?")
            processCancel(device)
            result = true
        }
        return result
    }

    /**
     * 指定したUsbDeviceをopenする
     * @param device
     * @return
     * @throws SecurityException パーミッションがなければSecurityExceptionを投げる
     */
    @Throws(SecurityException::class)
    fun openDevice(device: UsbDevice): UsbControlBlock {
        return if (hasPermission(device)) {
            var result = mCtrlBlocks[device]
            if (result == null) {
                result = UsbControlBlock(this@USBMonitor, device) // この中でopenDeviceする
                mCtrlBlocks[device] = result
            }
            result
        } else {
            throw SecurityException("has no permission")
        }
    }

    /**
     * BroadcastReceiver for USB permission
     */
    private val mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (destroyed) return
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                // when received the result of requesting USB permission
                synchronized(this@USBMonitor) {
                    val device =
                        intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // get permission, call onConnect
                            if (DEBUG) XLogWrapper.w(
                                TAG,
                                "get permission success in mUsbReceiver"
                            )
                            processConnect(device)
                        }
                    } else {
                        // failed to get permission
                        if (DEBUG) XLogWrapper.w(
                            TAG,
                            "get permission failed in mUsbReceiver"
                        )
                        processCancel(device)
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                updatePermission(device, hasPermission(device))
                processAttach(device)
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                // when device removed
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (device != null) {
                    val ctrlBlock = mCtrlBlocks.remove(device)
                    ctrlBlock?.close()
                    mDeviceCounts = 0
                    processDettach(device)
                }
            }
        }
    }

    /** number of connected & detected devices  */
    @Volatile
    private var mDeviceCounts = 0

    /**
     * periodically check connected devices and if it changed, call onAttach
     */
    private val mDeviceCheckRunnable: Runnable = object : Runnable {
        override fun run() {
            if (destroyed) return
            val devices: List<UsbDevice> = deviceList
            val n = devices.size
            val hasPermissionCounts: Int
            val m: Int
            synchronized(mHasPermissions) {
                hasPermissionCounts = mHasPermissions.size()
                mHasPermissions.clear()
                for (device in devices) {
                    hasPermission(device)
                }
                m = mHasPermissions.size()
            }
            if (n > mDeviceCounts || (m > hasPermissionCounts)) {
                mDeviceCounts = n
                if (mOnDeviceConnectListener != null) {
                    for (i in 0 until n) {
                        val device = devices[i]
                        mAsyncHandler.post { mOnDeviceConnectListener.onAttach(device) }
                    }
                }
            }
            mAsyncHandler.postDelayed(this, 2000) // confirm every 2 seconds
        }
    }

    /**
     * open specific USB device
     * @param device UsbDevice
     */
    private fun processConnect(device: UsbDevice) {
        if (destroyed) return
        updatePermission(device, true)
        mAsyncHandler.post {
            if (DEBUG) XLogWrapper.v(
                TAG,
                "processConnect:device=" + device.deviceName
            )
            var ctrlBlock: UsbControlBlock?
            val createNew: Boolean
            ctrlBlock = mCtrlBlocks[device]
            if (ctrlBlock == null) {
                ctrlBlock = UsbControlBlock(this@USBMonitor, device)
                mCtrlBlocks[device] = ctrlBlock
                createNew = true
            } else {
                createNew = false
            }
            if (mOnDeviceConnectListener != null) {
                if (ctrlBlock.connection == null) {
                    XLogWrapper.e(TAG, "processConnect: Open device failed")
                    mOnDeviceConnectListener.onCancel(device)
                    return@post
                }
                mOnDeviceConnectListener.onConnect(device, ctrlBlock, createNew)
            }
        }
    }

    private fun processCancel(device: UsbDevice?) {
        if (destroyed) return
        if (DEBUG) XLogWrapper.v(TAG, "processCancel:")
        updatePermission(device, false)
        if (mOnDeviceConnectListener != null) {
            mAsyncHandler.post { mOnDeviceConnectListener.onCancel(device) }
        }
    }

    private fun processAttach(device: UsbDevice?) {
        if (destroyed) return
        if (DEBUG) XLogWrapper.v(TAG, "processAttach:")
        if (mOnDeviceConnectListener != null) {
            mAsyncHandler.post { mOnDeviceConnectListener.onAttach(device) }
        }
    }

    private fun processDettach(device: UsbDevice) {
        if (destroyed) return
        if (DEBUG) XLogWrapper.v(TAG, "processDettach:")
        if (mOnDeviceConnectListener != null) {
            mAsyncHandler.post { mOnDeviceConnectListener.onDetach(device) }
        }
    }

    init {
        Log.w(TAG, "USB MONITOR INIT")
        if (DEBUG) XLogWrapper.v(TAG, "USBMonitor:Constructor")
        requireNotNull(listener) { "OnDeviceConnectListener should not null." }
        mWeakContext = WeakReference(context)
        mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        mOnDeviceConnectListener = listener
        mAsyncHandler = HandlerThreadHandler.createHandler(TAG)
        destroyed = false
        if (DEBUG) XLogWrapper.v(
            TAG,
            "USBMonitor:mUsbManager=$mUsbManager"
        )
    }

    /**
     * ベンダー名・製品名・バージョン・シリアルを取得する
     * @param device
     * @return
     */
    fun getDeviceInfo(device: UsbDevice?): UsbDeviceInfo? {
        return updateDeviceInfo(mUsbManager, device, null)
    }

    /**
     * control class
     * never reuse the instance when it closed
     */
    public class UsbControlBlock : Cloneable {
        private val mWeakMonitor: WeakReference<USBMonitor?>
        private val mWeakDevice: WeakReference<UsbDevice?>

        /**
         * get UsbDeviceConnection
         * @return
         */
        @get:Synchronized
        var connection: UsbDeviceConnection?
            protected set
        protected val mInfo: UsbDeviceInfo?
        val busNum: Int
        val devNum: Int
        private val mInterfaces = SparseArray<SparseArray<UsbInterface>>()

        /**
         * this class needs permission to access USB device before constructing
         * @param monitor
         * @param device
         */
        constructor(monitor: USBMonitor, device: UsbDevice) {
            if (DEBUG) XLogWrapper.i(TAG, "UsbControlBlock:constructor")
            mWeakMonitor = WeakReference(monitor)
            mWeakDevice = WeakReference(device)
            connection = monitor.mUsbManager.openDevice(device)
            if (connection == null) {
                XLogWrapper.w(TAG, "openDevice failed in UsbControlBlock11, wait and try again")
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                connection = monitor.mUsbManager.openDevice(device)
            }
            mInfo = updateDeviceInfo(monitor.mUsbManager, device, null)
            val name = device.deviceName
            val v = if (!TextUtils.isEmpty(name)) name.split("/".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray() else null
            var busnum = 0
            var devnum = 0
            if (v != null) {
                busnum = v[v.size - 2].toInt()
                devnum = v[v.size - 1].toInt()
            }
            busNum = busnum
            devNum = devnum
            if (DEBUG) {
                if (connection != null) {
                    val desc = connection!!.fileDescriptor
                    val rawDesc = connection!!.rawDescriptors
                    XLogWrapper.i(
                        TAG,
                        String.format(
                            Locale.US,
                            "name=%s,desc=%d,busnum=%d,devnum=%d,rawDesc=",
                            name,
                            desc,
                            busnum,
                            devnum
                        )
                    )
                } else {
                    XLogWrapper.e(
                        TAG,
                        "could not connect to device(mConnection=null) $name"
                    )
                }
            }
        }

        /**
         * copy constructor
         * @param src
         * @throws IllegalStateException
         */
        constructor(src: UsbControlBlock) {
            val monitor = src.uSBMonitor
            val device = src.device ?: throw IllegalStateException("device may already be removed")
            connection = monitor!!.mUsbManager.openDevice(device)
            if (connection == null) {
                XLogWrapper.w(TAG, "openDevice failed in UsbControlBlock, wait and try again")
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                connection = monitor.mUsbManager.openDevice(device)
                checkNotNull(connection) { "openDevice failed. device may already be removed or have no permission, dev = $device" }
            }
            mInfo = updateDeviceInfo(monitor.mUsbManager, device, null)
            mWeakMonitor = WeakReference(monitor)
            mWeakDevice = WeakReference(device)
            busNum = src.busNum
            devNum = src.devNum
            // FIXME USBMonitor.mCtrlBlocksに追加する(今はHashMapなので追加すると置き換わってしまうのでだめ, ListかHashMapにListをぶら下げる?)
        }

        /**
         * duplicate by clone
         * need permission
         * USBMonitor never handle cloned UsbControlBlock, you should release it after using it.
         * @return
         * @throws CloneNotSupportedException
         */
        @Throws(CloneNotSupportedException::class)
        public override fun clone(): UsbControlBlock {
            val ctrlblock: UsbControlBlock
            ctrlblock = try {
                UsbControlBlock(this)
            } catch (e: IllegalStateException) {
                throw CloneNotSupportedException(e.message)
            }
            return ctrlblock
        }

        val uSBMonitor: USBMonitor?
            get() = mWeakMonitor.get()
        val device: UsbDevice?
            get() = mWeakDevice.get()

        /**
         * get device name
         * @return
         */
        val deviceName: String
            get() {
                val device = mWeakDevice.get()
                return device?.deviceName ?: ""
            }

        /**
         * get device id
         * @return
         */
        val deviceId: Int
            get() {
                val device = mWeakDevice.get()
                return device?.deviceId ?: 0
            }

        /**
         * get device key string
         * @return same value if the devices has same vendor id, product id, device class, device subclass and device protocol
         */
        val deviceKeyName: String
            get() = getDeviceKeyName(mWeakDevice.get())

        /**
         * get device key string
         * @param useNewAPI if true, try to use serial number
         * @return
         * @throws IllegalStateException
         */
        @Throws(IllegalStateException::class)
        fun getDeviceKeyName(useNewAPI: Boolean): String {
            if (useNewAPI) checkConnection()
            return getDeviceKeyName(mWeakDevice.get(), mInfo!!.serial, useNewAPI)
        }

        /**
         * get device key
         * @return
         * @throws IllegalStateException
         */
        @get:Throws(IllegalStateException::class)
        val deviceKey: Int
            get() {
                checkConnection()
                return getDeviceKey(mWeakDevice.get())
            }

        /**
         * get device key
         * @param useNewAPI if true, try to use serial number
         * @return
         * @throws IllegalStateException
         */
        @Throws(IllegalStateException::class)
        fun getDeviceKey(useNewAPI: Boolean): Int {
            if (useNewAPI) checkConnection()
            return getDeviceKey(mWeakDevice.get(), mInfo!!.serial, useNewAPI)
        }

        /**
         * get device key string
         * if device has serial number, use it
         * @return
         */
        val deviceKeyNameWithSerial: String
            get() = getDeviceKeyName(mWeakDevice.get(), mInfo!!.serial, false)

        /**
         * get device key
         * if device has serial number, use it
         * @return
         */
        val deviceKeyWithSerial: Int
            get() = deviceKeyNameWithSerial.hashCode()

        /**
         * get file descriptor to access USB device
         * @return
         * @throws IllegalStateException
         */
        @get:Throws(IllegalStateException::class)
        @get:Synchronized
        val fileDescriptor: Int
            get() {
                checkConnection()
                return connection!!.fileDescriptor
            }

        /**
         * get raw descriptor for the USB device
         * @return
         * @throws IllegalStateException
         */
        @get:Throws(IllegalStateException::class)
        @get:Synchronized
        val rawDescriptors: ByteArray
            get() {
                checkConnection()
                return connection!!.rawDescriptors
            }

        /**
         * get vendor id
         * @return
         */
        val venderId: Int
            get() {
                val device = mWeakDevice.get()
                return device?.vendorId ?: 0
            }

        /**
         * get product id
         * @return
         */
        val productId: Int
            get() {
                val device = mWeakDevice.get()
                return device?.productId ?: 0
            }

        /**
         * get version
         * @return
         */
        val version: String?
            get() = mInfo!!.version

        /**
         * get serial number
         * @return
         */
        val serial: String?
            get() = mInfo!!.serial

        /**
         * get interface
         * @param interface_id
         * @throws IllegalStateException
         */
        @Synchronized
        @Throws(IllegalStateException::class)
        fun getInterface(interface_id: Int): UsbInterface? {
            return getInterface(interface_id, 0)
        }

        /**
         * get interface
         * @param interface_id
         * @param altsetting
         * @return
         * @throws IllegalStateException
         */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Synchronized
        @Throws(IllegalStateException::class)
        fun getInterface(interface_id: Int, altsetting: Int): UsbInterface? {
            checkConnection()
            var intfs = mInterfaces[interface_id]
            if (intfs == null) {
                intfs = SparseArray()
                mInterfaces.put(interface_id, intfs)
            }
            var intf = intfs[altsetting]
            if (intf == null) {
                val device = mWeakDevice.get()
                val n = device!!.interfaceCount
                for (i in 0 until n) {
                    val temp = device.getInterface(i)
                    if (temp.id == interface_id && temp.alternateSetting == altsetting) {
                        intf = temp
                        break
                    }
                }
                if (intf != null) {
                    intfs.append(altsetting, intf)
                }
            }
            return intf
        }

        /**
         * open specific interface
         * @param intf
         */
        @Synchronized
        fun claimInterface(intf: UsbInterface?) {
            claimInterface(intf, true)
        }

        @Synchronized
        fun claimInterface(intf: UsbInterface?, force: Boolean) {
            checkConnection()
            connection!!.claimInterface(intf, force)
        }

        /**
         * close interface
         * @param intf
         * @throws IllegalStateException
         */
        @Synchronized
        @Throws(IllegalStateException::class)
        fun releaseInterface(intf: UsbInterface) {
            checkConnection()
            val intfs = mInterfaces[intf.id]
            if (intfs != null) {
                val index = intfs.indexOfValue(intf)
                intfs.removeAt(index)
                if (intfs.size() == 0) {
                    mInterfaces.remove(intf.id)
                }
            }
            connection!!.releaseInterface(intf)
        }

        /**
         * Close device
         * This also close interfaces if they are opened in Java side
         */
        @Synchronized
        fun close() {
            if (DEBUG) XLogWrapper.i(TAG, "UsbControlBlock#close:")
            if (connection != null) {
                val n = mInterfaces.size()
                for (i in 0 until n) {
                    val intfs = mInterfaces.valueAt(i)
                    if (intfs != null) {
                        val m = intfs.size()
                        for (j in 0 until m) {
                            val intf = intfs.valueAt(j)
                            connection!!.releaseInterface(intf)
                        }
                        intfs.clear()
                    }
                }
                mInterfaces.clear()
                connection!!.close()
                connection = null
                val monitor = mWeakMonitor.get()
                if (monitor != null) {
                    if (monitor.mOnDeviceConnectListener != null) {
                        monitor.mOnDeviceConnectListener.onDisconnect(
                            mWeakDevice.get(),
                            this@UsbControlBlock
                        )
                    }
                    monitor.mCtrlBlocks.remove(device)
                }
            }
        }

        override fun equals(o: Any?): Boolean {
            if (o == null) return false
            if (o is UsbControlBlock) {
                val device = o.device
                return if (device == null) mWeakDevice.get() == null else device == mWeakDevice.get()
            } else if (o is UsbDevice) {
                return o == mWeakDevice.get()
            }
            return super.equals(o)
        }

        //		@Override
        //		protected void finalize() throws Throwable {
        ///			close();
        //			super.finalize();
        //		}
        @Synchronized
        @Throws(IllegalStateException::class)
        private fun checkConnection() {
            checkNotNull(connection) { "already closed" }
        }
    }

    companion object {
        @JvmField
        var DEBUG = true // TODO set false on production
        private const val TAG = "com.lightresearch.probecamera"
        private const val ACTION_USB_PERMISSION_BASE = "com.serenegiant.USB_PERMISSION."
        const val ACTION_USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"

        /**
         * USB機器毎の設定保存用にデバイスキー名を生成する。
         * ベンダーID, プロダクトID, デバイスクラス, デバイスサブクラス, デバイスプロトコルから生成
         * 同種の製品だと同じキー名になるので注意
         * @param device nullなら空文字列を返す
         * @return
         */
        fun getDeviceKeyName(device: UsbDevice?): String {
            return getDeviceKeyName(device, null, false)
        }

        /**
         * USB機器毎の設定保存用にデバイスキー名を生成する。
         * useNewAPI=falseで同種の製品だと同じデバイスキーになるので注意
         * @param device
         * @param useNewAPI
         * @return
         */
        fun getDeviceKeyName(device: UsbDevice?, useNewAPI: Boolean): String {
            return getDeviceKeyName(device, null, useNewAPI)
        }

        /**
         * USB機器毎の設定保存用にデバイスキー名を生成する。この機器名をHashMapのキーにする
         * UsbDeviceがopenしている時のみ有効
         * ベンダーID, プロダクトID, デバイスクラス, デバイスサブクラス, デバイスプロトコルから生成
         * serialがnullや空文字でなければserialを含めたデバイスキー名を生成する
         * useNewAPI=trueでAPIレベルを満たしていればマニュファクチャ名, バージョン, コンフィギュレーションカウントも使う
         * @param device nullなら空文字列を返す
         * @param serial    UsbDeviceConnection#getSerialで取得したシリアル番号を渡す, nullでuseNewAPI=trueでAPI>=21なら内部で取得
         * @param useNewAPI API>=21またはAPI>=23のみで使用可能なメソッドも使用する(ただし機器によってはnullが返ってくるので有効かどうかは機器による)
         * @return
         */
        @SuppressLint("NewApi")
        fun getDeviceKeyName(device: UsbDevice?, serial: String?, useNewAPI: Boolean): String {
            if (device == null) return ""
            val sb = StringBuilder()
            sb.append(device.vendorId)
            sb.append("#") // API >= 12
            sb.append(device.productId)
            sb.append("#") // API >= 12
            sb.append(device.deviceClass)
            sb.append("#") // API >= 12
            sb.append(device.deviceSubclass)
            sb.append("#") // API >= 12
            sb.append(device.deviceProtocol) // API >= 12
            if (!TextUtils.isEmpty(serial)) {
                sb.append("#")
                sb.append(serial)
            }
            if (useNewAPI && BuildCheck.isAndroid5) {
                sb.append("#")
                if (TextUtils.isEmpty(serial)) {
                    try {
                        sb.append(device.serialNumber)
                        sb.append("#")
                    } // API >= 21 & targetSdkVersion has to be <= 28
                    catch (ignore: SecurityException) {
                    }
                }
                sb.append(device.manufacturerName)
                sb.append("#") // API >= 21
                sb.append(device.configurationCount)
                sb.append("#") // API >= 21
                if (BuildCheck.isMarshmallow) {
                    sb.append(device.version)
                    sb.append("#") // API >= 23
                }
            }
            //		if (DEBUG) XLogWrapper.v(TAG, "getDeviceKeyName:" + sb.toString());
            return sb.toString()
        }

        /**
         * デバイスキーを整数として取得
         * getDeviceKeyNameで得られる文字列のhasCodeを取得
         * ベンダーID, プロダクトID, デバイスクラス, デバイスサブクラス, デバイスプロトコルから生成
         * 同種の製品だと同じデバイスキーになるので注意
         * @param device nullなら0を返す
         * @return
         */
        fun getDeviceKey(device: UsbDevice?): Int {
            return if (device != null) getDeviceKeyName(device, null, false).hashCode() else 0
        }

        /**
         * デバイスキーを整数として取得
         * getDeviceKeyNameで得られる文字列のhasCodeを取得
         * useNewAPI=falseで同種の製品だと同じデバイスキーになるので注意
         * @param device
         * @param useNewAPI
         * @return
         */
        fun getDeviceKey(device: UsbDevice?, useNewAPI: Boolean): Int {
            return if (device != null) getDeviceKeyName(device, null, useNewAPI).hashCode() else 0
        }

        /**
         * デバイスキーを整数として取得
         * getDeviceKeyNameで得られる文字列のhasCodeを取得
         * serialがnullでuseNewAPI=falseで同種の製品だと同じデバイスキーになるので注意
         * @param device nullなら0を返す
         * @param serial UsbDeviceConnection#getSerialで取得したシリアル番号を渡す, nullでuseNewAPI=trueでAPI>=21なら内部で取得
         * @param useNewAPI API>=21またはAPI>=23のみで使用可能なメソッドも使用する(ただし機器によってはnullが返ってくるので有効かどうかは機器による)
         * @return
         */
        fun getDeviceKey(device: UsbDevice?, serial: String?, useNewAPI: Boolean): Int {
            return if (device != null) getDeviceKeyName(device, serial, useNewAPI).hashCode() else 0
        }

        private const val USB_DIR_OUT = 0
        private const val USB_DIR_IN = 0x80
        private const val USB_TYPE_MASK = 0x03 shl 5
        private const val USB_TYPE_STANDARD = 0x00 shl 5
        private const val USB_TYPE_CLASS = 0x01 shl 5
        private const val USB_TYPE_VENDOR = 0x02 shl 5
        private const val USB_TYPE_RESERVED = 0x03 shl 5
        private const val USB_RECIP_MASK = 0x1f
        private const val USB_RECIP_DEVICE = 0x00
        private const val USB_RECIP_INTERFACE = 0x01
        private const val USB_RECIP_ENDPOINT = 0x02
        private const val USB_RECIP_OTHER = 0x03
        private const val USB_RECIP_PORT = 0x04
        private const val USB_RECIP_RPIPE = 0x05
        private const val USB_REQ_GET_STATUS = 0x00
        private const val USB_REQ_CLEAR_FEATURE = 0x01
        private const val USB_REQ_SET_FEATURE = 0x03
        private const val USB_REQ_SET_ADDRESS = 0x05
        private const val USB_REQ_GET_DESCRIPTOR = 0x06
        private const val USB_REQ_SET_DESCRIPTOR = 0x07
        private const val USB_REQ_GET_CONFIGURATION = 0x08
        private const val USB_REQ_SET_CONFIGURATION = 0x09
        private const val USB_REQ_GET_INTERFACE = 0x0A
        private const val USB_REQ_SET_INTERFACE = 0x0B
        private const val USB_REQ_SYNCH_FRAME = 0x0C
        private const val USB_REQ_SET_SEL = 0x30
        private const val USB_REQ_SET_ISOCH_DELAY = 0x31
        private const val USB_REQ_SET_ENCRYPTION = 0x0D
        private const val USB_REQ_GET_ENCRYPTION = 0x0E
        private const val USB_REQ_RPIPE_ABORT = 0x0E
        private const val USB_REQ_SET_HANDSHAKE = 0x0F
        private const val USB_REQ_RPIPE_RESET = 0x0F
        private const val USB_REQ_GET_HANDSHAKE = 0x10
        private const val USB_REQ_SET_CONNECTION = 0x11
        private const val USB_REQ_SET_SECURITY_DATA = 0x12
        private const val USB_REQ_GET_SECURITY_DATA = 0x13
        private const val USB_REQ_SET_WUSB_DATA = 0x14
        private const val USB_REQ_LOOPBACK_DATA_WRITE = 0x15
        private const val USB_REQ_LOOPBACK_DATA_READ = 0x16
        private const val USB_REQ_SET_INTERFACE_DS = 0x17
        private const val USB_REQ_STANDARD_DEVICE_SET =
            USB_DIR_OUT or USB_TYPE_STANDARD or USB_RECIP_DEVICE // 0x10
        private const val USB_REQ_STANDARD_DEVICE_GET =
            USB_DIR_IN or USB_TYPE_STANDARD or USB_RECIP_DEVICE // 0x90
        private const val USB_REQ_STANDARD_INTERFACE_SET =
            USB_DIR_OUT or USB_TYPE_STANDARD or USB_RECIP_INTERFACE // 0x11
        private const val USB_REQ_STANDARD_INTERFACE_GET =
            USB_DIR_IN or USB_TYPE_STANDARD or USB_RECIP_INTERFACE // 0x91
        private const val USB_REQ_STANDARD_ENDPOINT_SET =
            USB_DIR_OUT or USB_TYPE_STANDARD or USB_RECIP_ENDPOINT // 0x12
        private const val USB_REQ_STANDARD_ENDPOINT_GET =
            USB_DIR_IN or USB_TYPE_STANDARD or USB_RECIP_ENDPOINT // 0x92
        private const val USB_REQ_CS_DEVICE_SET =
            USB_DIR_OUT or USB_TYPE_CLASS or USB_RECIP_DEVICE // 0x20
        private const val USB_REQ_CS_DEVICE_GET =
            USB_DIR_IN or USB_TYPE_CLASS or USB_RECIP_DEVICE // 0xa0
        private const val USB_REQ_CS_INTERFACE_SET =
            USB_DIR_OUT or USB_TYPE_CLASS or USB_RECIP_INTERFACE // 0x21
        private const val USB_REQ_CS_INTERFACE_GET =
            USB_DIR_IN or USB_TYPE_CLASS or USB_RECIP_INTERFACE // 0xa1
        private const val USB_REQ_CS_ENDPOINT_SET =
            USB_DIR_OUT or USB_TYPE_CLASS or USB_RECIP_ENDPOINT // 0x22
        private const val USB_REQ_CS_ENDPOINT_GET =
            USB_DIR_IN or USB_TYPE_CLASS or USB_RECIP_ENDPOINT // 0xa2
        private const val USB_REQ_VENDER_DEVICE_SET =
            USB_DIR_OUT or USB_TYPE_CLASS or USB_RECIP_DEVICE // 0x40
        private const val USB_REQ_VENDER_DEVICE_GET =
            USB_DIR_IN or USB_TYPE_CLASS or USB_RECIP_DEVICE // 0xc0
        private const val USB_REQ_VENDER_INTERFACE_SET =
            USB_DIR_OUT or USB_TYPE_CLASS or USB_RECIP_INTERFACE // 0x41
        private const val USB_REQ_VENDER_INTERFACE_GET =
            USB_DIR_IN or USB_TYPE_CLASS or USB_RECIP_INTERFACE // 0xc1
        private const val USB_REQ_VENDER_ENDPOINT_SET =
            USB_DIR_OUT or USB_TYPE_CLASS or USB_RECIP_ENDPOINT // 0x42
        private const val USB_REQ_VENDER_ENDPOINT_GET =
            USB_DIR_IN or USB_TYPE_CLASS or USB_RECIP_ENDPOINT // 0xc2
        private const val USB_DT_DEVICE = 0x01
        private const val USB_DT_CONFIG = 0x02
        private const val USB_DT_STRING = 0x03
        private const val USB_DT_INTERFACE = 0x04
        private const val USB_DT_ENDPOINT = 0x05
        private const val USB_DT_DEVICE_QUALIFIER = 0x06
        private const val USB_DT_OTHER_SPEED_CONFIG = 0x07
        private const val USB_DT_INTERFACE_POWER = 0x08
        private const val USB_DT_OTG = 0x09
        private const val USB_DT_DEBUG = 0x0a
        private const val USB_DT_INTERFACE_ASSOCIATION = 0x0b
        private const val USB_DT_SECURITY = 0x0c
        private const val USB_DT_KEY = 0x0d
        private const val USB_DT_ENCRYPTION_TYPE = 0x0e
        private const val USB_DT_BOS = 0x0f
        private const val USB_DT_DEVICE_CAPABILITY = 0x10
        private const val USB_DT_WIRELESS_ENDPOINT_COMP = 0x11
        private const val USB_DT_WIRE_ADAPTER = 0x21
        private const val USB_DT_RPIPE = 0x22
        private const val USB_DT_CS_RADIO_CONTROL = 0x23
        private const val USB_DT_PIPE_USAGE = 0x24
        private const val USB_DT_SS_ENDPOINT_COMP = 0x30
        private const val USB_DT_CS_DEVICE = USB_TYPE_CLASS or USB_DT_DEVICE
        private const val USB_DT_CS_CONFIG = USB_TYPE_CLASS or USB_DT_CONFIG
        private const val USB_DT_CS_STRING = USB_TYPE_CLASS or USB_DT_STRING
        private const val USB_DT_CS_INTERFACE = USB_TYPE_CLASS or USB_DT_INTERFACE
        private const val USB_DT_CS_ENDPOINT = USB_TYPE_CLASS or USB_DT_ENDPOINT
        private const val USB_DT_DEVICE_SIZE = 18

        /**
         * 指定したIDのStringディスクリプタから文字列を取得する。取得できなければnull
         * @param connection
         * @param id
         * @param languageCount
         * @param languages
         * @return
         */
        private fun getString(
            connection: UsbDeviceConnection,
            id: Int,
            languageCount: Int,
            languages: ByteArray
        ): String? {
            val work = ByteArray(256)
            var result: String? = null
            for (i in 1..languageCount) {
                val ret = connection.controlTransfer(
                    USB_REQ_STANDARD_DEVICE_GET,  // USB_DIR_IN | USB_TYPE_STANDARD | USB_RECIP_DEVICE
                    USB_REQ_GET_DESCRIPTOR,
                    USB_DT_STRING shl 8 or id, languages[i].toInt(), work, 256, 0
                )
                if (ret > 2 && work[0].toInt() == ret && work[1].toInt() == USB_DT_STRING) {
                    // skip first two bytes(bLength & bDescriptorType), and copy the rest to the string
                    try {
                        result = String(work, 2, ret - 2, Charset.forName("UTF-16LE"))
                        result = if ("Љ" != result) {    // 変なゴミが返ってくる時がある
                            break
                        } else {
                            null
                        }
                    } catch (e: UnsupportedEncodingException) {
                        // ignore
                    }
                }
            }
            return result
        }

        /**
         * ベンダー名・製品名・バージョン・シリアルを取得する
         * #updateDeviceInfo(final UsbManager, final UsbDevice, final UsbDeviceInfo)のヘルパーメソッド
         * @param context
         * @param device
         * @return
         */
        fun getDeviceInfo(context: Context, device: UsbDevice?): UsbDeviceInfo? {
            return updateDeviceInfo(
                context.getSystemService(Context.USB_SERVICE) as UsbManager,
                device,
                UsbDeviceInfo()
            )
        }

        /**
         * ベンダー名・製品名・バージョン・シリアルを取得する
         * @param manager
         * @param device
         * @param _info
         * @return
         */
        @TargetApi(Build.VERSION_CODES.M)
        fun updateDeviceInfo(
            manager: UsbManager?,
            device: UsbDevice?,
            _info: UsbDeviceInfo?
        ): UsbDeviceInfo? {
            val info = _info ?: UsbDeviceInfo()
            info.clear()
            if (device != null) {
                if (BuildCheck.isLollipop) {
                    info.manufacture = device.manufacturerName
                    info.productName = device.productName
                    info.serial = device.serialNumber
                }
                if (BuildCheck.isMarshmallow) {
                    info.usbVersion = device.version
                }
                if (manager != null && manager.hasPermission(device)) {
                    val connection = manager.openDevice(device) ?: return null
                    val desc = connection.rawDescriptors
                    if (TextUtils.isEmpty(info.usbVersion)) {
                        info.usbVersion = String.format(
                            "%x.%02x",
                            desc[3].toInt() and 0xff, desc[2].toInt() and 0xff
                        )
                    }
                    if (TextUtils.isEmpty(info.version)) {
                        info.version = String.format(
                            "%x.%02x", desc[13].toInt() and 0xff,
                            desc[12].toInt() and 0xff
                        )
                    }
                    if (TextUtils.isEmpty(info.serial)) {
                        info.serial = connection.serial
                    }
                    val languages = ByteArray(256)
                    var languageCount = 0
                    // controlTransfer(int requestType, int request, int value, int index, byte[] buffer, int length, int timeout)
                    try {
                        val result = connection.controlTransfer(
                            USB_REQ_STANDARD_DEVICE_GET,  // USB_DIR_IN | USB_TYPE_STANDARD | USB_RECIP_DEVICE
                            USB_REQ_GET_DESCRIPTOR,
                            USB_DT_STRING shl 8 or 0, 0, languages, 256, 0
                        )
                        if (result > 0) {
                            languageCount = (result - 2) / 2
                        }
                        if (languageCount > 0) {
                            if (TextUtils.isEmpty(info.manufacture)) {
                                info.manufacture = getString(
                                    connection,
                                    desc[14].toInt(),
                                    languageCount,
                                    languages
                                )
                            }
                            if (TextUtils.isEmpty(info.productName)) {
                                info.productName = getString(
                                    connection,
                                    desc[15].toInt(),
                                    languageCount,
                                    languages
                                )
                            }
                            if (TextUtils.isEmpty(info.serial)) {
                                info.serial = getString(
                                    connection,
                                    desc[16].toInt(),
                                    languageCount,
                                    languages
                                )
                            }
                        }
                    } finally {
                        connection.close()
                    }
                }
                if (TextUtils.isEmpty(info.manufacture)) {
                    info.manufacture = USBVendorId.vendorName(device.vendorId)
                }
                if (TextUtils.isEmpty(info.manufacture)) {
                    info.manufacture = String.format("%04x", device.vendorId)
                }
                if (TextUtils.isEmpty(info.productName)) {
                    info.productName = String.format("%04x", device.productId)
                }
            }
            return info
        }
    }
}