package com.lightresearch.probecamera.ausbc

import android.app.Application
import android.content.ContextWrapper
import com.lightresearch.probecamera.ProbeCameraFragment
import ioio.lib.api.DigitalInput
import ioio.lib.api.DigitalOutput
import ioio.lib.api.IOIO
import ioio.lib.api.TwiMaster
import ioio.lib.api.exception.ConnectionLostException
import ioio.lib.util.BaseIOIOLooper
import ioio.lib.util.IOIOLooper
import ioio.lib.util.IOIOLooperProvider
import ioio.lib.util.android.IOIOAndroidApplicationHelper


class GlobalIOIO(val fragment: ProbeCameraFragment, contextWrapper: ContextWrapper) : Application(), IOIOLooperProvider {
    private val helper_ = IOIOAndroidApplicationHelper(contextWrapper, this)
    val led_1: DigitalOutput? = null
    private var flag1 = false
    private var flag2 = false
    private var flag3 = false
    private var flag4 = false
    private var flag5 = false
    private var flag6 = false
    private var flag7 = false
    private var flag8 = false
    private var flag9 = false
    private var flag10 = false
    private var flag11 = false
    private var flag12 = false
    private var flagx = false
    var volts = false
    var status = true
    private var mode = 0
    fun create() {
        helper_.create()
    }

    fun destroy() {
        helper_.destroy()
    }

    fun start() {
        helper_.start()
    }

    fun stop() {
        helper_.stop()
    }

    fun restart() {
        helper_.restart()
    }


    protected fun led_1(flag11: Boolean, mode11: Int) {
        mode = mode11
        flag1 = flag11
    }

    protected fun led_2(flag22: Boolean, mode22: Int) {
        mode = mode22
        flag2 = flag22
    }

    protected fun led_3(flag33: Boolean, mode33: Int) {
        mode = mode33
        flag3 = flag33
    }

    protected fun led_4(flag44: Boolean, mode44: Int) {
        mode = mode44
        flag4 = flag44
    }

    protected fun led_5(flag55: Boolean, mode55: Int) {
        mode = mode55
        flag5 = flag55
    }

    fun led_6(flag66: Boolean, mode66: Int) {
        mode = mode66
        flag6 = flag66
    }

    fun led_7(flag77: Boolean, mode77: Int) {
        mode = mode77
        flag7 = flag77
    }

    protected fun led_8(flag88: Boolean, mode88: Int) {
        mode = mode88
        flag8 = flag88
    }

    protected fun led_9(flag99: Boolean, mode99: Int) {
        mode = mode99
        flag9 = flag99
    }

    protected fun led_10(flag1010: Boolean, mode1010: Int) {
        mode = mode1010
        flag10 = flag1010
    }

    protected fun led_11(flag1111: Boolean, mode1111: Int) {
        mode = mode1111
        flag11 = flag1111
    }

    protected fun led_12(flag1212: Boolean, mode1212: Int) {
        mode = mode1212
        flag12 = flag1212
    }

    protected fun led_x(flagxx: Boolean) {
        flagx = flagxx
    }

    inner class Looper : BaseIOIOLooper() {
        lateinit var led_1: DigitalOutput
        lateinit var led_2: DigitalOutput
        lateinit var led_3: DigitalOutput
        lateinit var led_4: DigitalOutput
        lateinit var led_5: DigitalOutput
        lateinit var led_6: DigitalOutput
        lateinit var led_7: DigitalOutput
        lateinit var led_8: DigitalOutput
        lateinit var led_9: DigitalOutput
        lateinit var led_10: DigitalOutput
        lateinit var led_11: DigitalOutput
        lateinit var led_12: DigitalOutput
        lateinit var led_x: DigitalOutput
        lateinit var `in`: DigitalInput
        lateinit var enable_: DigitalOutput
        lateinit private var twi: TwiMaster
        private val address = 47



        @Throws(ConnectionLostException::class, InterruptedException::class)
        override fun setup() {
            super.setup()

            led_1 = ioio_.openDigitalOutput(14, DigitalOutput.Spec.Mode.OPEN_DRAIN, false) // fl? 12
            led_2 = ioio_.openDigitalOutput(1, DigitalOutput.Spec.Mode.OPEN_DRAIN, false) // wl? 13
            led_3 = ioio_.openDigitalOutput(2, DigitalOutput.Spec.Mode.OPEN_DRAIN, false)
            led_4 = ioio_.openDigitalOutput(3, DigitalOutput.Spec.Mode.OPEN_DRAIN, false)
            led_5 = ioio_.openDigitalOutput(4, DigitalOutput.Spec.Mode.OPEN_DRAIN, false)
            led_6 = ioio_.openDigitalOutput(6, DigitalOutput.Spec.Mode.OPEN_DRAIN, false)
            led_7 = ioio_.openDigitalOutput(5, DigitalOutput.Spec.Mode.OPEN_DRAIN, false)
            led_8 = ioio_.openDigitalOutput(7, DigitalOutput.Spec.Mode.OPEN_DRAIN, false)

            //
            led_9 = ioio_.openDigitalOutput(10, DigitalOutput.Spec.Mode.OPEN_DRAIN, false)
            led_10 = ioio_.openDigitalOutput(11, DigitalOutput.Spec.Mode.OPEN_DRAIN, false)
            led_11 = ioio_.openDigitalOutput(12, DigitalOutput.Spec.Mode.OPEN_DRAIN, false)
            led_12 = ioio_.openDigitalOutput(13, DigitalOutput.Spec.Mode.OPEN_DRAIN, false)
            led_x = ioio_.openDigitalOutput(IOIO.LED_PIN, true)
            enable_ = ioio_.openDigitalOutput(18, true)
            twi = ioio_.openTwiMaster(2, TwiMaster.Rate.RATE_100KHz, false)
            `in` = ioio_.openDigitalInput(16, DigitalInput.Spec.Mode.PULL_UP)

            //setup DigitalOutputs, AnalogInputs etc here.
        }

        @Throws(ConnectionLostException::class, InterruptedException::class)
        override fun loop() {
            super.loop()
            update(`in`.read())
            led_1.write(flag1)
            led_2.write(flag2)
            led_3.write(flag3)
            led_4.write(flag4)
            led_5.write(flag5)
            led_6.write(flag6)
            led_7.write(flag7)
            led_8.write(flag8)
            led_9.write(flag9)
            led_10.write(flag10)
            led_11.write(flag11)
            led_12.write(flag12)
            led_x.write(flagx)
            enable_.write(true)
            if (mode > 0) {
                val request1 = byteArrayOf(0x00.toByte(), 0xff.toByte()) // DMC
                val response1 = ByteArray(1)
                val request2 = byteArrayOf(0x80.toByte(), 0xff.toByte()) // DMC
                val response2 = ByteArray(1)
                val byte_intensity = (235 and 0xFF).toByte()
                if (mode == 10) {
                    request1[1] = 0x00.toByte()
                    request2[1] = 0x00.toByte()
                }
                if (mode == 1) {
                    request1[1] = 0x15.toByte() //10
                    request2[1] = 0x15.toByte() //10
                }
                if (mode == 2) {
                    request1[1] = 0x03.toByte() //03
                    request2[1] = 0x03.toByte() //03
                }
                //fl
                if (mode == 3) {
                    request1[1] = byte_intensity //60
                    request2[1] = byte_intensity //60
                }
                if (mode == 5) {
                    request1[1] = 0x03.toByte() //02
                    request2[1] = 0x03.toByte() //02
                }
                if (mode == 6) {
                    request1[1] = 0x10.toByte() //02
                    request2[1] = 0x10.toByte() //02
                }
                if (mode == 7) {
                    request1[1] = 0x30.toByte() //10
                    request2[1] = 0x30.toByte() //10
                }
                //new
                if (mode == 20) {
                    request1[1] = 0x30.toByte() //60
                    request2[1] = 0x30.toByte() //60
                }
                if (mode == 30) {
                    request1[1] = 0xB0.toByte() //02
                    request2[1] = 0xB0.toByte() //02
                }
                if (mode == 50) {
                    request1[1] = 0x02.toByte() //02
                    request2[1] = 0x02.toByte() //02
                }
                if (mode == 70) {
                    request1[1] = 0x66.toByte() //02
                    request2[1] = 0x66.toByte() //02
                }
                twi.writeReadAsync(address, false, request1, request1.size, response1, 0) // DMC
                //twi.writeReadAsync(address, false, request2, request2.length, response2, 0);// DMC
                mode = 0
            }
            if (`in`.read() == false) {
                status = false
            }
            if (`in`.read() == true && status == false) {
                start_capture()
                status = true
            }
        }
    }

    override fun createIOIOLooper(connectionType: String?, extra: Any?): IOIOLooper {
        return Looper()
    }

    private fun start_capture() {
        fragment.captureImageWL();
        //VideoActivity.start_capture();
    }

    private fun update(f: Boolean) {
        volts = f
    }
}

