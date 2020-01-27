package jp.ac.ibaraki.felicacardidlinkapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * アプリ起動時に教室管理権限を持つユーザがログインを行うアクティビティ
 * データベース内にあるユーザ情報をphp経由で呼び出してログイン判定を行う
 * @author ShinyaKinoshita
 */
public class MainActivity extends AppCompatActivity {//} implements OnClickListener{

    public static final String LOGIN_URL = "login.php";

    public static final String LOGIN_SUCCESS = "1";
    public static final String LOGIN_FAILED = "0";
    public static final String AUTH_ERROR = "-1";

    public static final String  ADMINISTER = "0";
    public static final String AFFAIRS = "1";
    public static final String TEACHER = "2";


    private String[] sendValue;
    private String JSON;
    private HashMap<String ,String> convertJSON = new HashMap<String ,String>();
    private WebConnection webConnection;
    private Intent intent;

    private EditText userId;
    private EditText pass;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //リソースにアクセス
        userId = (EditText) findViewById(R.id.editText1);
        pass = (EditText) findViewById(R.id.editText2);
        loginButton = (Button) findViewById(R.id.button);

        //ログイン処理
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // クリック時の処理
                Log.d("id",userId.getText().toString());
                Log.d("pass",pass.getText().toString());

                sendValue = new String[2];
                sendValue[0] = String.valueOf(userId.getText().toString());
                sendValue[1] = pass.getText().toString();
                webConnection = new WebConnection(sendValue,LOGIN_URL);
                JSON = webConnection.connect();
                //Log.d("JSON",JSON);
                convertJSON = conversionLoginResult(JSON);
                /** ログインの成否で処理を分岐 */
                if(convertJSON.get("success").equals(LOGIN_SUCCESS)){
                    /** 画面遷移 */
                    intent = new Intent(getApplicationContext(), MenuActivity.class);
                    intent.putExtra("userInfo",convertJSON);
                    startActivity(intent);
                    /*Toast.makeText(getApplicationContext(),"ログインに成功しました", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(),"ようこそ" + convertJSON.get("name") + "さん【" + convertJSON.get("position") + "】", Toast.LENGTH_SHORT).show();*/
                }
                else if(convertJSON.get("success").equals(LOGIN_FAILED)){
                    Toast.makeText(getApplicationContext(),R.string.failed_login, Toast.LENGTH_SHORT).show();
                }
                else if(convertJSON.get("success").equals(AUTH_ERROR)){//権限エラー
                    Toast.makeText(getApplicationContext(),R.string.permission_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     * 戻るボタン無効化
     */
    @Override
    public void onBackPressed() {
    }
    /**
     * JSON形式の文字列からログイン結果をを連想配列に変換する
     * @param JSON
     * @return 判定結果とログインユーザの情報をHashMap(連想配列)で返す
     */
    public HashMap<String ,String> conversionLoginResult(String JSON){
        HashMap<String ,String> result = new HashMap<String ,String>();
        String name;
        String position;
        String success;

        /**  JSONから各要素をStringに入れて更にHashMapに格納する */
        try {
            JSONObject rootObject = new JSONObject(JSON);

            success = String.valueOf(rootObject.get("success"));
            result.put("success",success);//put(key,value)で追加

            /** ログイン成功 */
            if(result.get("success").equals(LOGIN_SUCCESS)) {
                name = String.valueOf(rootObject.get("name"));
                result.put("name", name);//put(key,value)で追加

                /** 役職、権限の判定(教師以下は使用できない) */
                position = String.valueOf(rootObject.get("position"));
                if (position.equals(ADMINISTER)) {//管理者
                    result.put("position",getString(R.string.administer));//put(key,value)で追加
                } else if (position.equals(AFFAIRS)) {//学務
                    result.put("position",getString(R.string.affairs));//put(key,value)で追加
                } else if (position.equals(TEACHER)) {//教師
                    result.put("position",getString(R.string.teacher));//put(key,value)で追加
                    result.put("success", AUTH_ERROR);//put(key,value)で追加
                } else {

                }
            }

        } catch (JSONException e) {
            Toast.makeText(this,R.string.failed_login, Toast.LENGTH_SHORT).show();
        }
        return result;
    }

}