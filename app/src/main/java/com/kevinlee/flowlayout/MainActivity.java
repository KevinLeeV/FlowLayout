package com.kevinlee.flowlayout;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    private FlowLayout fl;
    private String[] titles = new String[]{"titles", "titles123234", "titles welcome", "titles lalala", "titles",
            "titles", "titles321", "titles hello", "titles come on", "titles", "titles haha", "titles 456", "titles"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    /**
     * 初始化View
     */
    private void initViews() {
        fl = (FlowLayout) findViewById(R.id.fl);
        fl.setTextSize(14);
        fl.setPadding(10);
        fl.generateButton(titles, 5);
        fl.setOnClickListener(new FlowLayout.OnClickListener() {
            @Override
            public void onClick(TextView view, int position) {
                Toast.makeText(MainActivity.this, view.getText() + "," + position, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onClick(List<View> viewList, List<Integer> posList, List<String> textList) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < viewList.size(); i++) {
                    int position = posList.get(i);
                    String title = textList.get(i);
                    sb.append(title + "," + position + "\n");
                }
                Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
