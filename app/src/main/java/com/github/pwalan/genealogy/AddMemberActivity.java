package com.github.pwalan.genealogy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.pwalan.genealogy.utils.C;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class AddMemberActivity extends Activity {
    //添加完成
    protected static final int ADD=1;

    private EditText et_name, et_birthday, et_father,et_mother, et_partner;
    private RadioGroup rg;
    private Button btn_save;
    private String gender="1";

    private App app;

    private JSONObject response;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        app=(App)getApplication();

        et_name=(EditText)findViewById(R.id.et_name);
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
                if(progressDialog==null) progressDialog=new ProgressDialog(AddMemberActivity.this);
                progressDialog.setTitle("请稍后");
                progressDialog.setMessage("上传中...");
                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap map=new HashMap();
                        map.put("uid",app.getUid());
                        map.put("name",et_name.getText().toString().trim());
                        map.put("birthday",et_birthday.getText().toString().trim());
                        map.put("gender",gender);
                        map.put("father",et_father.getText().toString().trim());
                        map.put("mother",et_mother.getText().toString().trim());
                        map.put("partner",et_partner.getText().toString().trim());
                        response= C.asyncPost(app.getServer()+"addMember",map);
                        handler.sendEmptyMessage(ADD);
                    }
                }).start();
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADD:
                    try {
                        String data = response.get("data").toString();
                        //取消进度框
                        if(progressDialog!=null) progressDialog.dismiss();

                        if(data.equals("add")){
                            Toast.makeText(AddMemberActivity.this,"添加成功！",Toast.LENGTH_SHORT).show();
                            finish();
                        }else{
                            Toast.makeText(AddMemberActivity.this,"添加失败！",Toast.LENGTH_SHORT).show();
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

}
