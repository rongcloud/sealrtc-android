package cn.rongcloud.rtc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.rongcloud.rtc.base.RongRTCBaseActivity;

/** Created by suancai on 2016/11/4. */
public class OptionsPickActivity extends RongRTCBaseActivity {

    public static final String BUNDLE_KEY_DATAS = "datas";
    public static final String BUNDLE_KEY_TITLE = "title";
    public static final String BUNDLE_KEY_RESULT = "result";

    private ListView contenteListView;
    private TextView pickListTitle;

    private String[] datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_optionpick);

        datas = getIntent().getStringArrayExtra(BUNDLE_KEY_DATAS);
        String title = getIntent().getStringExtra(BUNDLE_KEY_TITLE);

        contenteListView = (ListView) findViewById(R.id.content_listview);
        pickListTitle = (TextView) findViewById(R.id.pickoption_title);
        //        if(title.equals(getResources().getString(R.string.settings_text_resolution))){
        //            title="分辨率";
        //        }
        pickListTitle.setText(title);

        contenteListView.setAdapter(new ArrayAdapter<String>(this, R.layout.picklist_text, datas));

        findViewById(R.id.option_back)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });

        contenteListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        String data = datas[position];
                        Intent intent = new Intent();
                        intent.putExtra(BUNDLE_KEY_RESULT, data);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                });
    }
}
