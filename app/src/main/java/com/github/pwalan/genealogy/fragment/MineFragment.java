package com.github.pwalan.genealogy.fragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwalan.genealogy.App;
import com.github.pwalan.genealogy.R;
import com.github.pwalan.genealogy.ShowGenealogyActivity;
import com.github.pwalan.genealogy.UserAcitvity;
import com.github.pwalan.genealogy.myview.RoundImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MineFragment extends Fragment {
    //下载头像
    protected static final int DOWNLOAD_FILE_DONE = 1;

    private App app;
    private Bitmap bitmap;
    private LinearLayout ll_login;
    private RoundImageView img_head;
    private TextView tv_name;
    private Button btn_show,btn_exit,btn_aboutus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);

        app=(App)getActivity().getApplication();
        img_head=(RoundImageView)view.findViewById(R.id.img_head);
        tv_name=(TextView)view.findViewById(R.id.tv_name);

        ll_login=(LinearLayout)view.findViewById(R.id.ll_login);
        ll_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(app.isLogin()){
                    Toast.makeText(getActivity(),"已登录！",Toast.LENGTH_SHORT).show();
                }else{
                    startActivityForResult(new Intent(getActivity(), UserAcitvity.class), 0);
                }
            }
        });

        btn_show=(Button)view.findViewById(R.id.btn_show);
        btn_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(app.isLogin()){
                    Intent intent=new Intent(getActivity(), ShowGenealogyActivity.class);
                    intent.putExtra("uid",app.getUid());
                    startActivity(intent);
                }else{
                    Toast.makeText(getActivity(),"请登录！",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_exit=(Button)view.findViewById(R.id.btn_exit);
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
                System.exit(0);
            }
        });

        btn_aboutus=(Button)view.findViewById(R.id.btn_aboutus);
        btn_aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"关于软件",Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode== Activity.RESULT_OK && requestCode == 0)
        {
            if(app.isLogin()){
                tv_name.setText(app.getUsername());
                if(app.getHeadurl()!=null){
                    getHttpBitmap(app.getHeadurl());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 获取网落图片资源
     * @param url
     */
    public void getHttpBitmap(final String url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    URL myFileURL = new URL(url);
                    //获得连接
                    HttpURLConnection conn=(HttpURLConnection)myFileURL.openConnection();
                    //设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
                    conn.setConnectTimeout(6000);
                    //连接设置获得数据流
                    conn.setDoInput(true);
                    //不使用缓存
                    conn.setUseCaches(false);
                    //这句可有可无，没有影响
                    //conn.connect();
                    //得到数据流
                    InputStream is = conn.getInputStream();
                    //解析得到图片
                    bitmap = BitmapFactory.decodeStream(is);
                    //关闭数据流
                    is.close();
                    handler.sendEmptyMessage(DOWNLOAD_FILE_DONE);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case DOWNLOAD_FILE_DONE:
                    img_head.setImageBitmap(bitmap);
                    Log.i("main","获取首页头像");
                    break;
                default:
                    break;
            }
        }
    };
}
