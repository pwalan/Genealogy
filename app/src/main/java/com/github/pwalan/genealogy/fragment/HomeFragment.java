package com.github.pwalan.genealogy.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.github.pwalan.genealogy.App;
import com.github.pwalan.genealogy.R;
import com.github.pwalan.genealogy.ShowGenealogyActivity;
import com.github.pwalan.genealogy.myview.RefreshableView;
import com.github.pwalan.genealogy.utils.C;
import com.github.pwalan.genealogy.utils.ListViewBinder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    //获取数据
    protected static final int GET_DATA = 1;
    //获取头像
    protected static final int GET_HEAD=2;

    private App app;
    private JSONObject response;
    private JSONArray data;
    private int count;
    private Bitmap bitmap;

    RefreshableView refreshableView;

    private ListView glist;
    private ArrayList<Integer> uids;
    private List<Map<String, Object>> listItems;
    SimpleAdapter simpleAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member, container, false);

        app = (App) getActivity().getApplication();
        glist = (ListView) view.findViewById(R.id.mlist);
        refreshableView = (RefreshableView) view.findViewById(R.id.refreshable_view);
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
                refreshableView.finishRefreshing();
            }
        }, 0);

        getData();

        return view;
    }

    /**
     * 获取数据
     */
    public void getData() {
        count=0;
        listItems = new ArrayList<Map<String, Object>>();
        uids = new ArrayList<Integer>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap map = new HashMap();
                response = C.asyncPost(app.getServer() + "getPublishList", map);
                handler.sendEmptyMessage(GET_DATA);
            }
        }).start();
    }

    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_DATA:
                    try {
                        data = new JSONArray(response.get("data").toString());
                        JSONObject jo = data.getJSONObject(count);
                        getHttpBitmap(jo.getString("head"),GET_HEAD);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case GET_HEAD:
                    Map<String, Object> listItem = new HashMap<String, Object>();
                    try {
                        JSONObject jo = data.getJSONObject(count);
                        listItem.put("username",jo.getString("username")+" 的家谱");
                        listItem.put("head", bitmap);
                        listItems.add(listItem);
                        uids.add(jo.getInt("uid"));
                        count++;
                        if(count==data.length()){
                            simpleAdapter = new SimpleAdapter(getActivity(), listItems, R.layout.item_glist,
                                    new String[]{"head","username"},
                                    new int[]{R.id.iv_head,R.id.tv_username});
                            simpleAdapter.setViewBinder(new ListViewBinder());
                            glist.setAdapter(simpleAdapter);
                            glist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent intent=new Intent(getActivity(), ShowGenealogyActivity.class);
                                    intent.putExtra("uid",uids.get(i));
                                    startActivity(intent);
                                }
                            });
                        }else{
                            jo = data.getJSONObject(count);
                            getHttpBitmap(jo.getString("head"),GET_HEAD);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void getHttpBitmap(final String url, final int msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL myFileURL = new URL(url);
                    //获得连接
                    HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
                    //设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
                    conn.setConnectTimeout(6000);
                    //连接设置获得数据流
                    conn.setDoInput(true);
                    conn.connect();
                    //得到数据流
                    InputStream is = conn.getInputStream();
                    //解析得到图片
                    bitmap = BitmapFactory.decodeStream(is);
                    //关闭数据流
                    is.close();
                    handler.sendEmptyMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
