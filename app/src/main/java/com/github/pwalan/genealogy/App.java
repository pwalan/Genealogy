package com.github.pwalan.genealogy;

import android.app.Application;

public class App extends Application {
    private boolean isLogin;
    private String username;
    private int uid;
    private String headurl;
    private String server;

    @Override
    public void onCreate() {
        //对两个值进行初始化
        isLogin = false;
        username="";
        uid =0;
        headurl="http://pwalan-10035979.image.myqcloud.com/test_fileId_3119a3d1-b799-4400-b65e-48c92ba7aebd";
        server="http://pwalan.cn/GenealogyServer/";
        super.onCreate();
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setIsLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getHeadurl() {
        return headurl;
    }

    public void setHeadurl(String headurl) {
        this.headurl = headurl;
    }
}
