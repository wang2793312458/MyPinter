package com.bc.mypinter.printer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;

public class TcpClientClass {

	Handler mHandler;

	public TcpClientClass(Handler _mhandler) {
		mHandler = _mhandler;
	}

	private static final String TAG = "TcpClientClass";
	public Socket client;
	public OutputStream outputStream = null;
	public InputStream inputStream = null;

	public void Connect(String serverAddr, int port) {
		SocketAddress my_sockaddr = new InetSocketAddress(serverAddr, port);

		client = new Socket();
		try {
			client.connect(my_sockaddr, 5000);
			outputStream = client.getOutputStream();
			inputStream = client.getInputStream();
			new Thread(new TCPServerThread()).start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
		}
	}

	public boolean isConnected() {
		return client.isConnected();
	}

	public void DisConnect() {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
			if (client != null) {
				client.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
		}
	}

	public void SendData(byte[] bt) {
		try {
			outputStream.write(bt);
			outputStream.flush();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, e.getMessage());
		}
	}

	public class TCPServerThread extends Thread {

		public TCPServerThread() {
		}

		public void run() {
			// tvRecv.setText("start");
			byte[] buffer = new byte[1024];
			final StringBuilder sb = new StringBuilder();
			while (true) {
				try {
					int readSize = inputStream.read(buffer);
					Log.d(TAG, "readSize:" + readSize);
					// Server is stoping
					if (readSize == -1) {
						inputStream.close();
						break;
					}
					// Update the receive editText
					else if (readSize > 0) {
						byte[] btRec = new byte[readSize];

						if (btRec[0] == 0x13) {
							PrintService.isFUll = true;
							Log.i(TAG, "0x13:");
						} else if (btRec[0] == 0x11) {
							PrintService.isFUll = false;
							Log.i(TAG, "0x11:");
						} else {
							System.arraycopy(buffer, 0, btRec, 0, readSize);
							mHandler.obtainMessage(PrinterClass.MESSAGE_READ,
									readSize, 0, btRec).sendToTarget();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					// tvRecv.setText("error");
				}
			}

		}

		// tvRecv.setText("end");
	}

	public String getGatewayIPAddress(Context ctx) {
		WifiManager wifi_service = (WifiManager) ctx
				.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
		WifiInfo wifiinfo = wifi_service.getConnectionInfo();
		return Formatter.formatIpAddress(dhcpInfo.gateway);
	}
}
