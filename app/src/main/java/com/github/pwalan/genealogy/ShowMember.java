package com.github.pwalan.genealogy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwalan.genealogy.utils.C;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成员信息
 */
public class ShowMember extends Activity {
    //获取数据
    protected static final int GET_DATA = 1;
    //修改成员信息
    protected static final int UPDATE=2;
    //删除成员
    protected static final int DELETE=3;

    private App app;
    private int id;
    private String name;
    private String gender="1";
    private JSONObject response, data;
    private List<Map<String, String>> listItems;

    //startActivityForResult需要的intent
    private Intent lastIntent ;

    //标题
    private ImageView titleLeftImv;
    private TextView titleTv;
    private ImageView img_up;
    PopupMenu popup = null;

    //显示
    private TextView tv_name,tv_birthday,tv_gender,tv_father,tv_mother,tv_partner;

    //修改
    private EditText et_birthday, et_father,et_mother, et_partner;
    private RadioGroup rg;
    private Button btn_save;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_member);

        id=getIntent().getIntExtra("id",0);
        lastIntent = getIntent();

        initView();

        getData();
    }

    /**
     * 初始化页面
     */
    private void initView() {
        app=(App)getApplication();

        //显示
        tv_name=(TextView)findViewById(R.id.tv_name);
        tv_birthday=(TextView)findViewById(R.id.tv_birthday);
        tv_gender=(TextView)findViewById(R.id.tv_gender);
        tv_father=(TextView)findViewById(R.id.tv_father);
        tv_mother=(TextView)findViewById(R.id.tv_mother);
        tv_partner=(TextView)findViewById(R.id.tv_partner);

        //修改
        et_birthday=(EditText)findViewById(R.id.et_birthday);
        et_father=(EditText)findViewById(R.id.et_father);
        et_mother=(EditText)findViewById(R.id.et_mother);
        et_partner=(EditText)findViewById(R.id.et_partner);
        rg=(RadioGroup)findViewById(R.id.rg);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==R.id.rb_1){
                    gender="1";
                }else if(i==R.id.rb_0){
                    gender="0";
                }
            }
        });
        btn_save=(Button)findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progressDialog == null) progressDialog = new ProgressDialog(ShowMember.this);
                progressDialog.setTitle("请稍后");
                progressDialog.setMessage("修改中...");
                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap map = new HashMap();
                        map.put("uid", app.getUid());
                        map.put("name", name);
                        map.put("birthday", et_birthday.getText().toString().trim());
                        map.put("gender", gender);
                        map.put("father", et_father.getText().toString().trim());
                        map.put("mother", et_mother.getText().toString().trim());
                        map.put("partner", et_partner.getText().toString().trim());
                        response = C.asyncPost(app.getServer() + "updateMember", map);
                        handler.sendEmptyMessage(UPDATE);
                    }
                }).start();
            }
        });

        //初始时隐藏修改的控件
        et_birthday.setVisibility(View.INVISIBLE);
        et_father.setVisibility(View.INVISIBLE);
        et_mother.setVisibility(View.INVISIBLE);
        et_partner.setVisibility(View.INVISIBLE);
        rg.setVisibility(View.INVISIBLE);
        btn_save.setVisibility(View.INVISIBLE);

        //标题
        titleLeftImv = (ImageView) findViewById(R.id.title_imv);
        titleLeftImv.setImageResource(R.drawable.exit);
        titleLeftImv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        titleTv = (TextView) findViewById(R.id.title_text_tv);
        titleTv.setText("成员信息");

        img_up = (ImageView) findViewById(R.id.img_up);
        img_up.setImageResource(R.drawable.menu);
        img_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup = new PopupMenu(ShowMember.this, v);
                getMenuInflater().inflate(R.menu.menu_show_member, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                                     @Override
                                                     public boolean onMenuItemClick(MenuItem item) {
                                                         switch (item.getItemId()) {
                                                             case R.id.update:
                                                                 popup.dismiss();
                                                                 //修改时隐藏显示控件并显示修改控件
                                                                 tv_birthday.setVisibility(View.INVISIBLE);
                                                                 tv_gender.setVisibility(View.INVISIBLE);
                                                                 tv_father.setVisibility(View.INVISIBLE);
                                                                 tv_mother.setVisibility(View.INVISIBLE);
                                                                 tv_partner.setVisibility(View.INVISIBLE);

                                                                 et_birthday.setVisibility(View.VISIBLE);
                                                                 et_birthday.setText(tv_birthday.getText().toString().trim());
                                                                 et_father.setVisibility(View.VISIBLE);
                                                                 et_father.setText(tv_father.getText().toString().trim());
                                                                 et_mother.setVisibility(View.VISIBLE);
                                                                 et_mother.setText(tv_mother.getText().toString().trim());
                                                                 et_partner.setVisibility(View.VISIBLE);
                                                                 et_partner.setText(tv_partner.getText().toString().trim());
                                                                 rg.setVisibility(View.VISIBLE);
                                                                 btn_save.setVisibility(View.VISIBLE);
                                                                 break;
                                                             case R.id.delete:
                                                                 popup.dismiss();
                                                                 if (progressDialog == null) progressDialog = new ProgressDialog(ShowMember.this);
                                                                 progressDialog.setTitle("请稍后");
                                                                 progressDialog.setMessage("删除中...");
                                                                 progressDialog.show();

                                                                 new Thread(new Runnable() {
                                                                     @Override
                                                                     public void run() {
                                                                         HashMap map = new HashMap();
                                                                         map.put("uid", app.getUid());
                                                                         map.put("name",name);
                                                                         response = C.asyncPost(app.getServer() + "deleteMember", map);
                                                                         handler.sendEmptyMessage(DELETE);
                                                                     }
                                                                 }).start();

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
    }

    /**
     * 获取用户的数据
     */
    private void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap map = new HashMap();
                map.put("id", id);
                response = C.asyncPost(app.getServer() + "getMember", map);
                handler.sendEmptyMessage(GET_DATA);
            }
        }).start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_DATA:
                    try {
                        data=new JSONObject(response.get("data").toString());
                        name=data.getString("name");
                        tv_name.setText(name);
                        tv_birthday.setText(data.getString("birthday"));
                        if(data.getString("gender").equals("1")){
                            tv_gender.setText("男");
                        }else{
                            tv_gender.setText("女");
                        }
                        tv_father.setText(data.getString("father"));
                        tv_mother.setText(data.getString("mother"));
                        tv_partner.setText(data.getString("partner"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case UPDATE:
                    //取消进度框
                    if(progressDialog!=null) progressDialog.dismiss();
                    try {
                        String result=response.getString("data");
                        if(result.equals("updated")){
                            Toast.makeText(ShowMember.this,"更新完成！",Toast.LENGTH_SHORT).show();

                            et_birthday.setVisibility(View.INVISIBLE);
                            et_father.setVisibility(View.INVISIBLE);
                            et_mother.setVisibility(View.INVISIBLE);
                            et_partner.setVisibility(View.INVISIBLE);
                            rg.setVisibility(View.INVISIBLE);
                            btn_save.setVisibility(View.INVISIBLE);

                            tv_birthday.setVisibility(View.VISIBLE);
                            tv_birthday.setText(et_birthday.getText().toString().trim());
                            tv_gender.setVisibility(View.VISIBLE);
                            if(gender.equals("1")){
                                tv_gender.setText("男");
                            }else{
                                tv_gender.setText("女");
                            }
                            tv_father.setVisibility(View.VISIBLE);
                            tv_father.setText(et_father.getText().toString().trim());
                            tv_mother.setVisibility(View.VISIBLE);
                            tv_mother.setText(et_mother.getText().toString().trim());
                            tv_partner.setVisibility(View.VISIBLE);
                            tv_partner.setText(et_partner.getText().toString().trim());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //设置结果
                    setResult(Activity.RESULT_OK, lastIntent);
                    break;
                case DELETE:
                    //取消进度框
                    if(progressDialog!=null) progressDialog.dismiss();
                    try {
                        String result=response.getString("data");
                        if(result.equals("deleted")){
                            Toast.makeText(ShowMember.this,"已删除！",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //设置结果
                    setResult(Activity.RESULT_OK, lastIntent);
                    break;
                default:
                    break;
            }
        }
    };

}
