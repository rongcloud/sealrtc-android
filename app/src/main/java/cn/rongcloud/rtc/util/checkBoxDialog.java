package cn.rongcloud.rtc.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import cn.rongcloud.rtc.BuildConfig;
import cn.rongcloud.rtc.R;

/** @Author dengxudong. @Time 2018/3/5. @Description: */
public class checkBoxDialog extends Dialog {
    private Context mContext;
    private AppCompatCheckBox btnSwitchCamera,
            btnMuteSpeaker,
            btnWhiteBoard,
            btnRaiseHand,
            btnChangeResolution_up,
            btnChangeResolution_down;
    private AppCompatCheckBox btn_modeSelect;

    public checkBoxDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public checkBoxDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    protected checkBoxDialog(
            @NonNull Context context,
            boolean cancelable,
            @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(mContext, R.layout.layout_app_dialog_checkbox, null);
        //        LinearLayout.LayoutParams params=new
        // LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
        // LinearLayout.LayoutParams.WRAP_CONTENT);
        //        params.setMargins(0,0,40,0);
        //        view.setLayoutParams(params);
        setContentView(view);

        //        this.setCanceledOnTouchOutside(false);
        //        this.setCancelable(false);
        //
        Window win = getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        //        lp.type = WindowManager.LayoutParams.TYPE_TOAST;
        lp.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.gravity = Gravity.RIGHT;
        lp.x = 75; // px 对话框出现在右边,所以lp.x就表示相对右边的偏移,负值忽略.
        win.setAttributes(lp);

        setCanceledOnTouchOutside(false);
        setCancelable(false);

        btn_modeSelect = (AppCompatCheckBox) findViewById(R.id.btn_modeSelect);
        btnRaiseHand = (AppCompatCheckBox) findViewById(R.id.menu_request_to_normal);
        btnSwitchCamera = (AppCompatCheckBox) findViewById(R.id.menu_switch);
        //        btnMuteMic = (AppCompatCheckBox) findViewById(R.id.menu_mute_mic);

        btnMuteSpeaker = (AppCompatCheckBox) findViewById(R.id.menu_mute_speaker);
        btnWhiteBoard = (AppCompatCheckBox) findViewById(R.id.menu_whiteboard);

        btnChangeResolution_up = (AppCompatCheckBox) findViewById(R.id.menu_up);
        btnChangeResolution_down = (AppCompatCheckBox) findViewById(R.id.menu_down);

        btnChangeResolution_up.setVisibility(View.GONE);
        btnChangeResolution_down.setVisibility(View.GONE);
    }

    public AppCompatCheckBox getBtn_modeSelect() {
        return btn_modeSelect;
    }
    //
    //    public void setVideoModeBtnText(String str){
    //        if(btn_modeSelect==null){return;}
    //        btn_modeSelect.setText(str);
    //    }

    public AppCompatCheckBox getCheckBox(String str) {
        AppCompatCheckBox cb = null;
        switch (str) {
            case "btnRaiseHand":
                cb = btnRaiseHand;
                break;
            case "btnSwitchCamera":
                cb = btnSwitchCamera;
                break;
            case "btnCloseCamera":
                //                cb=btnCloseCamera;
                break;
            case "btnMuteMic":
                //                cb=btnMuteMic;
                break;
            case "btnMuteSpeaker":
                cb = btnMuteSpeaker;
                break;
            case "btnWhiteBoard":
                cb = btnWhiteBoard;
                break;
            case "btn_modeSelect":
                cb = btn_modeSelect;
                break;
        }
        return cb;
    }

    public void resetCbState() {
        if (null != btnWhiteBoard && btnWhiteBoard.isChecked() == true)
            btnWhiteBoard.setChecked(false);
    }

    public void showSideBar() {
        if (null != mContext && !this.isShowing()) {
            this.show();
        }
    }

    public void dismissSideBar() {
        if (null != mContext) {
            this.dismiss();
        }
    }

    public AppCompatCheckBox getbtnChangeResolution_up() {
        return btnChangeResolution_up;
    }

    public AppCompatCheckBox getbtnChangeResolution_down() {
        return btnChangeResolution_down;
    }
}
