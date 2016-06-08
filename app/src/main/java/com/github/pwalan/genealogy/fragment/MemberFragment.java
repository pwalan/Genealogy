package com.github.pwalan.genealogy.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.github.pwalan.genealogy.App;
import com.github.pwalan.genealogy.R;
import com.github.pwalan.genealogy.ShowMember;
import com.github.pwalan.genealogy.myview.RefreshableView;
import com.github.pwalan.genealogy.utils.C;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberFragment extends Fragment {
    //获取数据
    protected static final int GET_DATA = 1;

    private App app;
    private JSONObject response;
    private JSONArray data;

    RefreshableView refreshableView;

    private ListView mlist;
    private ArrayList<Integer> ids;
    private List<Map<String, String>> listItems;
    SimpleAdapter simpleAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member, container, false);

        app = (App) getActivity().getApplication();
        mlist = (ListView) view.findViewById(R.id.mlist);
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
        listItems = new ArrayList<Map<String, String>>();
        ids = new ArrayList<Integer>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap map = new HashMap();
                map.put("uid", app.getUid());
                response = C.asyncPost(app.getServer() + "getMemberlist", map);
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
                        for (int i = 0; i < data.length(); i++) {
                            Map<String, String> listItem = new HashMap<String, String>();
                            JSONObject jo = data.getJSONObject(i);
                            ids.add(jo.getInt("id"));
                            listItem.put("name", jo.getString("name"));
                            listItem.put("age", jo.getString("age"));
                            listItems.add(listItem);
                        }
                        simpleAdapter = new SimpleAdapter(getActivity(), listItems, R.layout.item_member,
                                new String[]{"name", "age"},
                                new int[]{R.id.tv_name, R.id.tv_age});
                        mlist.setAdapter(simpleAdapter);
                        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Toast.makeText(getActivity(), listItems.get(i).get("name").toString(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), ShowMember.class);
                                intent.putExtra("id", ids.get(i));
                                startActivityForResult(intent, 0);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            getData();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
