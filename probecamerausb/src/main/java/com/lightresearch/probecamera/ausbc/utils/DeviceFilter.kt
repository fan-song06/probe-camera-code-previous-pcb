package com.lightresearch.probecamera.ausbc.utils

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.hardware.usb.UsbDevice
import android.text.TextUtils
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.Collections


class DeviceFilter {
    // USB Vendor ID (or -1 for unspecified)
    val mVendorId: Int

    // USB Product ID (or -1 for unspecified)
    val mProductId: Int

    // USB device or interface class (or -1 for unspecified)
    val mClass: Int

    // USB device subclass (or -1 for unspecified)
    val mSubclass: Int

    // USB device protocol (or -1 for unspecified)
    val mProtocol: Int

    // USB device manufacturer name string (or null for unspecified)
    val mManufacturerName: String?

    // USB device product name string (or null for unspecified)
    val mProductName: String?

    // USB device serial number string (or null for unspecified)
    val mSerialNumber: String?

    // set true if specific device(s) should exclude
    val isExclude: Boolean

    @JvmOverloads
    constructor(
        vid: Int,
        pid: Int,
        clasz: Int,
        subclass: Int,
        protocol: Int,
        manufacturer: String?,
        product: String?,
        serialNum: String?,
        isExclude: Boolean = false
    ) {
        mVendorId = vid
        mProductId = pid
        mClass = clasz
        mSubclass = subclass
        mProtocol = protocol
        mManufacturerName = manufacturer
        mProductName = product
        mSerialNumber = serialNum
        this.isExclude = isExclude/*		Log.i(TAG, String.format("vendorId=0x%04x,productId=0x%04x,class=0x%02x,subclass=0x%02x,protocol=0x%02x",
			mVendorId, mProductId, mClass, mSubclass, mProtocol)); */
    }

    @JvmOverloads
    constructor(device: UsbDevice, isExclude: Boolean = false) {
        mVendorId = device.vendorId
        mProductId = device.productId
        mClass = device.deviceClass
        mSubclass = device.deviceSubclass
        mProtocol = device.deviceProtocol
        mManufacturerName = null // device.getManufacturerName();
        mProductName = null // device.getProductName();
        mSerialNumber = null // device.getSerialNumber();
        this.isExclude = isExclude/*		Log.i(TAG, String.format("vendorId=0x%04x,productId=0x%04x,class=0x%02x,subclass=0x%02x,protocol=0x%02x",
			mVendorId, mProductId, mClass, mSubclass, mProtocol)); */
    }/*	public void write(XmlSerializer serializer) throws IOException {
		serializer.startTag(null, "usb-device");
		if (mVendorId != -1) {
			serializer
					.attribute(null, "vendor-id", Integer.toString(mVendorId));
		}
		if (mProductId != -1) {
			serializer.attribute(null, "product-id",
					Integer.toString(mProductId));
		}
		if (mClass != -1) {
			serializer.attribute(null, "class", Integer.toString(mClass));
		}
		if (mSubclass != -1) {
			serializer.attribute(null, "subclass", Integer.toString(mSubclass));
		}
		if (mProtocol != -1) {
			serializer.attribute(null, "protocol", Integer.toString(mProtocol));
		}
		if (mManufacturerName != null) {
			serializer.attribute(null, "manufacturer-name", mManufacturerName);
		}
		if (mProductName != null) {
			serializer.attribute(null, "product-name", mProductName);
		}
		if (mSerialNumber != null) {
			serializer.attribute(null, "serial-number", mSerialNumber);
		}
		serializer.attribute(null, "serial-number", Boolean.toString(isExclude));
		serializer.endTag(null, "usb-device");
	} */

    /**
     * 指定したクラス・サブクラス・プロトコルがこのDeviceFilterとマッチするかどうかを返す
     * mExcludeフラグは別途#isExcludeか自前でチェックすること
     * @param clasz
     * @param subclass
     * @param protocol
     * @return
     */
    private fun matches(clasz: Int, subclass: Int, protocol: Int): Boolean {
        return ((mClass == -1 || clasz == mClass) && (mSubclass == -1 || subclass == mSubclass) && (mProtocol == -1 || protocol == mProtocol))
    }

    /**
     * 指定したUsbDeviceがこのDeviceFilterにマッチするかどうかを返す
     * mExcludeフラグは別途#isExcludeか自前でチェックすること
     * @param device
     * @return
     */
    fun matches(device: UsbDevice): Boolean {
        if (mVendorId != -1 && device.vendorId != mVendorId) {
            return false
        }
        if (mProductId != -1 && device.productId != mProductId) {
            return false
        }/*		if (mManufacturerName != null && device.getManufacturerName() == null)
			return false;
		if (mProductName != null && device.getProductName() == null)
			return false;
		if (mSerialNumber != null && device.getSerialNumber() == null)
			return false;
		if (mManufacturerName != null && device.getManufacturerName() != null
				&& !mManufacturerName.equals(device.getManufacturerName()))
			return false;
		if (mProductName != null && device.getProductName() != null
				&& !mProductName.equals(device.getProductName()))
			return false;
		if (mSerialNumber != null && device.getSerialNumber() != null
				&& !mSerialNumber.equals(device.getSerialNumber()))
			return false; */

        // check device class/subclass/protocol
        if (matches(device.deviceClass, device.deviceSubclass, device.deviceProtocol)) {
            return true
        }

        // if device doesn't match, check the interfaces
        val count = device.interfaceCount
        for (i in 0 until count) {
            val intf = device.getInterface(i)
            if (matches(intf.interfaceClass, intf.interfaceSubclass, intf.interfaceProtocol)) {
                return true
            }
        }
        return false
    }

    /**
     * このDeviceFilterに一致してかつmExcludeがtrueならtrueを返す
     * @param device
     * @return
     */
    fun isExclude(device: UsbDevice): Boolean {
        return isExclude && matches(device)
    }

    /**
     * これって要らんかも, equalsでできる気が
     * @param f
     * @return
     */
    fun matches(f: DeviceFilter): Boolean {
        if (isExclude != f.isExclude) {
            return false
        }
        if (mVendorId != -1 && f.mVendorId != mVendorId) {
            return false
        }
        if (mProductId != -1 && f.mProductId != mProductId) {
            return false
        }
        if (f.mManufacturerName != null && mManufacturerName == null) {
            return false
        }
        if (f.mProductName != null && mProductName == null) {
            return false
        }
        if (f.mSerialNumber != null && mSerialNumber == null) {
            return false
        }
        if (mManufacturerName != null && (f.mManufacturerName != null) && mManufacturerName != f.mManufacturerName) {
            return false
        }
        if (mProductName != null && f.mProductName != null && mProductName != f.mProductName) {
            return false
        }
        return if (mSerialNumber != null && f.mSerialNumber != null && mSerialNumber != f.mSerialNumber) {
            false
        } else matches(f.mClass, f.mSubclass, f.mProtocol)

        // check device class/subclass/protocol
    }

    override fun equals(obj: Any?): Boolean {
        // can't compare if we have wildcard strings
        if ((mVendorId == -1) || (mProductId == -1) || mClass == -1 || mSubclass == -1 || mProtocol == -1) {
            return false
        }
        if (obj is DeviceFilter) {
            val filter = obj
            if (filter.mVendorId != mVendorId || filter.mProductId != mProductId || filter.mClass != mClass || filter.mSubclass != mSubclass || filter.mProtocol != mProtocol) {
                return false
            }
            if (filter.mManufacturerName != null && mManufacturerName == null || filter.mManufacturerName == null && mManufacturerName != null || filter.mProductName != null && mProductName == null || filter.mProductName == null && mProductName != null || filter.mSerialNumber != null && mSerialNumber == null || filter.mSerialNumber == null && mSerialNumber != null) {
                return false
            }
            return if (((filter.mManufacturerName != null && mManufacturerName == null || filter.mManufacturerName == null && mManufacturerName != null || filter.mProductName != null && mProductName == null || (filter.mProductName == null) && (mProductName != null) || filter.mSerialNumber != null) && mSerialNumber == null || filter.mSerialNumber == null) && mSerialNumber != null) false
            else filter.isExclude != isExclude
        }
        if (obj is UsbDevice) {
            val device = obj
            return if (isExclude || device.vendorId != mVendorId || device.productId != mProductId || device.deviceClass != mClass || device.deviceSubclass != mSubclass || device.deviceProtocol != mProtocol) {
                false
            } else true/*			if ((mManufacturerName != null && device.getManufacturerName() == null)
					|| (mManufacturerName == null && device
							.getManufacturerName() != null)
					|| (mProductName != null && device.getProductName() == null)
					|| (mProductName == null && device.getProductName() != null)
					|| (mSerialNumber != null && device.getSerialNumber() == null)
					|| (mSerialNumber == null && device.getSerialNumber() != null)) {
				return (false);
			} *//*			if ((device.getManufacturerName() != null && !mManufacturerName
					.equals(device.getManufacturerName()))
					|| (device.getProductName() != null && !mProductName
							.equals(device.getProductName()))
					|| (device.getSerialNumber() != null && !mSerialNumber
							.equals(device.getSerialNumber()))) {
				return (false);
			} */
        }
        return false
    }

    override fun hashCode(): Int {
        return mVendorId shl 16 or mProductId xor (mClass shl 16 or (mSubclass shl 8) or mProtocol)
    }

    override fun toString(): String {
        return ("DeviceFilter[mVendorId=" + mVendorId + ",mProductId=" + mProductId + ",mClass=" + mClass + ",mSubclass=" + mSubclass + ",mProtocol=" + mProtocol + ",mManufacturerName=" + mManufacturerName + ",mProductName=" + mProductName + ",mSerialNumber=" + mSerialNumber + ",isExclude=" + isExclude + "]")
    }

    companion object {
        private const val TAG = "DeviceFilter"

        /**
         * 指定したxmlリソースからDeviceFilterリストを生成する
         * @param context
         * @param deviceFilterXmlId
         * @return
         */
        fun getDeviceFilters(context: Context, deviceFilterXmlId: Int): List<DeviceFilter> {
            val parser: XmlPullParser = context.resources.getXml(deviceFilterXmlId)
            val deviceFilters: MutableList<DeviceFilter> = ArrayList()
            try {
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        val deviceFilter = readEntryOne(context, parser)
                        if (deviceFilter != null) {
                            deviceFilters.add(deviceFilter)
                        }
                    }
                    eventType = parser.next()
                }
            } catch (e: XmlPullParserException) {
                Log.d(TAG, "XmlPullParserException", e)
            } catch (e: IOException) {
                Log.d(TAG, "IOException", e)
            }
            return Collections.unmodifiableList(deviceFilters)
        }

        /**
         * read as integer values with default value from xml(w/o exception throws)
         * resource integer id is also resolved into integer
         * @param parser
         * @param namespace
         * @param name
         * @param defaultValue
         * @return
         */
        private fun getAttributeInteger(
            context: Context,
            parser: XmlPullParser,
            namespace: String?,
            name: String,
            defaultValue: Int
        ): Int {
            var result = defaultValue
            try {
                var v = parser.getAttributeValue(namespace, name)
                if (!TextUtils.isEmpty(v) && v!!.startsWith("@")) {
                    val r = v.substring(1)
                    val resId = context.resources.getIdentifier(r, null, context.packageName)
                    if (resId > 0) {
                        result = context.resources.getInteger(resId)
                    }
                } else {
                    var radix = 10
                    if (v != null && v.length > 2 && v[0] == '0' && (v[1] == 'x' || v[1] == 'X')) {
                        // allow hex values starting with 0x or 0X
                        radix = 16
                        v = v.substring(2)
                    }
                    result = v!!.toInt(radix)
                }
            } catch (e: NotFoundException) {
                result = defaultValue
            } catch (e: NumberFormatException) {
                result = defaultValue
            } catch (e: NullPointerException) {
                result = defaultValue
            }
            return result
        }

        /**
         * read as boolean values with default value from xml(w/o exception throws)
         * resource boolean id is also resolved into boolean
         * if the value is zero, return false, if the value is non-zero integer, return true
         * @param context
         * @param parser
         * @param namespace
         * @param name
         * @param defaultValue
         * @return
         */
        private fun getAttributeBoolean(
            context: Context,
            parser: XmlPullParser,
            namespace: String?,
            name: String,
            defaultValue: Boolean
        ): Boolean {
            var result = defaultValue
            try {
                var v = parser.getAttributeValue(namespace, name)
                if ("TRUE".equals(v, ignoreCase = true)) {
                    result = true
                } else if ("FALSE".equals(v, ignoreCase = true)) {
                    result = false
                } else if (!TextUtils.isEmpty(v) && v!!.startsWith("@")) {
                    val r = v.substring(1)
                    val resId = context.resources.getIdentifier(r, null, context.packageName)
                    if (resId > 0) {
                        result = context.resources.getBoolean(resId)
                    }
                } else {
                    var radix = 10
                    if (v != null && v.length > 2 && v[0] == '0' && (v[1] == 'x' || v[1] == 'X')) {
                        // allow hex values starting with 0x or 0X
                        radix = 16
                        v = v.substring(2)
                    }
                    val `val` = v!!.toInt(radix)
                    result = `val` != 0
                }
            } catch (e: NotFoundException) {
                result = defaultValue
            } catch (e: NumberFormatException) {
                result = defaultValue
            } catch (e: NullPointerException) {
                result = defaultValue
            }
            return result
        }

        /**
         * read as String attribute with default value from xml(w/o exception throws)
         * resource string id is also resolved into string
         * @param parser
         * @param namespace
         * @param name
         * @param defaultValue
         * @return
         */
        private fun getAttributeString(
            context: Context,
            parser: XmlPullParser,
            namespace: String?,
            name: String,
            defaultValue: String?
        ): String? {
            var result = defaultValue
            try {
                result = parser.getAttributeValue(namespace, name)
                if (result == null) result = defaultValue
                if (!TextUtils.isEmpty(result) && result!!.startsWith("@")) {
                    val r = result.substring(1)
                    val resId = context.resources.getIdentifier(r, null, context.packageName)
                    if (resId > 0) result = context.resources.getString(resId)
                }
            } catch (e: NotFoundException) {
                result = defaultValue
            } catch (e: NumberFormatException) {
                result = defaultValue
            } catch (e: NullPointerException) {
                result = defaultValue
            }
            return result
        }

        @Throws(XmlPullParserException::class, IOException::class)
        fun readEntryOne(context: Context, parser: XmlPullParser): DeviceFilter? {
            var vendorId = -1
            var productId = -1
            var deviceClass = -1
            var deviceSubclass = -1
            var deviceProtocol = -1
            var exclude = false
            var manufacturerName: String? = null
            var productName: String? = null
            var serialNumber: String? = null
            var hasValue = false
            var tag: String
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                tag = parser.name
                if (!TextUtils.isEmpty(tag) && tag.equals("usb-device", ignoreCase = true)) {
                    if (eventType == XmlPullParser.START_TAG) {
                        hasValue = true
                        vendorId = getAttributeInteger(context, parser, null, "vendor-id", -1)
                        if (vendorId == -1) {
                            vendorId = getAttributeInteger(context, parser, null, "vendorId", -1)
                            if (vendorId == -1) vendorId =
                                getAttributeInteger(context, parser, null, "venderId", -1)
                        }
                        productId = getAttributeInteger(context, parser, null, "product-id", -1)
                        if (productId == -1) productId =
                            getAttributeInteger(context, parser, null, "productId", -1)
                        deviceClass = getAttributeInteger(context, parser, null, "class", -1)
                        deviceSubclass = getAttributeInteger(context, parser, null, "subclass", -1)
                        deviceProtocol = getAttributeInteger(context, parser, null, "protocol", -1)
                        manufacturerName =
                            getAttributeString(context, parser, null, "manufacturer-name", null)
                        if (TextUtils.isEmpty(manufacturerName)) manufacturerName =
                            getAttributeString(context, parser, null, "manufacture", null)
                        productName =
                            getAttributeString(context, parser, null, "product-name", null)
                        if (TextUtils.isEmpty(productName)) productName =
                            getAttributeString(context, parser, null, "product", null)
                        serialNumber =
                            getAttributeString(context, parser, null, "serial-number", null)
                        if (TextUtils.isEmpty(serialNumber)) serialNumber =
                            getAttributeString(context, parser, null, "serial", null)
                        exclude = getAttributeBoolean(context, parser, null, "exclude", false)
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if (hasValue) {
                            return DeviceFilter(
                                vendorId,
                                productId,
                                deviceClass,
                                deviceSubclass,
                                deviceProtocol,
                                manufacturerName,
                                productName,
                                serialNumber,
                                exclude
                            )
                        }
                    }
                }
                eventType = parser.next()
            }
            return null
        }
    }
}
