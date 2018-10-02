package com.yt8492.ircontrol

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
const val LED = "BCM17"
const val SENSOR = "BCM18"
const val CONTROL_LIST = "ControlList"
private val TAG = MainActivity::class.java.simpleName
class MainActivity : Activity() {

    val noeRecording = false
    val manager = PeripheralManager.getInstance()
    val ledGpio = manager.openGpio(LED)
    val sensorGpio = manager.openGpio(SENSOR)
    var gpioEdgeList: MutableList<Pair<Boolean, Long>>? = null
    lateinit var controlList: MutableList<MutableList<Pair<Boolean, Long>>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var json = getSharedPreferences(TAG, Context.MODE_PRIVATE)?.getString(CONTROL_LIST, null)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val listType = object : TypeToken<MutableList<MutableList<Pair<Boolean, Long>>>>() {}.type
        controlList = gson.fromJson(json, listType)
        button.setOnClickListener {
            gpioEdgeList = mutableListOf()
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("記録中")
            dialog.setPositiveButton("終了", DialogInterface.OnClickListener { dialogInterface, i ->
                gpioEdgeList?.let {
                    controlList.add(it)
                }
                gpioEdgeList = null
            })
        }
    }

    private val sensorCallback = object : GpioCallback {
        override fun onGpioEdge(gpio: Gpio): Boolean {
            gpioEdgeList?.add(Pair(gpio.value, System.nanoTime()))
            return true
        }

    }

    private fun setSharedPreference(controls: List<Pair<Boolean, Long>>) {
        val sharedPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(controls)
        editor.putString(CONTROL_LIST, json)
        editor.apply()
    }
}
