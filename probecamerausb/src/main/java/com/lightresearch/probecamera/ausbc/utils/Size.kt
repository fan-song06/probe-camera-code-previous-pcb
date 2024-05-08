package com.lightresearch.probecamera.ausbc.utils

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.util.Locale


class Size : Parcelable {
    //
    /**
     * native側のuvc_raw_format_tの値, こっちは主にlibuvc用
     * 9999 is still image
     */
    var type: Int

    /**
     * native側のraw_frame_tの値, androusb用,
     * libuvcは対応していない
     */
    var frame_type: Int
    var index: Int
    var width: Int
    var height: Int
    var frameIntervalType = 0
    var frameIntervalIndex: Int
    var intervals: IntArray?

    // ここ以下はframeIntervalTypeとintervalsから#updateFrameRateで計算する
    var fps: FloatArray? = null
    private var frameRates: String? = null

    /**
     * コンストラクタ
     * @param _type native側のraw_format_tの値, ただし9999は静止画
     * @param _frame_type native側のraw_frame_tの値
     * @param _index
     * @param _width
     * @param _height
     */
    constructor(_type: Int, _frame_type: Int, _index: Int, _width: Int, _height: Int) {
        type = _type
        frame_type = _frame_type
        index = _index
        width = _width
        height = _height
        frameIntervalType = -1
        frameIntervalIndex = 0
        intervals = null
        updateFrameRate()
    }

    /**
     * コンストラクタ
     * @param _type native側のraw_format_tの値, ただし9999は静止画
     * @param _frame_type native側のraw_frame_tの値
     * @param _index
     * @param _width
     * @param _height
     * @param _min_intervals
     * @param _max_intervals
     */
    constructor(
        _type: Int,
        _frame_type: Int,
        _index: Int,
        _width: Int,
        _height: Int,
        _min_intervals: Int,
        _max_intervals: Int,
        _step: Int
    ) {
        type = _type
        frame_type = _frame_type
        index = _index
        width = _width
        height = _height
        frameIntervalType = 0
        frameIntervalIndex = 0
        intervals = IntArray(3)
        intervals!![0] = _min_intervals
        intervals!![1] = _max_intervals
        intervals!![2] = _step
        updateFrameRate()
    }

    /**
     * コンストラクタ
     * @param _type native側のraw_format_tの値, ただし9999は静止画
     * @param _frame_type native側のraw_frame_tの値
     * @param _index
     * @param _width
     * @param _height
     * @param _intervals
     */
    constructor(
        _type: Int,
        _frame_type: Int,
        _index: Int,
        _width: Int,
        _height: Int,
        _intervals: IntArray?
    ) {
        type = _type
        frame_type = _frame_type
        index = _index
        width = _width
        height = _height
        val n = _intervals?.size ?: -1
        if (n > 0) {
            frameIntervalType = n
            intervals = IntArray(n)
            System.arraycopy(_intervals, 0, intervals, 0, n)
        } else {
            frameIntervalType = -1
            intervals = null
        }
        frameIntervalIndex = 0
        updateFrameRate()
    }

    /**
     * コピーコンストラクタ
     * @param other
     */
    constructor(other: Size) {
        type = other.type
        frame_type = other.frame_type
        index = other.index
        width = other.width
        height = other.height
        frameIntervalType = other.frameIntervalType
        frameIntervalIndex = other.frameIntervalIndex
        val n = if (other.intervals != null) other.intervals!!.size else -1
        if (n > 0) {
            intervals = IntArray(n)
            System.arraycopy(other.intervals, 0, intervals, 0, n)
        } else {
            intervals = null
        }
        updateFrameRate()
    }

    private constructor(source: Parcel) {
        // 読み取り順はwriteToParcelでの書き込み順と同じでないとダメ
        type = source.readInt()
        frame_type = source.readInt()
        index = source.readInt()
        width = source.readInt()
        height = source.readInt()
        frameIntervalType = source.readInt()
        frameIntervalIndex = source.readInt()
        if (frameIntervalType >= 0) {
            intervals = if (frameIntervalType > 0) {
                IntArray(frameIntervalType)
            } else {
                IntArray(3)
            }
            source.readIntArray(intervals!!)
        } else {
            intervals = null
        }
        updateFrameRate()
    }

    fun set(other: Size?): Size {
        if (other != null) {
            type = other.type
            frame_type = other.frame_type
            index = other.index
            width = other.width
            height = other.height
            frameIntervalType = other.frameIntervalType
            frameIntervalIndex = other.frameIntervalIndex
            val n = if (other.intervals != null) other.intervals!!.size else -1
            if (n > 0) {
                intervals = IntArray(n)
                System.arraycopy(other.intervals, 0, intervals, 0, n)
            } else {
                intervals = null
            }
            updateFrameRate()
        }
        return this
    }

    // 一番近いのを選ぶ
    @get:Throws(IllegalStateException::class)
    var currentFrameRate: Float
        get() {
            val n = if (fps != null) fps!!.size else 0
            if (frameIntervalIndex >= 0 && (frameIntervalIndex < n)) {
                return fps!![frameIntervalIndex]
            }
            throw IllegalStateException("unknown frame rate or not ready")
        }
        set(frameRate) {
            // 一番近いのを選ぶ
            var index = -1
            val n = if (fps != null) fps!!.size else 0
            for (i in 0 until n) {
                if (fps!![i] <= frameRate) {
                    index = i
                    break
                }
            }
            frameIntervalIndex = index
        }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(type)
        dest.writeInt(frame_type)
        dest.writeInt(index)
        dest.writeInt(width)
        dest.writeInt(height)
        dest.writeInt(frameIntervalType)
        dest.writeInt(frameIntervalIndex)
        if (intervals != null) {
            dest.writeIntArray(intervals)
        }
    }

    fun updateFrameRate() {
        val n = frameIntervalType
        if (n > 0) {
            fps = FloatArray(n)
            for (i in 0 until n) {
                fps!![i] = 10000000.0f / intervals!![i]
                val _fps = fps!![i]
            }
        } else if (n == 0) {
            try {
                val min = Math.min(intervals!![0], intervals!![1])
                val max = Math.max(intervals!![0], intervals!![1])
                val step = intervals!![2]
                if (step > 0) {
                    var m = 0
                    run {
                        var i = min
                        while (i <= max) {
                            m++
                            i += step
                        }
                    }
                    fps = FloatArray(m)
                    m = 0
                    var i = min
                    while (i <= max) {
                        fps!![m++] = 10000000.0f / i
                        val _fps = fps!![m++]
                        i += step
                    }
                } else {
                    val max_fps = 10000000.0f / min
                    var m = 0
                    run {
                        var fps = 10000000.0f / min
                        while (fps <= max_fps) {
                            m++
                            fps += 1.0f
                        }
                    }
                    fps = FloatArray(m)
                    m = 0
                    run {
                        var fps = 10000000.0f / min
                        while (fps <= max_fps) {
                            this.fps!![m++] = fps
                            fps += 1.0f
                        }
                    }
                }
            } catch (e: Exception) {
                // ignore, なんでかminとmaxが0になってるんちゃうかな
                fps = null
            }
        }
        val m = if (fps != null) fps!!.size else 0
        val sb = StringBuilder()
        sb.append("[")
        for (i in 0 until m) {
            sb.append(String.format(Locale.US, "%4.1f", fps!![i]))
            if (i < m - 1) {
                sb.append(",")
            }
        }
        sb.append("]")
        frameRates = sb.toString()
        if (frameIntervalIndex > m) {
            frameIntervalIndex = 0
        }
    }

    override fun toString(): String {
        var frame_rate = 0.0f
        try {
            frame_rate = currentFrameRate
        } catch (e: Exception) {
        }
        return String.format(
            Locale.US,
            "Size(%dx%d@%4.1f,type:%d,frame:%d,index:%d,%s)",
            width,
            height,
            frame_rate,
            type,
            frame_type,
            index,
            frameRates
        )
    }

    companion object {
        @JvmField
        val CREATOR: Creator<Size?> = object : Creator<Size?> {
            override fun createFromParcel(source: Parcel): Size? {
                return Size(source)
            }

            override fun newArray(size: Int): Array<Size?> {
                return arrayOfNulls(size)
            }
        }
    }
}
