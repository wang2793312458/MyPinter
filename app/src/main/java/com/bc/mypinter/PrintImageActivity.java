package com.bc.mypinter;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileNotFoundException;

public class PrintImageActivity extends AppCompatActivity {
    private static final int REQUEST_EX = 1;
    private Bitmap btMap = null;
    private ImageView iv;
    private Button bt_image;//
    private Button bt_openpic;//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_image);

        iv = (ImageView) findViewById(R.id.iv_test);

        bt_openpic = (Button) findViewById(R.id.bt_openpci);
        bt_openpic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_EX);
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
        btMap = BitmapFactory.decodeResource(getResources(), R.mipmap.demo);
        iv.setImageBitmap(btMap);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EX && resultCode == RESULT_OK
                && null != data) {
            Uri selectedImage = data.getData();
            ContentResolver cr = this.getContentResolver();

            try {
                btMap = BitmapFactory.decodeStream(cr
                        .openInputStream(selectedImage));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            iv.setImageBitmap(btMap);
        }

    }
}
