package utils;

import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android_serialport_api.SerialPort;

public class SerialPortUtils {
    private final String TAG = "SerialPortUtils";
    private String path = "/dev/ttyS2";
    private int baudrate = 4800;
    public boolean serialPortStatus = false; //是否打开串口标志
    public String data_;
    public boolean threadStatus; //线程状态，为了安全终止线程

    public SerialPort serialPort = null;
    public InputStream inputStream = null;
    public OutputStream outputStream = null;

    /**
     * 打开串口
     * @return serialPort串口对象
     */
    public SerialPort openSerialPort(){
        try {
            serialPort = new SerialPort(new File(path),baudrate,0);
            this.serialPortStatus = true;
            threadStatus = false; //线程状态

            //获取打开的串口中的输入输出流，以便于串口数据的收发
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();

            new ReadThread().start(); //开始线程监控是否有数据要接收
        } catch (IOException e) {
            Log.e(TAG, "openSerialPort: 打开串口异常：" + e.toString());
            return serialPort;
        }
        Log.d(TAG, "openSerialPort: 打开串口");
        return serialPort;
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort(){
        try {
            inputStream.close();
            outputStream.close();

            this.serialPortStatus = false;
            this.threadStatus = true; //线程状态
            serialPort.close();
        } catch (IOException e) {
            Log.e(TAG, "closeSerialPort: 关闭串口异常："+e.toString());
            return;
        }
        Log.d(TAG, "closeSerialPort: 关闭串口成功");
    }

    /**
     * 发送串口指令（字符串）
     * @param data String数据指令
     */
    public void sendSerialPort(String data){
        Log.d(TAG, "sendSerialPort: 发送数据");

        try {
            byte[] sendData = Hex2byte(data); //string转byte[]
//            this.data_ = new String(sendData); //byte[]转string
//          byte[] sendData = new byte[]{0x01, 0x04, 0x00, 0x00, 0x00, 0x01, 0x31, (byte) 0xCA};
            if (sendData.length > 0) {
                outputStream.write(sendData);
                outputStream.write('\n');
                //outputStream.write('\r'+'\n');
                outputStream.flush();
                Log.d(TAG, "sendSerialPort: 串口数据发送成功");
            }
        } catch (IOException e) {
            Log.e(TAG, "sendSerialPort: 串口数据发送失败：" + e.toString());
        }

    }

    /**
     * 单开一线程，来读数据
     */
    private class ReadThread extends Thread{
        @Override
        public void run() {
            super.run();
            //判断进程是否在运行，更安全的结束进程
            while (!threadStatus){
                Log.d(TAG, "进入线程run");
                //64   1024
                byte[] buffer = new byte[64];
                int size; //读取数据的大小
                try {
                    size = inputStream.read(buffer);
                    if (size > 0){
                        Log.d(TAG, "run: 接收到了数据：" + bytes2HexString(buffer, size));
                        Log.d(TAG, "run: 接收到了数据大小：" + String.valueOf(size));
                        onDataReceiveListener.onDataReceive(buffer,size);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "run: 数据读取异常：" +e.toString());
                }
            }

        }
    }

    //这是写了一监听器来监听接收数据
    public OnDataReceiveListener onDataReceiveListener = null;
    public static interface OnDataReceiveListener {
        public void onDataReceive(byte[] buffer, int size);
    }
    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

    public String bytes2HexString(byte[] b,int length) {
        String r = "";

        for (int i = 0; i < length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            r += hex.toUpperCase();
        }

        return r;
    }


    public byte[] Hex2byte(String var1) {
        var1 = var1.replace(" ", "");
        byte[] var3 = new byte[var1.length() / 2];
        char[] var4 = var1.toCharArray();

        for(int var2 = 0; var2 < var1.length() / 2; ++var2) {
            var3[var2] = (byte)((Character.digit(var4[var2 * 2], 16) << 4) + Character.digit(var4[var2 * 2 + 1], 16) & 255);
        }

        return var3;
    }

}
