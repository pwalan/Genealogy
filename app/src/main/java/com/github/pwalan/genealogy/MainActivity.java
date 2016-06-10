package com.github.pwalan.genealogy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwalan.genealogy.fragment.HomeFragment;
import com.github.pwalan.genealogy.fragment.MemberFragment;
import com.github.pwalan.genealogy.fragment.MineFragment;
import com.github.pwalan.genealogy.utils.C;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends FragmentActivity implements View.OnClickListener {
    //获取数据
    protected static final int PUBLISH = 1;

    private App app;
    private JSONObject response;

    PopupMenu popup = null;

    // 初始化顶部栏显示
    private ImageView titleLeftImv;
    private TextView titleTv;
    private ImageView img_up;
    // 定义3个Fragment对象
    private HomeFragment fg1;
    private MemberFragment fg2;
    private MineFragment fg3;
    // 帧布局对象，用来存放Fragment对象
    private FrameLayout frameLayout;
    // 定义每个选项中的相关控件
    private RelativeLayout firstLayout;
    private RelativeLayout secondLayout;
    private RelativeLayout thirdLayout;
    private ImageView firstImage;
    private ImageView secondImage;
    private ImageView thirdImage;
    private TextView firstText;
    private TextView secondText;
    private TextView thirdText;
    // 定义几个颜色
    private int whirt = 0xFFFFFFFF;
    private int gray = 0xFF66CCFF;
    private int dark = 0xff000000;
    // 定义FragmentManager对象管理器
    private FragmentManager fragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        initView(); // 初始化界面控件
        setChioceItem(0); // 初始化页面加载时显示第一个选项卡
    }

    /**
     * 初始化页面
     */
    private void initView() {
        app=(App)getApplication();
        // 初始化页面标题栏
        titleLeftImv = (ImageView) findViewById(R.id.title_imv);
        titleLeftImv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChioceItem(2);
            }
        });
        //顶部右侧的加号图标点击添加成员
        img_up=(ImageView)findViewById(R.id.img_up);
        img_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup = new PopupMenu(MainActivity.this, v);
                getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                                     @Override
                                                     public boolean onMenuItemClick(MenuItem item) {
                                                         switch (item.getItemId()) {
                                                             case R.id.hand:
                                                                 popup.dismiss();
                                                                 if(app.isLogin()){
                                                                     Intent intent=new Intent(MainActivity.this,AddMemberActivity.class);
                                                                     startActivity(intent);
                                                                 }else{
                                                                     Toast.makeText(MainActivity.this,"要添加请先登录",Toast.LENGTH_SHORT).show();
                                                                 }
                                                                 break;
                                                             case R.id.address:
                                                                 popup.dismiss();
                                                                 if(app.isLogin()){
                                                                     Toast.makeText(MainActivity.this,"从通讯录中导入",Toast.LENGTH_SHORT).show();
                                                                 }else{
                                                                     Toast.makeText(MainActivity.this,"要导入请先登录",Toast.LENGTH_SHORT).show();
                                                                 }
                                                                 break;
                                                             case R.id.qq:
                                                                 popup.dismiss();
                                                                 if(app.isLogin()){
                                                                     Toast.makeText(MainActivity.this,"从通qq/微信中导入",Toast.LENGTH_SHORT).show();
                                                                 }else{
                                                                     Toast.makeText(MainActivity.this,"要导入请先登录",Toast.LENGTH_SHORT).show();
                                                                 }
                                                                 break;
                                                             case R.id.publish:
                                                                 popup.dismiss();
                                                                 if(app.isLogin()){
                                                                     updatePublish();
                                                                 }else{
                                                                     Toast.makeText(MainActivity.this,"要公开请先登录",Toast.LENGTH_SHORT).show();
                                                                 }
                                                                 break;
                                                             default:
                                                                 break;
                                                         }
                                                         return true;
                                                     }
                                                 }
                );
                popup.show();
            }
        });
        //顶部标签
        titleTv = (TextView) findViewById(R.id.title_text_tv);
        titleTv.setText("首 页");
        // 初始化底部导航栏的控件
        firstImage = (ImageView) findViewById(R.id.first_image);
        secondImage = (ImageView) findViewById(R.id.second_image);
        thirdImage = (ImageView) findViewById(R.id.third_image);
        firstText = (TextView) findViewById(R.id.first_text);
        secondText = (TextView) findViewById(R.id.second_text);
        thirdText = (TextView) findViewById(R.id.third_text);
        firstLayout = (RelativeLayout) findViewById(R.id.first_layout);
        secondLayout = (RelativeLayout) findViewById(R.id.second_layout);
        thirdLayout = (RelativeLayout) findViewById(R.id.third_layout);
        firstLayout.setOnClickListener(MainActivity.this);
        secondLayout.setOnClickListener(MainActivity.this);
        thirdLayout.setOnClickListener(MainActivity.this);
    }

    /**
     * 更新用户是否公开家谱
     */
    private void updatePublish(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap map = new HashMap();
                map.put("uid", app.getUid());
                response = C.asyncPost(app.getServer() + "updatePublish", map);
                handler.sendEmptyMessage(PUBLISH);
            }
        }).start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PUBLISH:
                    try {
                        String data=response.getString("data");
                        if(data.equals("publish")){
                            Toast.makeText(MainActivity.this,"已公开！",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this,"已取消公开！",Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.first_layout:
                setChioceItem(0);
                break;
            case R.id.second_layout:
                setChioceItem(1);
                break;
            case R.id.third_layout:
                setChioceItem(2);
                break;
            default:
                break;
        }
    }

    /**
     * 设置点击选项卡的事件处理
     *
     * @param index 选项卡的标号：0, 1, 2
     */
    private void setChioceItem(int index) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        clearChioce(); // 清空, 重置选项, 隐藏所有Fragment
        hideFragments(fragmentTransaction);
        switch (index) {
            case 0:
                // firstImage.setImageResource(R.drawable.XXXX); 需要的话自行修改
                firstText.setTextColor(dark);
                firstLayout.setBackgroundColor(gray);
                titleTv.setText(R.string.fg1_name);
                // 如果fg1为空，则创建一个并添加到界面上
                if (fg1 == null) {
                    fg1 = new HomeFragment();
                    fragmentTransaction.add(R.id.content, fg1);
                } else {
                    // 如果不为空，则直接将它显示出来
                    fragmentTransaction.show(fg1);
                }
                break;
            case 1:
                // secondImage.setImageResource(R.drawable.XXXX);
                secondText.setTextColor(dark);
                secondLayout.setBackgroundColor(gray);
                titleTv.setText(R.string.fg2_name);
                if (fg2 == null) {
                    fg2 = new MemberFragment();
                    fragmentTransaction.add(R.id.content, fg2);
                } else {
                    fragmentTransaction.show(fg2);
                }
                break;
            case 2:
                // thirdImage.setImageResource(R.drawable.XXXX);
                thirdText.setTextColor(dark);
                thirdLayout.setBackgroundColor(gray);
                titleTv.setText(R.string.fg3_name);
                if (fg3 == null) {
                    fg3 = new MineFragment();
                    fragmentTransaction.add(R.id.content, fg3);
                } else {
                    fragmentTransaction.show(fg3);
                }
                break;
        }
        fragmentTransaction.commit(); // 提交
    }

    /**
     * 当选中其中一个选项卡时，其他选项卡重置为默认
     */
    private void clearChioce() {
        // firstImage.setImageResource(R.drawable.XXX);
        firstText.setTextColor(gray);
        firstLayout.setBackgroundColor(whirt);
        // secondImage.setImageResource(R.drawable.XXX);
        secondText.setTextColor(gray);
        secondLayout.setBackgroundColor(whirt);
        // thirdImage.setImageResource(R.drawable.XXX);
        thirdText.setTextColor(gray);
        thirdLayout.setBackgroundColor(whirt);
    }

    /**
     * 隐藏Fragment
     *
     * @param fragmentTransaction
     */
    private void hideFragments(FragmentTransaction fragmentTransaction) {
        if (fg1 != null) {
            fragmentTransaction.hide(fg1);
        }
        if (fg2 != null) {
            fragmentTransaction.hide(fg2);
        }
        if (fg3 != null) {
            fragmentTransaction.hide(fg3);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click();
        }
        return false;
    }

    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true;
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }
}
