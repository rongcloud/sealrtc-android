package cn.rongcloud.rtc.device;

import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_AUDIO_SOURCE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.adapter.AudioSourceAdapter;
import cn.rongcloud.rtc.device.adapter.ItemDecoration;
import cn.rongcloud.rtc.device.entity.AudioSourceInfo;
import cn.rongcloud.rtc.device.entity.EventBusInfo;
import cn.rongcloud.rtc.device.utils.OnColorFormatItemClickListener;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

public class AudioSourceSelectActivity extends AppCompatActivity {
    private AudioSourceInfo audioSourceSelected;
    private List<AudioSourceInfo> audioSourceInfos;
    private AudioSourceAdapter audioSourceAdapter;
    private RecyclerView audioSourceRecyclerView;

    private static final String EXTRA_KEY_AUDIO_SOURCE_CODE = "EXTRA_KEY_AUDIO_SOURCE_CODE";

    public static void startActivity(Context context, int audioSourceCode) {
        Bundle bundle = new Bundle();
        Intent intent = new Intent(context, AudioSourceSelectActivity.class);
        bundle.putInt(EXTRA_KEY_AUDIO_SOURCE_CODE, audioSourceCode);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_source_select);

        audioSourceRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_audio_source);
        try {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();

            int audioSource = bundle.getInt(EXTRA_KEY_AUDIO_SOURCE_CODE);

            audioSourceInfos = new ArrayList<>();
            audioSourceInfos.add(new AudioSourceInfo("CAMCORDER", 5));
            audioSourceInfos.add(new AudioSourceInfo("DEFAULT", 0));
            audioSourceInfos.add(new AudioSourceInfo("MIC", 1));
            audioSourceInfos.add(new AudioSourceInfo("REMOTE_SUBMIX", 8));
            audioSourceInfos.add(new AudioSourceInfo("UNPROCESSED", 9));
            audioSourceInfos.add(new AudioSourceInfo("VOICE_CALL", 4));
            audioSourceInfos.add(new AudioSourceInfo("VOICE_COMMUNICATION", 7));
            audioSourceInfos.add(new AudioSourceInfo("VOICE_DOWNLINK", 3));
            audioSourceInfos.add(new AudioSourceInfo("VOICE_RECOGNITION", 6));
            audioSourceInfos.add(new AudioSourceInfo("VOICE_UPLINK", 2));

            audioSourceAdapter = new AudioSourceAdapter(audioSourceInfos);
            audioSourceAdapter.setSelectItem(audioSource);

            LinearLayoutManager linearLayoutManager =
                    new LinearLayoutManager(
                            AudioSourceSelectActivity.this, LinearLayoutManager.VERTICAL, false);
            audioSourceRecyclerView.setLayoutManager(linearLayoutManager);

            audioSourceRecyclerView.setAdapter(audioSourceAdapter);
            audioSourceRecyclerView.setFocusableInTouchMode(false);
            audioSourceRecyclerView.requestFocus();
            audioSourceRecyclerView.addItemDecoration(
                    new ItemDecoration(AudioSourceSelectActivity.this, 1));

            audioSourceAdapter.setOnItemClickListener(
                    new OnColorFormatItemClickListener() {
                        @Override
                        public void onClick(int position, String name, int value) {
                            audioSourceSelected = new AudioSourceInfo(name, value);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onNavBackClick(View view) {
        finish();
    }

    public void onOkClick(View view) {
        if (audioSourceSelected == null) return;
        EventBusInfo info =
                new EventBusInfo(
                        REQUEST_CODE_AUDIO_SOURCE,
                        String.valueOf(audioSourceSelected.getCode()),
                        audioSourceSelected.getCode());
        EventBus.getDefault().post(info);
        finish();
    }
}
