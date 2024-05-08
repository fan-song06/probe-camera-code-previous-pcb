package com.lightresearch.probecamera

import android.content.Context
import android.hardware.usb.UsbDevice
import android.view.Surface
import android.widget.Toast

class CameraCallbackListener(private val context: Context) {
        /**
         * On attach dev
         *
         * @param device usb device
         */
        fun onAttachDev(device: UsbDevice?) {
            Toast.makeText(context, "Device connected", Toast.LENGTH_SHORT).show()
        }

        /**
         * On detach dev
         *
         * @param device usb device
         */
        fun onDetachDec(device: UsbDevice?) {
//            Toast.makeText(context, "Device connected", Toast.LENGTH_SHORT).show()
        }

        /**
         * On connect dev
         *
         * @param device usb device
         */
        fun onConnectDev(device: UsbDevice?, ctrlBlock: Any? = null) {
//            Toast.makeText(context, "Device connected", Toast.LENGTH_SHORT).show()
        }

        /**
         * On dis connect dev
         *
         * @param device usb device
         */
        fun onDisConnectDec(device: UsbDevice?, ctrlBlock: Any? = null) {
            Toast.makeText(context, "Device disconnected", Toast.LENGTH_SHORT).show()
        }

        /**
         * On cancel dev
         *
         * @param device usb device
         */
        fun onCancelDev(device: UsbDevice?) {
//            Toast.makeText(context, "Device connected", Toast.LENGTH_SHORT).show()
        }

}