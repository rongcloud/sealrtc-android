package cn.rongcloud.rtc.device;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.device.adapter.AVSettingsDataSource;
import cn.rongcloud.rtc.device.adapter.AVSettingsPreviewAdapater;
import cn.rongcloud.rtc.device.adapter.ItemDecoration;
import cn.rongcloud.rtc.device.entity.AVConfigInfo;
import java.util.ArrayList;
import java.util.List;

/** @author xiaoq */
public class AVSettingsPreviewActivity extends RongRTCBaseActivity {
    private RecyclerView mRecyclerView;
    private AVSettingsPreviewAdapater mAdapter;
    private List<AVConfigInfo> avConfigInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_av_settings_preview);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new AVSettingsPreviewAdapater(avConfigInfoList);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new ItemDecoration(this, 1));

        loadChangedSettings();
    }

    private void loadChangedSettings() {
        avConfigInfoList.clear();
        avConfigInfoList.addAll(AVSettingsDataSource.getInstance().getChangedConfig());
        mAdapter.notifyDataSetChanged();
    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.settings_back:
                this.setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.settings_save:
                if (AVSettingsDataSource.getInstance().saveConfig()) {
                    Toast.makeText(this.getApplicationContext(), "配置已保存", Toast.LENGTH_SHORT)
                            .show();
                    this.setResult(RESULT_OK);
                    finish();
                }
                break;
            default:
                break;
        }
    }
}
