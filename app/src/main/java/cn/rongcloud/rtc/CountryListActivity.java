package cn.rongcloud.rtc;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.entity.CountryInfo;
import cn.rongcloud.rtc.media.http.HttpClient;
import cn.rongcloud.rtc.media.http.Request;
import cn.rongcloud.rtc.media.http.RequestMethod;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.UserUtils;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import me.yokeyword.indexablerv.IndexableAdapter;
import me.yokeyword.indexablerv.IndexableLayout;
import org.json.JSONArray;
import org.json.JSONObject;

/** Created by wangw on 2019/4/8. */
public class CountryListActivity extends RongRTCBaseActivity implements View.OnClickListener {

    private EditText mEdSearch;
    private IndexableLayout mIndexableLayout;
    private CountryAdapter mAdapter;
    private List<CountryInfo> mCountrys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country);
        initViews();
        getCountryList();
    }

    private void initViews() {
        mEdSearch = (EditText) findViewById(R.id.et_search);
        mIndexableLayout = (IndexableLayout) findViewById(R.id.countrylist);
        mIndexableLayout.setOverlayStyle_Center();
        mAdapter = new CountryAdapter();
        mAdapter.setOnItemContentClickListener(
                new IndexableAdapter.OnItemContentClickListener<CountryInfo>() {
                    @Override
                    public void onItemClick(
                            View v, int originalPosition, int currentPosition, CountryInfo entity) {
                        if (entity == null) return;
                        SessionManager.getInstance()
                                .put(UserUtils.COUNTRY, new Gson().toJson(entity));
                        finish();
                    }
                });
        mIndexableLayout.setLayoutManager(new LinearLayoutManager(this));
        mIndexableLayout.setAdapter(mAdapter);
        findViewById(R.id.tv_back).setOnClickListener(this);
        mEdSearch.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        onSearch(mEdSearch.getText().toString().trim());
                    }
                });
    }

    private void onSearch(String str) {
        if (TextUtils.isEmpty(str)) {
            setData(mCountrys);
            return;
        }
        if (mCountrys == null || mCountrys.isEmpty()) return;
        ArrayList<CountryInfo> list = new ArrayList<>();
        for (CountryInfo country : mCountrys) {
            String field = country.getFieldIndexBy();
            if (TextUtils.isEmpty(field)) continue;
            if (field.contains(str)
                    || (!TextUtils.isEmpty(country.pinyin) && country.pinyin.contains(str)))
                list.add(country);
        }
        setData(list);
    }

    /** 获取国家地区数据 */
    private void getCountryList() {
        Request.Builder request = new Request.Builder();
        request.url(UserUtils.URL_GET_COUNTRY);
        request.method(RequestMethod.GET);
        HttpClient.getDefault()
                .request(
                        request.build(),
                        new HttpClient.ResultCallback() {
                            @Override
                            public void onResponse(String result) {
                                mCountrys = onParseData(result);
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                if (mIndexableLayout != null
                                                        && !isFinishing()
                                                        && !isDestroyed()) {
                                                    setData(mCountrys);
                                                }
                                            }
                                        });
                            }

                            @Override
                            public void onFailure(int errorCode) {
                                postShowToast("onFailure:" + errorCode);
                            }
                        });
    }

    private void setData(List<CountryInfo> countrys) {
        if (countrys == null) {
            mAdapter.setDatas(new ArrayList<CountryInfo>());
        } else {
            mAdapter.setDatas(countrys);
        }
    }

    private List<CountryInfo> onParseData(String json) {
        if (TextUtils.isEmpty(json)) {
            showToast("json is Null");
            return null;
        }
        JSONObject jObj = null;
        ArrayList<CountryInfo> countrys = new ArrayList<>();
        try {
            jObj = new JSONObject(json);
            JSONArray result = jObj.optJSONArray("result");
            int length = result.length();
            for (int i = 0; i < length; i++) {
                JSONObject obj = result.getJSONObject(i);
                CountryInfo country = new CountryInfo(obj.optString("region"));
                JSONObject locale = obj.optJSONObject("locale");
                if (locale == null || TextUtils.isEmpty(country.region)) continue;
                country.en = locale.optString("en");
                country.zh = locale.optString("zh");
                countrys.add(country);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return countrys;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                finish();
                break;
        }
    }

    class CountryAdapter extends IndexableAdapter<CountryInfo> {

        @Override
        public RecyclerView.ViewHolder onCreateTitleViewHolder(ViewGroup parent) {
            return new TitleVH(
                    LayoutInflater.from(CountryListActivity.this)
                            .inflate(R.layout.item_country_title, parent, false));
        }

        @Override
        public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent) {
            return new ContentVH(
                    LayoutInflater.from(CountryListActivity.this)
                            .inflate(R.layout.item_country_content, parent, false));
        }

        @Override
        public void onBindTitleViewHolder(RecyclerView.ViewHolder holder, String indexTitle) {
            ((TitleVH) holder).mTvTitle.setText(Html.fromHtml(indexTitle));
        }

        @Override
        public void onBindContentViewHolder(RecyclerView.ViewHolder holder, CountryInfo entity) {
            ContentVH vh = (ContentVH) holder;
            vh.mTvName.setText(entity.getFieldIndexBy());
            vh.mTvRegion.setText("+" + entity.region);
        }
    }

    static class ContentVH extends RecyclerView.ViewHolder {

        public TextView mTvName;
        public TextView mTvRegion;

        public ContentVH(View itemView) {
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            mTvRegion = (TextView) itemView.findViewById(R.id.tv_region);
        }
    }

    static class TitleVH extends RecyclerView.ViewHolder {

        public TextView mTvTitle;

        public TitleVH(View itemView) {
            super(itemView);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_title);
        }
    }
}
