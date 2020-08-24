package cn.rongcloud.rtc;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioGroup;

public class AudioMixActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private Fragment[] fragments = new Fragment[2];
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_mix);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_button);
        radioGroup.setOnCheckedChangeListener(this);

        FragmentManager fm = getSupportFragmentManager();
        fragments[0] = fm.findFragmentById(R.id.fm_audio_mix);
        fragments[1] = fm.findFragmentById(R.id.fm_audio_effect);
        fm.beginTransaction().hide(fragments[1]).commit();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_btn_close:
            case R.id.v_place_holder:
                performClose();
                break;
        }
    }

    private void performClose() {
        AudioMixFragment.alive = false;
        finish();
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switchFragment();
    }

    private void switchFragment() {
        Fragment currentFragment = fragments[index()];
        Fragment nextFragment = fragments[next()];
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.hide(currentFragment);
        transaction.show(nextFragment);
        transaction.commit();
    }

    private int index() {
        return index % fragments.length;
    }

    private int next() {
        index++;
        return index();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.mix_slide_down);
    }
}
