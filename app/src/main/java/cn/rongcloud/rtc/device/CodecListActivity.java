package cn.rongcloud.rtc.device;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import cn.rongcloud.rtc.LoadDialog;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.adapter.CodecMediaTypeAdapter;
import cn.rongcloud.rtc.device.adapter.ItemDecoration;
import cn.rongcloud.rtc.device.entity.*;
import cn.rongcloud.rtc.device.utils.Consts;
import cn.rongcloud.rtc.device.utils.OnItemClickListener;
import cn.rongcloud.rtc.util.SessionManager;
import java.util.ArrayList;

public class CodecListActivity extends DeviceBaseActivity {

    private ArrayList<CodecInfo> codecInfos = new ArrayList<>();
    private ArrayList<String> codecName = new ArrayList<>();
    private ArrayList<MediaType> mediaTypes = new ArrayList<>();
    private RecyclerView recyclerView;
    private String CodecType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_activity_encoder_list);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        TextView tv_codecListTitle = (TextView) findViewById(R.id.tv_codecListTitle);

        try {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            if (!TextUtils.isEmpty(bundle.getString("CodecType"))) {
                CodecType = bundle.getString("CodecType");
                tv_codecListTitle.setText(CodecType.equals("1") ? "解码器列表" : "编码器列表");
            } else {
                tv_codecListTitle.setText("编解码器列表");
            }
            codecInfos = bundle.getParcelableArrayList("CodecInfo");
            codecName = bundle.getStringArrayList("CodecName");
        } catch (Exception e) {
            e.printStackTrace();
        }
        initData();
    }

    private void initData() {
        LoadDialog.show(CodecListActivity.this);
        if (codecInfos == null) {
            codecInfos = new ArrayList<>();
        }
        if (codecName == null) {
            codecName = new ArrayList<>();
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        CodecMediaTypeAdapter codecMediaTypeAdapter = new CodecMediaTypeAdapter(codecName);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        recyclerView.setAdapter(codecMediaTypeAdapter);
        recyclerView.addItemDecoration(new ItemDecoration(this, 1));
        LoadDialog.dismiss(CodecListActivity.this);
        codecMediaTypeAdapter.setOnItemClickListener(
                new OnItemClickListener() {
                    @Override
                    public void onClick(int position) {
                        if (TextUtils.isEmpty(CodecType)) {
                            Toast.makeText(CodecListActivity.this, "编码器类型错误", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        mediaTypes.clear();
                        String name = "";
                        name = codecName.get(position);
                        LoadDialog.show(CodecListActivity.this);
                        if (!TextUtils.isEmpty(name)) {
                            Intent intent =
                                    new Intent(
                                            CodecListActivity.this, CodecColorFormatActivity.class);
                            for (int i = 0; i < codecInfos.size(); i++) {
                                if (codecInfos.get(i).getCodecName().equals(name)) {
                                    mediaTypes.addAll(codecInfos.get(i).getMediaTypes());
                                    break;
                                }
                            }
                            SessionManager.getInstance().put(Consts.SP_ENCODER_NAME_KEY, name);
                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList("mediaTypes", mediaTypes);
                            bundle.putString("CodecName", name);
                            bundle.putString("CodecType", CodecType);
                            intent.putExtras(bundle);
                            LoadDialog.dismiss(CodecListActivity.this);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }
}
