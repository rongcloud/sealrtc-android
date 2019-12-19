package cn.rongcloud.rtc.device;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.rongcloud.rtc.LoadDialog;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.adapter.ColorFormatAdapter;
import cn.rongcloud.rtc.device.adapter.ItemDecoration;
import cn.rongcloud.rtc.device.entity.ColorFormat;
import cn.rongcloud.rtc.device.entity.MediaType;
import cn.rongcloud.rtc.device.utils.Consts;
import cn.rongcloud.rtc.device.utils.OnColorFormatItemClickListener;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.UserUtils;
import cn.rongcloud.rtc.util.Utils;

import static cn.rongcloud.rtc.device.utils.Consts.decoder_colorFormat_eventBus;
import static cn.rongcloud.rtc.device.utils.Consts.encoder_colorFormat_eventBus;

public class CodecColorFormatActivity extends DeviceBaseActivity {
    private RecyclerView recyclerView_colorFormat;
    private ColorFormatAdapter colorFormatAdapter;
    private ArrayList<MediaType> mediaTypes = new ArrayList<>();
    private ArrayList<ColorFormat> colorFormats = new ArrayList<>();
    private Map<String, ColorFormat> colorFormats_select = new HashMap<>();
    private String CodecType = "";
    private RelativeLayout rela_error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_activity_codec_color);
        colorFormats_select.clear();
        TextView tc_codecName = (TextView) findViewById(R.id.tc_codecName);
        rela_error = (RelativeLayout) findViewById(R.id.rela_error);
        recyclerView_colorFormat = (RecyclerView) findViewById(R.id.recyclerView_colorFormat);
        try {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            if (!TextUtils.isEmpty(bundle.getString("CodecName"))) {
                tc_codecName.setText(bundle.getString("CodecName"));
            } else {
                tc_codecName.setText("NULL");
            }
            CodecType = bundle.getString("CodecType");
            if (bundle.getParcelableArrayList("mediaTypes") != null) {
                mediaTypes = bundle.getParcelableArrayList("mediaTypes");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        initData();
    }

    private void initData() {
        for (int i = 0; i < mediaTypes.size(); i++) {
            for (int j = 0; j < mediaTypes.get(i).getColorFormats().size(); j++) {
                colorFormats.add(mediaTypes.get(i).getColorFormats().get(j));
            }
        }
        if (colorFormats == null || colorFormats.size() == 0) {
            rela_error.setVisibility(View.VISIBLE);
            return;
        } else {
            rela_error.setVisibility(View.GONE);
        }
        LoadDialog.show(CodecColorFormatActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CodecColorFormatActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView_colorFormat.setLayoutManager(linearLayoutManager);
        colorFormatAdapter = new ColorFormatAdapter(colorFormats);
        recyclerView_colorFormat.setAdapter(colorFormatAdapter);
        recyclerView_colorFormat.setFocusableInTouchMode(false);
        recyclerView_colorFormat.requestFocus();
        recyclerView_colorFormat.addItemDecoration(new ItemDecoration(CodecColorFormatActivity.this, 1));
        colorFormatAdapter.setOnItemClickListener(new OnColorFormatItemClickListener() {
            @Override
            public void onClick(int position, String alias, int colorFormat) {
                ColorFormat colorFormat1 = new ColorFormat(colorFormat, alias);
                if (colorFormats_select.containsKey(alias)) {
                    colorFormats_select.remove(alias);
                } else {
                    colorFormats_select.put(alias, colorFormat1);
                }
                Log.i("colorFormatAdapter", "colorFormat :" + alias);
            }
        });
        LoadDialog.dismiss(CodecColorFormatActivity.this);
    }

    public void colorFormatClick(View view) {
        try {
            if (colorFormats_select == null || colorFormats_select.size() == 0) {
                Toast.makeText(this, "没有选择颜色空间！", Toast.LENGTH_SHORT).show();
                finish();
            }
            if (colorFormats_select.size() > 1) {
                Toast.makeText(this, "只能选择一个！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (CodecType.equals("1")) {
                String alias = "";
                int color = 0;
                for (Map.Entry<String, ColorFormat> val : colorFormats_select.entrySet()) {
                    alias = val.getKey();
                    color = val.getValue().getColor();
                }
                SessionManager.getInstance(Utils.getContext()).put(Consts.decoder_colorFormat_alias_key, alias);
                SessionManager.getInstance(Utils.getContext()).put(Consts.colorFormat_val_key, color);
                EventBus.getDefault().post(decoder_colorFormat_eventBus);
            } else if (CodecType.equals("0")) {
                String alias = "";
                int color = 0;
                for (Map.Entry<String, ColorFormat> val : colorFormats_select.entrySet()) {
                    alias = val.getKey();
                    color = val.getValue().getColor();
                }
                SessionManager.getInstance(Utils.getContext()).put(Consts.encoder_colorFormat_alias_key, alias);
                SessionManager.getInstance(Utils.getContext()).put(Consts.colorFormat_val_key, color);
                EventBus.getDefault().post(encoder_colorFormat_eventBus);
            } else {
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
                return;
            }
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoadDialog.dismiss(CodecColorFormatActivity.this);
    }
}
