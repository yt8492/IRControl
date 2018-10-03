package com.yt8492.ircontrol

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewParent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
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
    lateinit var controlList: MutableMap<String, MutableList<Pair<Boolean, Long>>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        ledGpio.setActiveType(Gpio.ACTIVE_HIGH)
        sensorGpio.setEdgeTriggerType(Gpio.EDGE_BOTH)
        sensorGpio.registerGpioCallback(sensorCallback)
        val json: String?  = getSharedPreferences(TAG, Context.MODE_PRIVATE)?.getString(CONTROL_LIST, null)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val listType = object : TypeToken<MutableMap<String, MutableList<Pair<Boolean, Long>>>>() {}.type
        json?.let {
            controlList = gson.fromJson(it, listType)
        } ?: kotlin.run {
            controlList = mutableMapOf()
        }
        button.setOnClickListener { _ ->
            gpioEdgeList = mutableListOf()
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("記録中")
            dialog.setPositiveButton("終了") { _, _ ->
                gpioEdgeList?.let {
                    controlList.put(editText.text.toString(), it)
                }
                setSharedPreference(controlList)
                gpioEdgeList = null
                setSpinnerAdapter()
            }
            dialog.show()
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(paremt: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val spinner = parent as Spinner
                val control = controlList[spinner.selectedItem]
                control?.let {
                    for (i in it.indices) {
                        if (i < it.size - 1) {
                            ledGpio.value = it[i].first
                            Log.d(TAG, it[i].first.toString())
                            TimeUnit.NANOSECONDS.sleep(it[i+1].second - it[i].second)
                        }
                    }
                }
            }

        }
        setSpinnerAdapter()
    }

    private fun setSpinnerAdapter() {
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, controlList.keys.toList())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
    }

    private val sensorCallback = GpioCallback { gpio ->
        gpioEdgeList?.add(Pair(gpio.value, System.nanoTime()))
        true
    }

    private fun setSharedPreference(controls: MutableMap<String, MutableList<Pair<Boolean, Long>>>) {
        val sharedPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(controls)
        editor.putString(CONTROL_LIST, json)
        editor.apply()
    }
}
