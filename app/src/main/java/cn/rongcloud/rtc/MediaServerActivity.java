package cn.rongcloud.rtc;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.media.http.HttpClient;
import cn.rongcloud.rtc.media.http.Request;
import cn.rongcloud.rtc.media.http.RequestMethod;
import cn.rongcloud.rtc.util.SessionManager;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MediaServerActivity extends RongRTCBaseActivity {

    private static final String MEDIA_CONFIGURATION =
            "https://sealrtc.rongcloud.cn/user/configuration";

    private EditText mediaUrlEditText;
    private TextView mediaUrlTextView;
    private List<Model> models;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_server);
        mediaUrlEditText = (EditText) findViewById(R.id.et_media_url);
        mediaUrlEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        String text = s.toString();
                        boolean match = false;
                        Model model = null;
                        if (models != null) {
                            for (int i = 0; i < models.size(); ++i) {
                                model = models.get(i);
                                if (TextUtils.equals(model.url, text)) {
                                    match = true;
                                    break;
                                }
                            }
                        }
                        if (match) {
                            mediaUrlTextView.setText(model.name);
                        } else {
                            mediaUrlTextView.setText("无");
                        }
                    }
                });
        mediaUrlTextView = (TextView) findViewById(R.id.tv_setting_option_media_url);
        String name = SessionManager.getInstance().getString("MediaName");
        String url = SessionManager.getInstance().getString("MediaUrl");
        if (!TextUtils.isEmpty(url)) {
            mediaUrlEditText.setText(url);
            mediaUrlTextView.setText(name);
        }
        findViewById(R.id.setting_option_media_url)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final MediaUrlListDialog dialog = new MediaUrlListDialog();
                                dialog.setOnItemClickListener(
                                        new MediaUrlListDialog.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(Model model) {
                                                mediaUrlTextView.setText(model.name);
                                                mediaUrlEditText.setText(model.url);
                                            }
                                        });
                                loadMediaUrlList(
                                        new ResultCallBack() {
                                            @Override
                                            public void onResult(List<Model> list) {
                                                models = list;
                                                dialog.setData(list);
                                                dialog.show(getFragmentManager(), "");
                                            }
                                        });
                            }
                        });

        findViewById(R.id.option_back)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        String name = mediaUrlTextView.getText().toString();
        String url = mediaUrlEditText.getText().toString();
        if (!TextUtils.isEmpty(url)) {
            SessionManager.getInstance().put("MediaName", name);
            SessionManager.getInstance().put("MediaUrl", url);
            RCRTCEngine.getInstance().setMediaServerUrl(url);
        }
    }

    private void loadMediaUrlList(final ResultCallBack callBack) {
        Request request =
                new Request.Builder().url(MEDIA_CONFIGURATION).method(RequestMethod.GET).build();
        HttpClient.getDefault()
                .request(
                        request,
                        new HttpClient.ResultCallback() {
                            @Override
                            public void onResponse(String result) {
                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    int code = jsonObject.optInt("code");
                                    if (code == 200) {
                                        jsonObject = jsonObject.optJSONObject("result");
                                        JSONArray jsonArray =
                                                jsonObject.optJSONArray("mediaservers");
                                        List<Model> list = new ArrayList<>();
                                        for (int i = 0; i < jsonArray.length(); ++i) {
                                            jsonObject = jsonArray.optJSONObject(i);
                                            Model model = new Model();
                                            model.name = jsonObject.optString("name");
                                            model.url = jsonObject.optString("url");
                                            list.add(model);
                                        }
                                        callBack.onResult(list);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int errorCode) {}
                        });
    }

    public static class MediaUrlListDialog extends DialogFragment {
        private RecyclerView recyclerView;
        private Adapter adapter;
        private OnItemClickListener onItemClickListener;
        private List<Model> models;

        @Nullable
        @Override
        public View onCreateView(
                LayoutInflater inflater,
                @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.layout_dialog_media_url, container, false);
            Context context = view.getContext();
            recyclerView = (RecyclerView) view.findViewById(R.id.recycler_media_url);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter = new Adapter(context));
            if (models != null) {
                adapter.setData(models);
            }
            final Window window = getDialog().getWindow();
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            wlp.dimAmount = 0.0f;
            window.setAttributes(wlp);
            setCancelable(true);
            return view;
        }

        public void setData(List<Model> models) {
            this.models = models;
            if (adapter != null) {
                adapter.setData(models);
                adapter.notifyDataSetChanged();
            }
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        private class Adapter extends RecyclerView.Adapter<ViewHolder> {

            private Context mContext;
            private List<Model> models = new ArrayList<>();

            public Adapter(Context mContext) {
                this.mContext = mContext;
            }

            public void setData(List<Model> models) {
                this.models.clear();
                this.models.addAll(models);
            }

            @Override
            public ViewHolder onCreateViewHolder(
                    ViewGroup parent, int viewType) {
                View view =
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.item_media_url, parent, false);
                ViewHolder holder = new ViewHolder(view);
                return holder;
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, final int position) {
                holder.update(models.get(position));
                holder.itemView.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (onItemClickListener != null) {
                                    onItemClickListener.onItemClick(models.get(position));
                                }
                            }
                        });
            }

            @Override
            public int getItemCount() {
                return models.size();
            }
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.tv_name);
            }

            public void update(Model model) {
                textView.setText(model.name);
            }
        }

        public interface OnItemClickListener {
            void onItemClick(Model model);
        }
    }

    private static class Model {
        String name;
        String url;
    }

    private interface ResultCallBack {
        void onResult(List<Model> list);
    }
}
