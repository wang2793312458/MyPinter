package com.bc.mypinter.printer;

import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ArrayAdapter;

public interface PrinterClass {

	/**
	 * open the device
	 *
	 **/
	public boolean open(Context context);

	/**
	 * close the device
	 *
	 **/
	public boolean close(Context context);

	/**
	 * scan printer
	 *
	 **/
	public void scan();

	/**
	 * get device
	 * @return
	 */
	public List<Device> getDeviceList();

	/**
	 * stop scan
	 */
	public void stopScan();

	/**
	 * connect a printer
	 *
	 **/
	public boolean connect(String device);

	/**
	 * disconnect a printer
	 *
	 **/
	public boolean disconnect();
	/**
	 * get the connect state
	 *
	 **/
	public int getState();

	/**
	 * Set state
	 * @param state
	 */
	public void setState(int state);

	public boolean IsOpen();
	/**
	 * send data
	 *
	 **/
	public boolean write(byte[] bt);

	/**
	 * Print text
	 * @param textStr
	 * @return
	 */
	public boolean printText(String textStr);

	/**
	 * Print Image
	 * @param bitmap
	 * @return
	 */
	public boolean printImage(Bitmap bitmap);

	/**
	 * Print Unicode
	 * @param textStr
	 * @return
	 */
	public boolean printUnicode(String textStr);


	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
	// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
	// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
	// device
	public static final int LOSE_CONNECT = 4;
	public static final int FAILED_CONNECT = 5;
	public static final int SUCCESS_CONNECT = 6; // now connected to a remote


	public static final int STATE_SCANING = 7;// 扫描状态
	public static final int STATE_SCAN_STOP = 8;

	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;

	/*
	 * 检测型号
	 */
	public static final byte[] CMD_CHECK_TYPE=new byte[]{0x1B,0x2B};
	/*
	 * 水平制表
	 */
	public static final byte[] CMD_HORIZONTAL_TAB=new byte[]{0x09};
	/*
	 * 换行
	 */
	public static final byte[] CMD_NEWLINE=new byte[]{0x0A};
	/*
	 * 打印当前存储内容
	 */
	public static final byte[] CMD_PRINT_CURRENT_CONTEXT=new byte[]{0x0D};
	/*
	 * 初始化打印机
	 */
	public static final byte[] CMD_INIT_PRINTER=new byte[]{0x1B,0x40};
	/*
	 * 允许下划线打印
	 */
	public static final byte[] CMD_UNDERLINE_ON=new byte[]{0x1C,0x2D,0x01};
	/*
	 * 禁止下划线打印
	 */
	public static final byte[] CMD_UNDERLINE_OFF =new byte[]{0x1C,0x2D,0x00};
	/*
	 * 允许粗体打印
	 */
	public static final byte[] CMD_Blod_ON=new byte[]{0x1B,0x45,0x01};
	/*
	 * 禁止粗体打印
	 */
	public static final byte[] CMD_BLOD_OFF=new byte[]{0x1B,0x45,0x00};
	/*
	 * 选择字体：ASCII(12*24) 汉字（24*24）
	 */
	public static final byte[] CMD_SET_FONT_24x24=new byte[]{0x1B,0x4D,0x00};
	/*
	 * 选择字体：ASCII(8*16)  汉字（16*16）
	 */
	public static final byte[] CMD_SET_FONT_16x16=new byte[]{0x1B,0x4D,0x01};
	/*
	 * 字符正常：  不放大
	 */
	public static final byte[] CMD_FONTSIZE_NORMAL=new byte[]{0x1D,0x21,0x00};
	/*
	 * 字符2倍高：纵向放大
	 */
	public static final byte[] CMD_FONTSIZE_DOUBLE_HIGH=new byte[]{0x1D,0x21,0x01};
	/*
	 * 字符2倍宽：横向放大
	 */
	public static final byte[] CMD_FONTSIZE_DOUBLE_WIDTH=new byte[]{0x1D,0x21,0x10};
	/*
	 * 字符2倍整体放大
	 */
	public static final byte[] CMD_FONTSIZE_DOUBLE=new byte[]{0x1D,0x21,0x11};
	/*
	 * 左对齐
	 */
	public static final byte[] CMD_ALIGN_LEFT=new byte[]{0x1B,0x61,0x00};
	/*
	 * 居中对齐
	 */
	public static final byte[] CMD_ALIGN_MIDDLE=new byte[]{0x1B,0x61,0x01};
	/*
	 * 居右对齐
	 */
	public static final byte[] CMD_ALIGN_RIGHT=new byte[]{0x1B,0x61,0x02};
	/*
	 * 页进纸/黑标定位
	 */
	public static final byte[] CMD_BLACK_LOCATION=new byte[]{0x0C};

}
