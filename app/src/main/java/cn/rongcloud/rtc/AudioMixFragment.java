package cn.rongcloud.rtc;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

import cn.rongcloud.rtc.api.RCRTCAudioMixer;
import cn.rongcloud.rtc.api.RCRTCEngine;

public class AudioMixFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    public static final String DEFAULT_AUDIO_PATH = "file:///android_asset/music.mp3";
    private static int REQUEST_OPEN_FILE = 1;

    // 混音且播放
    public static final int MODE_PLAY_MIX = 0;
    // 只混不播放
    public static final int MODE_MIX_ONLY = 1;
    // 只播放不混
    public static final int MODE_PLAY_ONLY = 2;
    // 替换麦克风数据，不播放
    public static final int MODE_REPLACE = 3;

    private Context context;
    private ImageButton img_btn_play_pause;
    private ImageButton img_btn_stop;
    private Button btn_select_music;
    private Button btn_change_mode;
    private SeekBar sb_mix_local_vol;
    private SeekBar sb_mix_remote_vol;
    private SeekBar sb_mic_vol;
    private TextView tv_mix_local_vol;
    private TextView tv_mix_remote_vol;
    private TextView tv_mic_vol;

    public static String audioPath = DEFAULT_AUDIO_PATH;
    public static boolean mixing = false;
    public static boolean alive = false;
    public static int mixMode = MODE_PLAY_MIX;
    private String[] mixModes = new String[4];

    public AudioMixFragment() { }

    public static AudioMixFragment newInstance() {
        return new AudioMixFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AudioMixFragment.alive = true;
        this.context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_mix, container, false);
        img_btn_play_pause = (ImageButton) view.findViewById(R.id.img_btn_play_pause);
        img_btn_stop = (ImageButton) view.findViewById(R.id.img_btn_stop);
        btn_select_music = (Button) view.findViewById(R.id.btn_select_music);
        btn_change_mode = (Button) view.findViewById(R.id.btn_change_mode);
        sb_mix_local_vol = (SeekBar) view.findViewById(R.id.sb_mix_local_vol);
        sb_mix_remote_vol = (SeekBar) view.findViewById(R.id.sb_mix_remote_vol);
        sb_mic_vol = (SeekBar) view.findViewById(R.id.sb_mic_vol);
        tv_mix_local_vol = (TextView) view.findViewById(R.id.tv_mix_local_vol);
        tv_mix_remote_vol = (TextView) view.findViewById(R.id.tv_mix_remote_vol);
        tv_mic_vol = (TextView) view.findViewById(R.id.tv_mic_vol);

        img_btn_stop.setEnabled(mixing);
        img_btn_play_pause.setSelected(mixing);

        img_btn_stop.setOnClickListener(this);
        img_btn_play_pause.setOnClickListener(this);
        view.findViewById(R.id.btn_select_music).setOnClickListener(this);
        view.findViewById(R.id.btn_change_mode).setOnClickListener(this);

        sb_mic_vol.setOnSeekBarChangeListener(this);
        sb_mix_remote_vol.setOnSeekBarChangeListener(this);
        sb_mix_local_vol.setOnSeekBarChangeListener(this);

        tv_mic_vol.setText("0");
        int mixRemoteVol = RCRTCAudioMixer.getInstance().getMixingVolume();
        tv_mix_remote_vol.setText(String.valueOf(mixRemoteVol));
        sb_mix_remote_vol.setProgress(mixRemoteVol);

        int mixLocalVol = RCRTCAudioMixer.getInstance().getPlaybackVolume();
        tv_mix_local_vol.setText(String.valueOf(mixLocalVol));
        sb_mix_local_vol.setProgress(mixLocalVol);

        int recordingVol = RCRTCEngine.getInstance().getDefaultAudioStream().getRecordingVolume();
        tv_mic_vol.setText(String.valueOf(recordingVol));
        sb_mic_vol.setProgress(recordingVol);

        mixModes[0] = getResources().getString(R.string.mix_mode_play_mix);
        mixModes[1] = getResources().getString(R.string.mix_mode_mix_only);
        mixModes[2] = getResources().getString(R.string.mix_mode_play_only);
        mixModes[3] = getResources().getString(R.string.mix_mode_replace);

        btn_select_music.setText(getFileName(audioPath));
        btn_change_mode.setText(mixModes[mixMode]);

        return view;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_btn_play_pause:
                boolean selected = img_btn_play_pause.isSelected();
                img_btn_play_pause.setSelected(!selected);
                if (selected) {
                    performPause();
                } else {
                    performPlay();
                }
                break;
            case R.id.img_btn_stop:
                performStop();
                break;
            case R.id.btn_select_music:
                performSelectMusic();
                break;
            case R.id.btn_change_mode:
                performChangeMode();
                break;
        }
    }

    private void performPlay() {
        if (mixing) {
            RCRTCAudioMixer.getInstance().resume();
        } else {
            boolean result = false;
            switch (mixMode) {
                case MODE_PLAY_MIX:
                    result =
                            RCRTCAudioMixer.getInstance()
                                    .startMix(audioPath, RCRTCAudioMixer.Mode.MIX, true, -1);
                    break;
                case MODE_MIX_ONLY:
                    result =
                            RCRTCAudioMixer.getInstance()
                                    .startMix(audioPath, RCRTCAudioMixer.Mode.MIX, false, -1);
                    break;
                case MODE_PLAY_ONLY:
                    result =
                            RCRTCAudioMixer.getInstance()
                                    .startMix(audioPath, RCRTCAudioMixer.Mode.NONE, true, -1);
                    break;
                case MODE_REPLACE:
                    result =
                            RCRTCAudioMixer.getInstance()
                                    .startMix(audioPath, RCRTCAudioMixer.Mode.REPLACE, true, -1);
                    break;
            }
            if (result) {
                img_btn_stop.setEnabled(true);
                mixing = true;
            } else {
                String toast = getResources().getString(R.string.mix_file_not_support);
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
                img_btn_play_pause.setSelected(false);
            }
        }
    }

    private void performPause() {
        RCRTCAudioMixer.getInstance().pause();
    }

    private void performStop() {
        RCRTCAudioMixer.getInstance().stop();
        img_btn_play_pause.setSelected(false);
        img_btn_stop.setEnabled(false);
        mixing = false;
    }

    private void performSelectMusic() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_OPEN_FILE);
    }

    private void performChangeMode() {
        new AlertDialog.Builder(context)
                .setItems(
                        mixModes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int index) {
                                mixMode = index;
                                btn_change_mode.setText(mixModes[mixMode]);
                                dialogInterface.dismiss();
                                performStop();
                            }
                        })
                .create()
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_OPEN_FILE == requestCode && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                File file = uri2File(data.getData());
                if (file == null) {
                    Toast.makeText(context, "获取不到该文件的绝对路径！", Toast.LENGTH_LONG).show();
                    return;
                }
                audioPath = file.getAbsolutePath();
                performStop();
                btn_select_music.setText(file.getName());
            }
        }
    }


    private String getFileName(String path) {
        return new File(path).getName();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sb_mic_vol:
                tv_mic_vol.setText(String.valueOf(progress));
                RCRTCEngine.getInstance().getDefaultAudioStream().adjustRecordingVolume(progress);
                break;
            case R.id.sb_mix_local_vol:
                tv_mix_local_vol.setText(String.valueOf(progress));
                RCRTCAudioMixer.getInstance().setPlaybackVolume(progress);
                break;
            case R.id.sb_mix_remote_vol:
                tv_mix_remote_vol.setText(String.valueOf(progress));
                RCRTCAudioMixer.getInstance().setMixingVolume(progress);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // do nothing
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // do nothing
    }

    /**
     * Reference https://github.com/Blankj/AndroidUtilCode/blob/master/lib/utilcode/src/main/java/com/blankj/utilcode/util/UriUtils.java
     */
    public File uri2File(@NonNull final Uri uri) {
        String authority = uri.getAuthority();
        String scheme = uri.getScheme();
        String path = uri.getPath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && path != null) {
            String[] externals = new String[]{"/external", "/external_path"};
            for (String external : externals) {
                if (path.startsWith(external + "/")) {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + path.replace(external, ""));
                    if (file.exists()) {
                        return file;
                    }
                }
            }
        }
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            if (path != null) {
                return new File(path);
            }
            return null;
        }// end 0
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents".equals(authority)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return new File(Environment.getExternalStorageDirectory() + "/" + split[1]);
                } else {
                    // Below logic is how External Storage provider build URI for documents
                    // http://stackoverflow.com/questions/28605278/android-5-sd-card-label
                    StorageManager mStorageManager = (StorageManager) context.getSystemService(
                            Context.STORAGE_SERVICE);
                    try {
                        Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                        Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
                        Method getUuid = storageVolumeClazz.getMethod("getUuid");
                        Method getState = storageVolumeClazz.getMethod("getState");
                        Method getPath = storageVolumeClazz.getMethod("getPath");
                        Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
                        Method isEmulated = storageVolumeClazz.getMethod("isEmulated");

                        Object result = getVolumeList.invoke(mStorageManager);

                        final int length = Array.getLength(result);
                        for (int i = 0; i < length; i++) {
                            Object storageVolumeElement = Array.get(result, i);
                            //String uuid = (String) getUuid.invoke(storageVolumeElement);

                            final boolean mounted =
                                    Environment.MEDIA_MOUNTED.equals(getState.invoke(storageVolumeElement))
                                            || Environment.MEDIA_MOUNTED_READ_ONLY
                                            .equals(getState.invoke(storageVolumeElement));

                            //if the media is not mounted, we need not get the volume details
                            if (!mounted) {
                                continue;
                            }

                            //Primary storage is already handled.
                            if ((Boolean) isPrimary.invoke(storageVolumeElement)
                                    && (Boolean) isEmulated.invoke(storageVolumeElement)) {
                                continue;
                            }

                            String uuid = (String) getUuid.invoke(storageVolumeElement);

                            if (uuid != null && uuid.equals(type)) {
                                return new File(getPath.invoke(storageVolumeElement) + "/" + split[1]);
                            }
                        }
                    } catch (Exception ex) {
                    }
                }
                return null;
            }// end 1_0
            else if ("com.android.providers.downloads.documents".equals(authority)) {
                final String id = DocumentsContract.getDocumentId(uri);
                if (!TextUtils.isEmpty(id)) {
                    try {
                        final Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                Long.valueOf(id)
                        );
                        return getFileFromUri(contentUri, "1_1");
                    } catch (NumberFormatException e) {
                        if (id.startsWith("raw:")) {
                            return new File(id.substring(4));
                        }
                    }
                }
                return null;
            }// end 1_1
            else if ("com.android.providers.media.documents".equals(authority)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    return null;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getFileFromUri(contentUri, selection, selectionArgs, "1_2");
            }// end 1_2
            else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                return getFileFromUri(uri, "1_3");
            }// end 1_3
            else {
                return null;
            }// end 1_4
        }// end 1
        else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            return getFileFromUri(uri, "2");
        }// end 2
        else {
            return null;
        }// end 3
    }

    private File getFileFromUri(final Uri uri, final String code) {
        return getFileFromUri(uri, null, null, code);
    }

    private File getFileFromUri(final Uri uri,
                                final String selection,
                                final String[] selectionArgs,
                                final String code) {
        if ("com.google.android.apps.photos.content".equals(uri.getAuthority())) {
            if (!TextUtils.isEmpty(uri.getLastPathSegment())) {
                return new File(uri.getLastPathSegment());
            }
        } else if ("com.tencent.mtt.fileprovider".equals(uri.getAuthority())) {
            String path = uri.getPath();
            if (!TextUtils.isEmpty(path)) {
                File fileDir = Environment.getExternalStorageDirectory();
                return new File(fileDir, path.substring("/QQBrowser".length(), path.length()));
            }
        }

        final Cursor cursor = context.getContentResolver().query(
                uri, new String[]{"_data"}, selection, selectionArgs, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndex("_data");
                if (columnIndex > -1) {
                    return new File(cursor.getString(columnIndex));
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            cursor.close();
        }
    }

}
