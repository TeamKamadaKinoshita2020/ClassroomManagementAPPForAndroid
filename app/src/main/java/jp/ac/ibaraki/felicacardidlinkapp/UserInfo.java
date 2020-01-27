package jp.ac.ibaraki.felicacardidlinkapp;

/**
 * ログインしたユーザの情報を保有するためのクラス
 * Created by 大学用 on 2017/11/10.
 */

public class UserInfo{
    private String name;
    private String position;
    private String auth;

    //コンストラクタ
    public UserInfo(String name,String position,String auth){
        this.name = name;
        this.position = position;
        this.auth = auth;
    }

    public String getName(){
        return this.name;
    }

    public  String getPosition(){
        return this.position;
    }

    public  String getAuth(){
        return  this.auth;
    }
}
