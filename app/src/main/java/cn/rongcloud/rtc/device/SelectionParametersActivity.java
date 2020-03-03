package cn.rongcloud.rtc.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.adapter.ItemDecoration;
import cn.rongcloud.rtc.device.adapter.SelectionParametersAdapter;
import cn.rongcloud.rtc.device.entity.EventBusInfo;
import cn.rongcloud.rtc.device.utils.Consts;
import cn.rongcloud.rtc.device.utils.OnItemClickListener;
import cn.rongcloud.rtc.util.SessionManager;

import static cn.rongcloud.rtc.device.utils.Consts.*;

/**
 * 选择列表
 */
public class SelectionParametersActivity extends DeviceBaseActivity {

    private TextView tv_Title;
    private RecyclerView recyclerView;
    private ArrayList<String> data = new ArrayList<>();
    private int requestCode = 0;

    public static void startActivity(Context context, int requestCode, ArrayList<String> list) {
        Intent intent = new Intent(context, SelectionParametersActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_KEY_REQUEST_CODE_KEY, requestCode);
        bundle.putStringArrayList(EXTRA_KEY_REQUEST_ARRAYLIST_DATA, list);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_parameters);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        tv_Title = findViewById(R.id.tv_Title);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        data = bundle.getStringArrayList(EXTRA_KEY_REQUEST_ARRAYLIST_DATA);
        requestCode = bundle.getInt(EXTRA_KEY_REQUEST_CODE_KEY);
        String title = getParameterTitle();
        tv_Title.setText(TextUtils.isEmpty(title) ? "" : title);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        SelectionParametersAdapter adapter = new SelectionParametersAdapter(data);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new ItemDecoration(this, 1));
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(int position) {
                String content = data.get(position);
                post(content);
            }
        });
    }

    public void settingInputClick(View view) {
        if (view.getId() == R.id.linear_save) {
//            save();
        } else if (view.getId() == R.id.settings_back) {
            finish();
        }
    }

    private void post(String content) {
//        switch (requestCode) {
//            case Consts.REQUEST_CODE_ENCODER_TYPE:
//                SessionManager.getInstance().put(Consts.SP_ENCODER_TYPE_KEY, content.equals(getResources().getString(R.string.hw_encoder_str)) ? true : false);
//                break;
//            case Consts.REQUEST_CODE_ENCODER_NAME:
//                SessionManager.getInstance().put(Consts.SP_ENCODER_NAME_KEY, content);
//                break;
//            case Consts.REQUEST_CODE_DECODER_TYPE:
//                SessionManager.getInstance().put(Consts.SP_DECODER_TYPE_KEY, content.equals(getResources().getString(R.string.hw_decoder_str)) ? true : false);
//                break;
//            case Consts.REQUEST_CODE_DECODER_NAME:
//                SessionManager.getInstance().put(Consts.SP_ENCODER_NAME_KEY, content);
//                break;
//            case REQUEST_CODE_CAPTURE_TYPE:
//                SessionManager.getInstance().put(ACQUISITION_MODE_KEY, content.equals(getResources().getString(R.string.capture_type_texture)) ? true : false);
//                break;
//            case REQUEST_CODE_ENCODER_LEVEL:
//                SessionManager.getInstance().put(Consts.SP_ENCODER_LEVEL_KEY, content.equals(getResources().getString(R.string.encoder_leval_hight)) ? true : false);
//                break;
//            default:
//                break;
//        }
        EventBusInfo info = new EventBusInfo(requestCode, content, 0);
        EventBus.getDefault().post(info);
        finish();
    }

    private String getParameterTitle() {
        String val = "";
        switch (requestCode) {
            case Consts.REQUEST_CODE_ENCODER_NAME:
                val = getResources().getString(R.string.encoder_name_str);
                break;
            case Consts.REQUEST_CODE_DECODER_NAME:
                val = getResources().getString(R.string.decoder_name_str);
                break;
            default:
                break;
        }
        return val;
    }
}
