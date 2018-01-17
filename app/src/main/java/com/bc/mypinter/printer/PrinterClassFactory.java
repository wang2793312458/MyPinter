package com.bc.mypinter.printer;

import android.content.Context;
import android.os.Handler;

import com.bc.mypinter.printer.bt.BtService;
import com.bc.mypinter.printer.usb.UsbService;
import com.bc.mypinter.printer.wifi.WifiService;


public class PrinterClassFactory {
    public static PrinterClass create(int type,Context _context,Handler _mhandler,Handler _handler){
        if(type==0){
            return new BtService(_context,_mhandler, _handler);
        }else if(type==1){
            return new WifiService(_context,_mhandler, _handler);
        }else if(type==2){
            return new UsbService(_mhandler);
        }
        return null;
    }

}
