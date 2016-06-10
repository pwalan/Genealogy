package com.github.pwalan.genealogy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.github.pwalan.genealogy.utils.C;
import com.github.pwalan.genealogy.utils.Member;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShowGenealogyActivity extends Activity {
    //获取数据
    protected static final int GET_DATA = 1;

    private App app;
    private int uid;
    private JSONObject response;
    private JSONArray data;
    private ArrayList<Member> members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_show_genealogy);

        uid = getIntent().getIntExtra("uid", 0);
        app = (App) getApplication();

        members = new ArrayList<Member>();

        getData();

    }

    /**
     * 获取用户的数据
     */
    private void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap map = new HashMap();
                map.put("uid", uid);
                response = C.asyncPost(app.getServer() + "getUserMembers", map);
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
                        data = new JSONArray(response.get("data").toString());
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jo = data.getJSONObject(i);
                            Member member = new Member(jo.getString("name"), jo.getString("gender"), jo.getString("partner"),
                                    jo.getString("father"), jo.getString("mother"), 0, 0);
                            members.add(member);
                        }
                        setContentView(new MyView(ShowGenealogyActivity.this));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    class MyView extends View {
        public MyView(Context context) {
            super(context);
        }

        @Override
        // 重写该方法，进行绘图
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // 把整张画布绘制成白色
            canvas.drawColor(Color.WHITE);
            Paint paint = new Paint();
            // 去锯齿
            paint.setAntiAlias(true);
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);

            // ----------设置字符大小后绘制----------
            paint.setTextSize(30);
            paint.setColor(Color.BLACK);
            paint.setShader(null);
            // 定义一个Path对象
            Path path = new Path();

            //处理成员信息,先确定成员放置的位置，再画关系线
            if (members.size() > 0) {
                members.get(0).setX(50);
                members.get(0).setY(50);
                int x = 0;
                int y = 0;
                int current=1;
                Member member = new Member();
                for (int i = 1; i < members.size(); i++) {
                    member = members.get(i);
                    for (int j = 0; j < i; j++) {
                        if (members.get(j).getGender().equals("1")) {
                            x = members.get(j).getX();
                            y = members.get(j).getY();
                        }
                        //如果第i个成员的伴侣在前面有了，就可以将其放在同一行
                        if (member.getPartner().equals(members.get(j).getName())) {
                            member.setX(members.get(j).getX() + 150);
                            member.setY(members.get(j).getY());
                            break;  //跳出此次循环
                        } else if (member.getFather().equals(members.get(j).getName())) {
                            //如果第i个成员的父母亲在前面有了，就可以将其放在下一行
                            member.setX(members.get(j).getX() + 80);
                            member.setY(members.get(j).getY() + 150);
                            break;  //跳出此次循环
                        } else if (member.getX() == 0) {
                            //否则还是放在同一行
                            member.setX(x + 250);
                            member.setY(y);
                        }
                    }
                }
            }

            // 绘制字符串
            for (int i = 0; i < members.size(); i++) {
                canvas.drawText(members.get(i).getName(), members.get(i).getX(), members.get(i).getY(), paint);
            }

            //绘制关系线
            for (int i = 0; i < members.size(); i++) {
                //伴侣
                if (members.get(i).getGender().equals("1")) {
                    for (int j = 0; j < members.size(); j++) {
                        if (members.get(i).getPartner().equals(members.get(j).getName())) {
                            path.moveTo(members.get(i).getX() + 80, members.get(i).getY() - 14);
                            path.lineTo(members.get(j).getX() - 10, members.get(j).getY() - 14);
                            canvas.drawPath(path, paint);
                            break;
                        }
                    }
                }
                //父母孩子
                for (int j = 0; j < members.size(); j++) {
                    if (members.get(i).getFather().equals(members.get(j).getName())) {
                        path.moveTo(members.get(i).getX() + 30, members.get(i).getY() - 35);
                        path.lineTo(members.get(j).getX() + 110, members.get(j).getY() - 14);
                        canvas.drawPath(path, paint);
                        break;
                    }
                }
            }

        }

    }

}
