package com.bc.mypinter;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.bc.mypinter.printer.PrinterClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        setListAdapter(new SimpleAdapter(this, getData("simple-list-item-2"), android.R.layout.simple_list_item_2, new String[]{"title", "description"}, new int[]{android.R.id.text1, android.R.id.text2}));

        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);

        if (MainActivity.pl != null && (position == 0 || position == 1) && MainActivity.pl.getState() != PrinterClass.STATE_CONNECTED) {
            intent = new Intent();
            intent.setClass(PrintActivity.this, PrintSettingActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 0:
                if (MainActivity.pl.getState() != PrinterClass.STATE_CONNECTED) {
                    PrintActivity.this.finish();
                }
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        MainActivity.pl.disconnect();
        super.onDestroy();
    }

    /**
     * 当List的项被选中时触发
     */
    protected void onListItemClick(ListView listView, View v, int position, long id) {
        /*Map map = (Map)listView.getItemAtPosition(position);
        Toast toast = Toast.makeText(this, map.get("title")+" is selected.", Toast.LENGTH_LONG);
        toast.show();*/
        Intent intent = new Intent();
        switch (position) {
            case 0:
                intent.setClass(PrintActivity.this,
                        PrintTextActivity.class);
                break;
            case 1:
                intent.setClass(PrintActivity.this,
                        PrintImageActivity.class);
                break;
            case 2:
                intent.setClass(PrintActivity.this,
                        PrintBarCodeActivity.class);
                break;
            case 3:
                intent.setClass(PrintActivity.this,
                        PrintQrCodeActivity.class);
                break;
            case 4:
                intent.setClass(PrintActivity.this,
                        PrintCmdActivity.class);
                break;
            default:
                break;
        }
        ////intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    /**
     * 构造SimpleAdapter的第二个参数，类型为List<Map<?,?>>
     *
     * @param title
     * @return
     */
    private List<Map<String, String>> getData(String title) {
        List<Map<String, String>> listData = new ArrayList<Map<String, String>>();

        Map<String, String> map = new HashMap<String, String>();
        map.put("title", getResources().getString(R.string.str_printword));
        map.put("description", getResources().getString(R.string.str_printword_desc));
        listData.add(map);

        map = new HashMap<String, String>();
        map.put("title", getResources().getString(R.string.str_printimg));
        map.put("description", getResources().getString(R.string.str_printimg_desc));
        listData.add(map);

        map = new HashMap<String, String>();
        map.put("title", getResources().getString(R.string.str_printbarcode));
        map.put("description", getResources().getString(R.string.str_printbarcode_desc));
        listData.add(map);

        map = new HashMap<String, String>();
        map.put("title", getResources().getString(R.string.str_printqrcode));
        map.put("description", getResources().getString(R.string.str_printqrcode_desc));
        listData.add(map);

        map = new HashMap<String, String>();
        map.put("title", getResources().getString(R.string.str_printcmd));
        map.put("description", getResources().getString(R.string.str_printcmd_desc));
        listData.add(map);

        return listData;
    }
}
