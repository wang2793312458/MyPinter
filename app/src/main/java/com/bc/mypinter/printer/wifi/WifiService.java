package com.bc.mypinter.printer.wifi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.bc.mypinter.printer.Device;
import com.bc.mypinter.printer.PrintService;
import com.bc.mypinter.printer.PrinterClass;
import com.bc.mypinter.printer.TcpClientClass;


public class WifiService extends PrintService implements PrinterClass {
	private static final String TAG = "WifiService";
	int port=10000;
	TcpClientClass tcpClient=null;
	DatagramPacket packet;
	private DatagramSocket socket;
	private DatagramPacket getpacket;
	private byte data2[] = new byte[4 * 1024];
	WifiManager mWm = null;
	ProgressDialog dialog;
	boolean sendDatagram = true;
	MyThread myth = null;
	Th th=null;
	int scanState=0;

	Context context;
	Handler mhandler, handler;

	public WifiService(Context _context, Handler _mhandler, Handler _handler) {
		context = _context;
		mhandler = _mhandler;
		handler = _handler;

		tcpClient=new TcpClientClass(mhandler);
	}

	@Override
	public boolean open(Context context) {
		// TODO Auto-generated method stub		
		return setWifi(context, true);
	}

	@Override
	public boolean close(Context context) {
		// TODO Auto-generated method stub
		setWifi(context, false);
		return false;
	}

	@Override
	public void scan() {
		// TODO Auto-generated method stub
		try {
			// 开启UDP接收数据监听，获取服务器IP
			th = new Th();
			th.start();

			String strIp=getIp();
			strIp=strIp.substring(0, strIp.lastIndexOf('.'));
			strIp+=".255";
			// UDP发送数据建立UDP服务器通信
			socket = new DatagramSocket();
			InetAddress serverAddress = InetAddress.getByName(strIp);// 设置对方IP
			String str = "AT+FIND=?\r\n";// 设置要发送的报文
			byte data[] = str.getBytes();// 把字符串str字符串转换为字节数组
			packet = new DatagramPacket(data, data.length, serverAddress, 10002);// 设置发送数据，地址，端口

			String getwayIP=tcpClient.getGatewayIPAddress(context);
			connect(getwayIP);
			if(tcpClient.isConnected())
			{
				sendDatagram=false;
				//tcpClient.DisConnect();
				Device d = new Device();
				d.deviceName = getwayIP;
				d.deviceAddress = getwayIP;
				Message msg = new Message();
				msg.what = 1;
				msg.obj = d;
				handler.sendMessage(msg);
			}
			else
			{
				sendDatagram=true;
				myth= new MyThread();
				myth.start();

				Message msg = new Message();
				msg.obj = "正在搜索设备";
				mhandler.sendMessage(msg);

				setState(PrinterClass.STATE_SCANING);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void stopScan() {
		// TODO Auto-generated method stub
		th=null;
		myth=null;
		sendDatagram=false;
	}

	@Override
	public boolean connect(String device) {
		// TODO Auto-generated method stub
		tcpClient.Connect(device, port);
		setState(PrinterClass.STATE_CONNECTED);
		return true;
	}

	@Override
	public boolean disconnect() {
		// TODO Auto-generated method stub
		tcpClient.DisConnect();
		return true;
	}

	@Override
	public int getState() {
		// TODO Auto-generated method stub
		return scanState;
	}

	@Override
	public boolean IsOpen() {
		// TODO Auto-generated method stub
		if (mWm != null) {
			return mWm.isWifiEnabled();
		} else {
			return false;
		}
	}

	@Override
	public boolean write(byte[] buffer) {
		// TODO Auto-generated method stub

		if(getState()!= PrinterClass.STATE_CONNECTED)
		{
			Toast toast = Toast.makeText(context, "connection lost", Toast.LENGTH_SHORT);
			toast.show();
			return false;
		}

		if (buffer.length <= 100) {
			tcpClient.SendData(buffer);
			return true;
		}
		int sendSize = 100;
		int issendfull=0;
		for (int j = 0; j < buffer.length; j += sendSize) {

			if (PrintService.isFUll) {
				Log.i("BUFFER", "BUFFER FULL");
				int index = 0;
				while (index++ < 500) {
					if (!PrintService.isFUll) {
						issendfull=0;
						Log.i("BUFFER", "BUFFER NULL"+index);
						break;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			byte[] btPackage = new byte[sendSize];
			if (buffer.length - j < sendSize) {
				btPackage = new byte[buffer.length - j];
			}
			System.arraycopy(buffer, j, btPackage, 0, btPackage.length);
			tcpClient.SendData(btPackage);

			/*try {
				Thread.sleep(86);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}

		//tcpClient.SendData(bt);
		return true;
	}

	@Override
	public boolean printText(String textStr) {
		// TODO Auto-generated method
		/*byte[] buffer = getText(textStr);

		if (buffer.length <= 100) {
			write(buffer);
			return true;
		}
		int sendSize = 100;
		int issendfull=0;
		for (int j = 0; j < buffer.length; j += sendSize) {

			byte[] btPackage = new byte[sendSize];
			if (buffer.length - j < sendSize) {
				btPackage = new byte[buffer.length - j];
			}
			System.arraycopy(buffer, j, btPackage, 0, btPackage.length);
			write(btPackage);

			try {
				Thread.sleep(86);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (PrintService.isFUll) {
				Log.i("BUFFER", "BUFFER FULL");
				int index = 0;
				while (index++ < 500) {
					if (!PrintService.isFUll) {
						issendfull=0;
						Log.i("BUFFER", "BUFFER NULL"+index);
						break;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		return true;*/

		return write(getText(textStr));
	}

	@Override
	public boolean printImage(Bitmap bitmap) {
		// TODO Auto-generated method stub
		write(getImage(bitmap));
		return write(new byte[]{0x0a});
	}

	@Override
	public boolean printUnicode(String textStr) {
		// TODO Auto-generated method stub
		return write(getTextUnicode(textStr));
	}

	/**
	 * 是否开启 wifi true：开启 false：关闭
	 *
	 * 一定要加入权限： <uses-permission
	 * android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
	 * <uses-permission
	 * android:name="android.permission.CHANGE_WIFI_STATE"></uses-permission>
	 *
	 * @param context
	 * @param isEnable
	 */
	public boolean setWifi(Context context, boolean isEnable) {
		if (mWm == null) {
			mWm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			// return;
		}
		return	mWm.setWifiEnabled(isEnable);
	}

	public class MyThread extends Thread {
		public void run() {
			int timeSpan=0;
			while (sendDatagram) {
				try {
					if(timeSpan++>20)
					{
						setState(PrinterClass.STATE_SCAN_STOP);
						timeSpan=0;
						break;
					}
					socket.send(packet);
					Thread.sleep(500);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}// 把数据发送到服务端。
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 获取服务器IP
	 *
	 * @author xuxl
	 *
	 */
	class Th extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
			boolean b = true;
			try {
				getpacket = new DatagramPacket(data2, data2.length);// 创建一个接收PACKET
			} catch (Exception e) {
			}
			while (b) {
				try {
					socket.receive(getpacket);// 获取UDP服务器发送数据信息，放入PACKET中
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 判断服务器是否发送数据
				if (getpacket.getAddress() != null) {
					//sendDatagram = false;
					// 获得服务器IP
					// String macAdd=getpacket.getData().toString();
					String ipStr = getpacket.getAddress().toString()
							.substring(1);
					String macAdd = new String(data2, 0, getpacket.getLength())
							.trim();

					Device d = new Device();
					d.deviceName = macAdd;
					d.deviceAddress = ipStr;
					Message msg = new Message();
					msg.what = 1;
					msg.obj = d;
					handler.sendMessage(msg);
				}
			}
		}
	}

	@Override
	public void setState(int state) {
		// TODO Auto-generated method stub
		Log.i(TAG,"setState:"+state);
		scanState= state;
	}

	@Override
	public List<Device> getDeviceList() {
		// TODO Auto-generated method stub
		return null;
	}

	private String getIp(){
		WifiManager wm=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		//检查Wifi状态
		if(!wm.isWifiEnabled())
			wm.setWifiEnabled(true);
		WifiInfo wi=wm.getConnectionInfo();
		//获取32位整型IP地址
		int ipAdd=wi.getIpAddress();
		//把整型地址转换成“*.*.*.*”地址
		String ip=intToIp(ipAdd);
		return ip;
	}
	private String intToIp(int i) {
		return (i & 0xFF ) + "." +
				((i >> 8 ) & 0xFF) + "." +
				((i >> 16 ) & 0xFF) + "." +
				( i >> 24 & 0xFF) ;
	}
}
