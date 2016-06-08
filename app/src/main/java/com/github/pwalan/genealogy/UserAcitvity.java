package com.github.pwalan.genealogy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.pwalan.genealogy.myview.RoundImageView;
import com.github.pwalan.genealogy.utils.C;
import com.github.pwalan.genealogy.utils.QCloud;
import com.github.pwalan.genealogy.utils.SelectPicActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;


/**
 * 用户登录/注册
 */
public class UserAcitvity extends Activity implements View.OnClickListener {

    protected static final int LOGIN = 1;
    protected static final int REGISTER = 2;
    protected static final int UPLOAD = 3;

    private ProgressDialog progressDialog;

    //startActivityForResult需要的intent
    private Intent lastIntent ;

    private String passwd;
    private String passwdconf;
    private String picPath=null;
    //是否正在注册，用来决定头像是否可更换
    private Boolean registering;

    private EditText et_username;
    private EditText et_passwd;
    private EditText et_passwdconf;
    private RoundImageView img_head;

    private Button btn_login;
    private Button btn_register;
    private Button btn_toregister;

    private App app;
    private JSONObject response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        app=(App)getApplication();

        lastIntent = getIntent();

        //腾讯云上传初始化
        QCloud.init(this);

        registering=false;
        et_username = (EditText) findViewById(R.id.et_username);
        et_username.requestFocus();
        et_passwd = (EditText) findViewById(R.id.et_passwd);
        et_passwdconf = (EditText) findViewById(R.id.et_passwdconf);
        img_head = (RoundImageView) findViewById(R.id.img_head);
        img_head.setOnClickListener(this);

        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
        btn_register = (Button) findViewById(R.id.btn_register);
        btn_register.setOnClickListener(this);
        btn_toregister = (Button) findViewById(R.id.btn_toregister);
        btn_toregister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                //登录
                app.setUsername(et_username.getText().toString().trim());
                passwd=et_passwd.getText().toString().trim();

                if(progressDialog==null) progressDialog=new ProgressDialog(this);
                progressDialog.setTitle("请稍后");
                progressDialog.setMessage("登录中...");
                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap map=new HashMap();
                        map.put("username",app.getUsername());
                        map.put("passwd",passwd);
                        response= C.asyncPost(app.getServer()+"login",map);
                        handler.sendEmptyMessage(LOGIN);
                    }
                }).start();
                break;

            case R.id.btn_toregister:
                //点击去注册后此按钮和登录按钮隐藏，确认密码和注册显示
                registering=true;
                btn_toregister.setVisibility(View.INVISIBLE);
                btn_login.setVisibility(View.INVISIBLE);
                et_passwdconf.setVisibility(View.VISIBLE);
                btn_register.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_register:
                //注册
                app.setUsername(et_username.getText().toString().trim());
                passwd=et_passwd.getText().toString().trim();
                passwdconf=et_passwdconf.getText().toString().trim();
                app.setHeadurl(QCloud.resultUrl);

                if(passwd.equals(passwdconf)){
                    if(progressDialog==null) progressDialog=new ProgressDialog(this);
                    progressDialog.setTitle("请稍后");
                    progressDialog.setMessage("注册中...");
                    progressDialog.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HashMap map=new HashMap();
                            map.put("username",app.getUsername());
                            map.put("passwd",passwd);
                            map.put("head",app.getHeadurl());
                            response= C.asyncPost(app.getServer() + "register", map);
                            Log.i("register_response", response.toString());
                            handler.sendEmptyMessage(REGISTER);
                        }
                    }).start();
                }else{
                    Toast.makeText(this, "两次密码不一致，请重新输入！", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.img_head:
                //上传头像,仅限注册时
                if(registering){
                    Intent intent = new Intent(this,SelectPicActivity.class);
                    startActivityForResult(intent, 0);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode== Activity.RESULT_OK && requestCode == 0)
        {
            picPath = data.getStringExtra(SelectPicActivity.KEY_PHOTO_PATH);
            Log.i("dfood", "最终选择的图片=" + picPath);
            Bitmap bm = BitmapFactory.decodeFile(picPath);
            img_head.setImageBitmap(bm);
            handler.sendEmptyMessage(UPLOAD);

            //更新图库

            Uri localUri = Uri.fromFile(new File(picPath));
            Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
            sendBroadcast(localIntent);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN:
                    //取消进度框
                    if(progressDialog!=null) progressDialog.dismiss();

                    try {
                        String status=response.getString("status");
                        if(status.equals("succeed")){
                            Toast.makeText(UserAcitvity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                            app.setUid(response.getInt("uid"));
                            app.setHeadurl(response.getString("head"));
                            app.setIsLogin(true);
                            //设置结果
                            setResult(Activity.RESULT_OK, lastIntent);
                            finish();
                        }else{
                            et_passwd.setText("");
                            Toast.makeText(UserAcitvity.this, "账号验证失败，请重试！", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case REGISTER:
                    //取消进度框
                    if(progressDialog!=null) progressDialog.dismiss();

                    try {
                        String status=response.getString("status");
                        if(status.equals("succeed")){
                            Toast.makeText(UserAcitvity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                            //注册成功后注册按钮、确认密码隐藏，登录、去注册显示
                            btn_toregister.setVisibility(View.VISIBLE);
                            btn_login.setVisibility(View.VISIBLE);
                            et_passwdconf.setVisibility(View.INVISIBLE);
                            btn_register.setVisibility(View.INVISIBLE);
                            registering=false;
                        }else{
                            Toast.makeText(UserAcitvity.this, "注册失败，用户名已存在！", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case UPLOAD:
                    if(picPath!=null)
                    {
                        QCloud.UploadPic(picPath, UserAcitvity.this);
                    }else{
                        Toast.makeText(UserAcitvity.this, "上传的文件路径出错", Toast.LENGTH_SHORT).show();
                    }
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

}
