package com.bc.mypinter.printer.usb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.bc.mypinter.MainActivity;
import com.bc.mypinter.printer.Device;
import com.bc.mypinter.printer.PrintService;
import com.bc.mypinter.printer.PrinterClass;


public class UsbService extends PrintService implements PrinterClass {
	PrintService printservice = new PrintService();
	private static final String TAG = "UsbService";
	public readThread handlerThread;
	public boolean READ_ENABLE = false;
	protected final Object ThreadLock = new Object();
	int size;
	char[] readBuffer;
	private Handler mHandler;


	//发送数据定义
	private WriteThread mWriteThread;
	private List<byte[]> messageList = new ArrayList<byte[]>();
	boolean iswrite = false;
	boolean canWrite = false;

	int baudRate = 115200; /* baud rate */
	byte stopBit = 1; /* 1:1stop bits, 2:2 stop bits */
	byte dataBit = 8; /* 8:8bit, 7: 7bit 6: 6bit 5: 5bit */
	byte parity = 0; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
	byte flowControl = 0; /* 0:none, 1: flow control(CTS,RTS) */

	public UsbService(Handler _mHandler) {
		mHandler = _mHandler;
	}

	@Override
	public boolean open(Context context) {
		if (MainActivity.uartInterface.SetConfig(baudRate, dataBit, stopBit,
				parity, flowControl)) {
			if (READ_ENABLE == false) {
				READ_ENABLE = true;
				handlerThread = new readThread();
				handlerThread.start();

				mWriteThread = new WriteThread();
				mWriteThread.start();
			}
		}
		return true;
	}

	@Override
	public boolean close(Context context) {
		if (MainActivity.uartInterface != null) {
			if (MainActivity.uartInterface.isConnected()) {
				MainActivity.uartInterface.CloseDevice();
			}
			if (READ_ENABLE == true) {
				READ_ENABLE = false;
			}
			MainActivity.uartInterface = null;
			if (mWriteThread != null) {
				mWriteThread = null;
			}
			return true;
		}
		return false;
	}

	@Override
	public void scan() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Device> getDeviceList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stopScan() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean connect(String device) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean disconnect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getState() {
		if (MainActivity.uartInterface != null) {
			if (MainActivity.uartInterface.isConnected()) {
				return 3;
			}
		}
		return 0;
	}

	@Override
	public void setState(int state) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean IsOpen() {
		if (MainActivity.uartInterface != null) {
			if (MainActivity.uartInterface.isConnected()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean write(byte[] buffer) {
		return messageList.add(buffer);
	}

	@Override
	public boolean printText(String textStr) {
		byte[] buffer = printservice.getText(textStr);
		return write(buffer);
	}

	@Override
	public boolean printImage(Bitmap bitmap) {
		byte[] buffer = printservice.getImage(bitmap);
		return write(buffer);
	}

	@Override
	public boolean printUnicode(String textStr) {
		byte[] buffer = printservice.getTextUnicode(textStr);
		return write(buffer);
	}

	private boolean Write(byte[] buffer)
	{
		int sendSize = 500;
		if (buffer.length <= sendSize) {
			try {
				MainActivity.uartInterface.WriteData(buffer, buffer.length);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		for (int j = 0; j < buffer.length; j += sendSize) {
			byte[] btPackage = new byte[sendSize];
			if (buffer.length - j < sendSize) {
				btPackage = new byte[buffer.length - j];
			}
			System.arraycopy(buffer, j, btPackage, 0, btPackage.length);
			try {
				MainActivity.uartInterface.WriteData(btPackage, btPackage.length);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	/* usb input data handler */
	private class readThread extends Thread {

		public void run() {
			while (READ_ENABLE) {
				// Log.d(TAG, "Thread");
				synchronized (ThreadLock) {
					if (MainActivity.uartInterface != null) {
						readBuffer = new char[512];
						size = MainActivity.uartInterface.ReadData(
								readBuffer, 64);

						if (size > 0) {
							byte[] buffer=new byte[size];

							for(int i=0;i<buffer.length;i++)
							{
								buffer[i]=(byte) readBuffer[i];
							}

							if (buffer[0] == 0x13) {
								PrintService.isFUll = true;
								Log.i(TAG, "0x13:");
							} else if (buffer[0] == 0x11) {
								PrintService.isFUll = false;
								Log.i(TAG, "0x11:");
							} else if (iswrite) {
								iswrite=false;
								if (buffer[0] == 0) {
									canWrite = true;
								} else {
									canWrite = false;
									Write(new byte[] { 0x0a });
								}
								Log.i(TAG, "Pinter State：" + byte2HexStr(buffer, size));
							} else {
								mHandler.obtainMessage(PrinterClass.MESSAGE_READ, size, -1, buffer)
										.sendToTarget();
							}
						}
					}
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	public static String byte2HexStr(byte[] b, int lenth) {
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < lenth; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
			sb.append(" ");
		}
		return sb.toString().toUpperCase().trim();
	}


	private class WriteThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while (!isInterrupted()) {
				for (int i = 0; i < messageList.size(); i++) {
					byte[] buffer = messageList.get(i);
					iswrite = true;
					Write(new byte[] { 0x1b, 0x76 });
					for (int m = 0; m < 300; m++) {
						if (canWrite) {
							canWrite=false;
							Log.i(TAG, "Wait state time：" + m);
							if (Write(buffer)) {
								messageList.remove(i);
							}
							break;
						}
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					canWrite=false;
					iswrite = false;
				}
			}
		}
	}
}
