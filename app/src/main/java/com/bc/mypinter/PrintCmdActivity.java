package com.bc.mypinter;

import android.app.ListActivity;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintCmdActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_cmd);
        setListAdapter(new SimpleAdapter(this,getData("simple-list-item-2"),android.R.layout.simple_list_item_2,new String[]{"title", "description"},new int[]{android.R.id.text1, android.R.id.text2}));
    }

    /**
     * 当List的项被选中时触发
     */
    protected void onListItemClick(ListView listView, View v, int position, long id) {
        /*Map map = (Map)listView.getItemAtPosition(position);
        Toast toast = Toast.makeText(this, map.get("title")+" is selected.", Toast.LENGTH_LONG);
        toast.show();*/
        Map map = (Map)listView.getItemAtPosition(position);
        String cmd=map.get("description").toString();
        byte[] bt =hexStringToBytes(cmd);
        MainActivity.pl.write(bt);

        ////PrintActivity.pl.printText("This text is a print test\r\n");

        Toast toast = Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * 构造SimpleAdapter的第二个参数，类型为List<Map<?,?>>
     * @param title
     * @return
     */
    private List<Map<String, String>> getData(String title) {
        List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
        Resources res =getResources();
        String[] cmdStr=res.getStringArray(R.array.cmd);
        for(int i=0;i<cmdStr.length;i++)
        {
            String [] cmdArray=cmdStr[i].split(",");
            if(cmdArray.length==2)
            {
                Map<String, String> map = new HashMap<String, String>();
                map.put("title",cmdArray[0]);
                map.put("description", cmdArray[1]);
                listData.add(map);
            }
        }
        return listData;
    }

    /**
     * 将字符串形式表示的十六进制数转换为byte数组
     */
    public static byte[] hexStringToBytes(String hexString)
    {
        hexString = hexString.toLowerCase();
        String[] hexStrings = hexString.split(" ");
        byte[] bytes = new byte[hexStrings.length];
        for (int i = 0; i < hexStrings.length; i++)
        {
            char[] hexChars = hexStrings[i].toCharArray();
            bytes[i] = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
        }
        return bytes;
    }
    private static byte charToByte(char c)
    {
        return (byte) "0123456789abcdef".indexOf(c);
    }

}
