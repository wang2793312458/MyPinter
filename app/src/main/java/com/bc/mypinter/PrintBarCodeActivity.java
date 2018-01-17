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

public class PrintBarCodeActivity extends AppCompatActivity {
    private Bitmap btMap = null;
    private ImageView iv;
    private TextView et_input;
    private Button bt_bar;
    private Button bt_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_bar_code);

        iv = (ImageView) findViewById(R.id.iv_test);
        et_input = (EditText) findViewById(R.id.et_input);
        bt_bar = (Button) findViewById(R.id.bt_bar);
        bt_bar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (MainActivity.pl.getState() != PrinterClass.STATE_CONNECTED) {
                    Toast.makeText(
                            PrintBarCodeActivity.this,
                            PrintBarCodeActivity.this.getResources().getString(
                                    R.string.str_unconnected), 2000).show();
                    return;
                }
                String message = et_input.getText().toString();

                if (message.getBytes().length > message.length()) {
                    Toast.makeText(
                            PrintBarCodeActivity.this,
                            PrintBarCodeActivity.this.getResources().getString(
                                    R.string.str_cannotcreatebar), 2000).show();
                    return;
                }
                if (message.length() > 0) {

                    btMap = BarcodeCreater.creatBarcode(PrintBarCodeActivity.this,
                            message, PrintService.imageWidth * 8, 100, true, 1);
                    iv.setImageBitmap(btMap);
                }

            }
        });


        bt_image = (Button) findViewById(R.id.bt_image);
        bt_image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (btMap != null) {
                    MainActivity.pl.printImage(btMap);
                    return;
                }
            }
        });

        btMap = BarcodeCreater.creatBarcode(PrintBarCodeActivity.this,
                "9787111291954", PrintService.imageWidth * 8, 100, true, 1);
        iv.setImageBitmap(btMap);
    }
}
