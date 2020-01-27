package jp.ac.ibaraki.felicacardidlinkapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;

/**
 * トップページでログインに成功した場合に表示される画面
 * ここで一覧表示されている機能を選択して教室、座席カードの管理を行う
 * @author ShinyaKinoshita
 */
public class MenuActivity extends AppCompatActivity implements View.OnClickListener{

    private Intent intent;
    private HashMap<String,String> userInfo;
    private TextView userText;
    private Button manageClassroomButton;
    private Button registerSeatCardButton;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        intent = getIntent();
        userInfo = (HashMap<String,String>)intent.getSerializableExtra("userInfo");
        userText = (TextView) findViewById(R.id.userText);
        userText.setText(getString(R.string.login_user) + "：" + userInfo.get("name") + "【" + userInfo.get("position") + "】");

        /*readCardButton = (Button) findViewById(R.id.readCardIdButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);*/


        findViewById(R.id.manageClassroomButton) .setOnClickListener(this);
        findViewById(R.id.registerSeatCardButon) .setOnClickListener(this);
        findViewById(R.id.logoutButton) .setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v != null) {
            switch (v.getId()) {
                case R.id.manageClassroomButton: /** 教室(座席)管理画面へ */
                    /** 画面遷移 */
                    intent = new Intent(getApplicationContext(), SelectClassroomActivity.class);
                    intent.putExtra("userInfo",userInfo);
                    startActivity(intent);
                    break;

                case R.id.registerSeatCardButon: /** 新規カード登録画面へ */
                    /** 画面遷移 */
                    intent = new Intent(getApplicationContext(), RegisterSeatCardActivity.class);
                    intent.putExtra("userInfo",userInfo);
                    startActivity(intent);
                    break;

                case R.id.logoutButton: /** ログアウト処理 */
                    /*確認ダイヤログ表示 */
                    new android.support.v7.app.AlertDialog.Builder(this)
                            .setTitle(R.string.confirm_logout)
                            .setMessage(R.string.confirm_logout_message)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    intent = new Intent(getApplication(), MainActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 戻るボタン無効化
     */
    @Override
    public void onBackPressed() {
    }
}
