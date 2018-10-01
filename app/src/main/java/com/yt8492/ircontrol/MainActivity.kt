package com.yt8492.ircontrol

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManager
import kotlinx.android.synthetic.main.activity_main.*
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
class MainActivity : Activity() {

    val noeRecording = false
    val manager = PeripheralManager.getInstance()
    val ledGpio = manager.openGpio(LED)
    val sensorGpio = manager.openGpio(SENSOR)
    var gpioEdgeList = arrayListOf<Pair<Boolean, Long>>()
    var gpioEdgeTimeFrom = 0
    var gpioEdgeTimeTo = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("記録中")
            dialog.setPositiveButton("終了", DialogInterface.OnClickListener { dialogInterface, i ->

            })
        }
    }

    private val sensorCallback = object : GpioCallback {
        override fun onGpioEdge(gpio: Gpio): Boolean {
            gpioEdgeList.add(Pair(gpio.value, 1))
            return true
        }

    }

    private fun recordIr() {
        while (true) {

        }
    }
}
