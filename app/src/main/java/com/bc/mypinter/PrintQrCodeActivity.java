package com.bc.mypinter;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bc.mypinter.printer.BarcodeCreater;
import com.bc.mypinter.printer.PrintService;
import com.bc.mypinter.printer.PrinterClass;

import java.io.UnsupportedEncodingException;

public class PrintQrCodeActivity extends AppCompatActivity {
    private Bitmap btMap = null;
    private ImageView iv;
    private TextView et_input;
    private Button bt_2d;
    private Button bt_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_qr_code);

        iv = (ImageView) findViewById(R.id.iv_test);
        et_input = (EditText) findViewById(R.id.et_input);
        bt_2d = (Button) findViewById(R.id.bt_2d);
        bt_2d.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (MainActivity.pl.getState() != PrinterClass.STATE_CONNECTED) {
                    Toast.makeText(
                            PrintQrCodeActivity.this,
                            PrintQrCodeActivity.this.getResources().getString(
                                    R.string.str_unconnected), 2000).show();
                    return;
                }
                String message = et_input.getText().toString();
                if (message.length() > 0) {
                    try {
                        message = new String(message.getBytes("utf8"));
                    } catch (UnsupportedEncodingException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    btMap = BarcodeCreater.encode2dAsBitmap(message,
                            PrintService.imageWidth * 8,
                            PrintService.imageWidth * 8, 2);
                    BarcodeCreater.saveBitmap2file(btMap, "mypic1.JPEG");
                    iv.setImageBitmap(btMap);
                }

            }
        });

        bt_image = (Button) findViewById(R.id.bt_image);
        bt_image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (btMap != null) {
                    new Thread() {
                        public void run() {
                            MainActivity.pl.printImage(btMap);
                        }
                    }.start();

                    return;
                }
            }
        });

        btMap = BarcodeCreater.encode2dAsBitmap("打印机测试Printer Testing",
                PrintService.imageWidth * 8, PrintService.imageWidth * 8, 2);
        BarcodeCreater.saveBitmap2file(btMap, "mypic1.JPEG");
        iv.setImageBitmap(btMap);
    }
}
