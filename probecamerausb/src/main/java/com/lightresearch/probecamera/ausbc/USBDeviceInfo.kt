package com.lightresearch.probecamera.ausbc

class UsbDeviceInfo {
    var usbVersion: String? = null
    var manufacture: String? = null
    var productName: String? = null
    var version: String? = null
    var serial: String? = null
    fun clear() {
        serial = null
        version = null
        productName = null
        manufacture = null
        usbVersion = null
    }

    override fun toString(): String {
        return String.format(
            "UsbDevice:usb_version=%s,manufacturer=%s,product=%s,version=%s,serial=%s",
            if (usbVersion != null) usbVersion else "",
            if (manufacture != null) manufacture else "",
            if (productName != null) productName else "",
            if (version != null) version else "",
            if (serial != null) serial else ""
        )
    }
}