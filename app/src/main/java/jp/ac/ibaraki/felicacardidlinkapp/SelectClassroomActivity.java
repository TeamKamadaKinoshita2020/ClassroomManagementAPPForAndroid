package jp.ac.ibaraki.felicacardidlinkapp;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectClassroomActivity extends ListActivity implements View.OnClickListener{

    public static final String CLASSROOM_LIST_URL = "get-classroom-list.php";
    public static final String CLASSROOM_LAYOUT_URL = "comfirm-classroom-layout.php";


    private Intent intent;
    private TextView userText;
    private TextView selectText;
    private ClassroomInfo selectClassroom;
    private WebConnection webConnection;
    private static final String[] dummy = {""};
    private String[] sendValue;
    private String JSON;
    private String message;
    private ArrayList<HashMap> classroomList;
    private HashMap<String,String> userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_classroom);

        //レイアウトの作成
        intent = getIntent();
        userInfo = (HashMap<String,String>)intent.getSerializableExtra("userInfo");
        userText = (TextView) findViewById(R.id.userText);
        userText.setText(getString(R.string.login_user) + "：" + userInfo.get("name") + "【" + userInfo.get("position") + "】");

        selectText = (TextView) findViewById(R.id.textView2);

        //教室リストの作成
        webConnection = new WebConnection(dummy,CLASSROOM_LIST_URL);//POST値、URLを設定
        JSON = webConnection.connect();
        classroomList = conversionClassroomList(JSON);

        List<ClassroomInfo> list = new ArrayList<ClassroomInfo>();
        for (int i = 0; i < classroomList.size(); i++) {
            String id = ((HashMap<String, String>)classroomList.get(i)).get("roomId");
            list.add(new ClassroomInfo(((HashMap<String, String>) classroomList.get(i)).get("name"),Integer.parseInt(id)));
        }
        setListAdapter(new ClassroomListAdapter(this, list));

        //クリックイベント設定
        findViewById(R.id.logoutButton) .setOnClickListener(this);
        findViewById(R.id.button1) .setOnClickListener(this);
        findViewById(R.id.button2) .setOnClickListener(this);

    }

    @Override
    public void onClick(View v){
        if (v != null) {
            switch (v.getId()) {
                case R.id.button1:
                    if(selectClassroom != null) {/** 管理教室を決定した場合 */
                        intent = new Intent(this, ReadCardIdActivity.class);
                        intent.putExtra("roomName",selectClassroom.getName());
                        intent.putExtra("roomId",String.valueOf(selectClassroom.getId()));
                        intent.putExtra("userInfo",userInfo);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(getApplicationContext(),R.string.select_classroom, Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.button2: /** レイアウト確認が押された場合の処理 */
                    if(selectClassroom != null) {
                        intent = new Intent(this, ConfirmLayoutActivity.class);
                        intent.putExtra("name",selectClassroom.getName());
                        intent.putExtra("roomId",String.valueOf(selectClassroom.getId()));
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(getApplicationContext(),R.string.select_classroom, Toast.LENGTH_SHORT).show();
                    }
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
     * リストの講義が選択された場合の処理
     * @param l
     * @param v
     * @param position
     * @param id
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.d("a","タップ");

        super.onListItemClick(l, v, position, id);
        final ClassroomInfo item = (ClassroomInfo) l.getAdapter().getItem(position);
        selectText.setText(getString(R.string.select_now)  + "：" + item.getName() + "【ID:" + item.getId() + "】");
        Toast.makeText(getApplicationContext(),getString(R.string.classroom_num) + "：" + item.getName() + "【ID:" + item.getId() + "】", Toast.LENGTH_SHORT).show();
        selectClassroom = new ClassroomInfo(item.getName(),item.getId());
    }

    /**
     * JSON形式の文字列から教室のリストを作成するメソッド
     * @param JSON
     * @return 作成されるリスト
     */
    public ArrayList<HashMap> conversionClassroomList(String JSON){
        ArrayList<HashMap> classroomList = new ArrayList<HashMap>();
        String name;
        String roomId;
        String count;

        try {
            JSONObject rootObject = new JSONObject(JSON);
            count = String.valueOf(rootObject.get("count"));
            Log.d("count",count);//デバック用

            if(Integer.parseInt(count) > 0) {
                for (int i = 0; i < Integer.parseInt(count); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();//一時的に格納する
                    JSONObject classroomObject = rootObject.getJSONObject("" + i);
                    name = String.valueOf(classroomObject.get("name"));
                    roomId = String.valueOf(classroomObject.get("room_id"));

                    map.put("name", name);
                    map.put("roomId", roomId);

                    classroomList.add(map);

                    Log.d("classroom_info", roomId + ":" + name);
                }
            }
        } catch (JSONException e) {
            Toast.makeText(this,R.string.failed_list_make, Toast.LENGTH_SHORT).show();
        }
        return classroomList;
    }
}
