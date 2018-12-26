package com.example.alex.demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import utils.SerialPortUtils
import java.nio.Buffer
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val serialPortUtils = SerialPortUtils()

        serialPortUtils.openSerialPort()

        serialPortUtils.setOnDataReceiveListener(object : SerialPortUtils.OnDataReceiveListener {

            override fun onDataReceive(buffer: ByteArray, size: Int) {
                Log.d("1", "进入数据监听事件中。。。" + String(buffer));
            }
        })

        serialPortUtils.sendSerialPort("01040000000131CA")

    }

}
