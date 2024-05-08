package com.lightresearch.probecamera.ausbc.callback

import android.hardware.usb.UsbDevice
import com.lightresearch.probecamera.ausbc.USBMonitor

interface IDeviceConnectCallBack {
    /**
     * On attach dev
     *
     * @param device usb device
     */
    fun onAttachDev(device: UsbDevice?)

    /**
     * On detach dev
     *
     * @param device usb device
     */
    fun onDetachDec(device: UsbDevice?)

    /**
     * On connect dev
     *
     * @param device usb device
     */
    fun onConnectDev(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock? = null)

    /**
     * On dis connect dev
     *
     * @param device usb device
     */
    fun onDisConnectDec(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock? = null)

    /**
     * On cancel dev
     *
     * @param device usb device
     */
    fun onCancelDev(device: UsbDevice?)
}