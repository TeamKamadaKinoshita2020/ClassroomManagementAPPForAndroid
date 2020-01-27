package jp.ac.ibaraki.felicacardidlinkapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReadCardIdActivity extends ListActivity implements View.OnClickListener,ReadCardIdFragment.MyListener {
    public static final String GET_SEAT_LIST_URL = "android-seat-list.php";
    public static final String GET_CARD_INFO_URL = "get-card-info.php";
    public static final String LINK_CARD_ID_URL = "link-card-id.php";

    private ReadCardIdFragment dialogFragment;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

    private String roomId;
    private String selectSeatNum;
    private String selectCardId;//元々登録されている座席カードID
    private String selectCardIdentityId;//元々登録されている座席カードの識別ID

    private String readCardId;//読み取った座席カードID

    private Intent intent;
    private HashMap<String,String> userInfo;//前のアクティビティから渡される値
    private ClassroomInfo classroomInfo;

    private TextView userText;
    private TextView classroomText;
    private Button button;
    private WebConnection webConnection;

    private ArrayList seatList;

    private String[] sendValue;
    private String JSON;

    private boolean cardReadFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_card_id);

        intent = getIntent();
        userInfo = (HashMap<String,String>)intent.getSerializableExtra("userInfo");
        classroomInfo = new ClassroomInfo(intent.getStringExtra("roomName"),Integer.parseInt(intent.getStringExtra("roomId")));
        userText = (TextView) findViewById(R.id.userText);
        userText.setText(getString(R.string.login_user) + "：" + userInfo.get("name") + "【" + userInfo.get("position") + "】");
        classroomText = (TextView) findViewById(R.id.classroomText);
        classroomText.setText("選択教室:" + classroomInfo.getName());

        //教室リストの作成
        sendValue = new String[2];
        sendValue[0] = String.valueOf(classroomInfo.getId());
        Log.d("id",sendValue[0]);
        webConnection = new WebConnection(sendValue,GET_SEAT_LIST_URL);//POST値、URLを設定
        JSON = webConnection.connect();
        seatList = conversionSeatList(JSON);

        List<SeatInfo> list = new ArrayList<SeatInfo>();
        for (int i = 0; i < seatList.size(); i++) {
            String cardId = ((HashMap<String, String>) seatList.get(i)).get("cardId");
            String identityId = ((HashMap<String, String>) seatList.get(i)).get("identityId");
            list.add(new SeatInfo(((HashMap<String, String>) seatList.get(i)).get("seatNum"),cardId,identityId));
        }
        setListAdapter(new SeatListAdapter(this, list));

        //クリックイベント設定
        findViewById(R.id.button1) .setOnClickListener(this);
        findViewById(R.id.button2) .setOnClickListener(this);
        findViewById(R.id.logoutButton) .setOnClickListener(this);

        /** レイアウト関連終わり */

        // ForegroundDispatchSystemを有効
        this.mAdapter = NfcAdapter.getDefaultAdapter(this);
        this.mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(
                this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }


        //NFCがOFFならONにさせるウィンドウ表示
        if(mAdapter.isEnabled() == false){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            alertDialog.setTitle(R.string.noticeNFCdisable1);
            alertDialog.setMessage(R.string.noticeNFCdisable2);
            alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    final Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    startActivity(intent);
                    dialog.dismiss();
                }
            });
            alertDialog.show();
        }
    }


    @Override
    public void onClick(View v){
        if (v != null) {
            switch (v.getId()) {
                case R.id.button1:/** 教室一覧ボタンが押された場合 */
                    finish();
                    break;
                case R.id.button2: /** レイアウト確認が押された場合の処理 */
                    intent = new Intent(this, ConfirmLayoutActivity.class);
                    intent.putExtra("name",classroomInfo.getName());
                    intent.putExtra("roomId",String.valueOf(classroomInfo.getId()));
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

    @Override
    public void onResume() {
        super.onResume();
        if (this.mAdapter != null) {
            setNfcIntentFilter(this, this.mAdapter, mPendingIntent);
        }
    }

    /**
     * Felicaカード読み取り時の処理
     * @param intent
     */
    @Override
    public void onNewIntent(final Intent intent) {
        this.readCardId = getIdm(intent);
        Log.d("idm",this.readCardId);
        //読取モードかつ読みとれていれば処理を行う
        if(this.readCardId != null && cardReadFlag){

            sendValue = new String[1];
            sendValue[0] = this.readCardId;
            webConnection = new WebConnection(sendValue,GET_CARD_INFO_URL);
            JSON = webConnection.connect();
            /** DB内に登録されたカードかどうかチェックする */
            try {
                Log.d("result",JSON);
                JSONObject rootObject = new JSONObject(JSON);
                String success = String.valueOf(rootObject.get("success"));
                if (success.equals("1")) {
                    /** カードID登録確認ダイヤログの表示 */
                    Bundle args = new Bundle();
                    args.putString(ReadCardIdFragment.FIELD_TITLE, "登録カードの確認");
                    args.putString(ReadCardIdFragment.FIELD_MESSAGE, "この内容で登録を行いますか？");
                    // 自分で定義したレイアウト
                    args.putInt(ReadCardIdFragment.FIELD_LAYOUT, R.layout.fragment_read_card_id);
                    args.putString(ReadCardIdFragment.SELECT_CARD_ID,selectCardId);
                    args.putString(ReadCardIdFragment.READ_CARD_ID, readCardId);
                    args.putString(ReadCardIdFragment.SELECT_CARD_IDENTITY_ID,selectCardIdentityId);
                    args.putString(ReadCardIdFragment.READ_CARD_IDENTITY_ID, String.valueOf(rootObject.get("identity_id")));
                    args.putString(ReadCardIdFragment.FIELD_LABEL_POSITIVE, "登録を行う");
                    args.putString(ReadCardIdFragment.FIELD_LABEL_NEGATIVE, "キャンセル");
                    dialogFragment.dismiss();//現在表示しているダイヤログを閉じて新しく開く
                    dialogFragment = new ReadCardIdFragment();
                    dialogFragment.setArguments(args);
                    dialogFragment.setCancelable(false);
                    dialogFragment.show(getFragmentManager(), "dialog2");
                } else {
                    Toast.makeText(this, "データベースに存在しないカードです", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(this,"読み取りエラー", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    // ダイヤログでボタンが押された場合の処理
    public void onClickButton(boolean positiveFlag) {
        cardReadFlag = false;//読み取りモードフラグをオフにする
        if(positiveFlag) {
            sendValue = new String[3];
            sendValue[0] =  String.valueOf(classroomInfo.getId());
            sendValue[1] = selectSeatNum;
            sendValue[2] = readCardId;
            Log.d("send",sendValue[0]+sendValue[1]+sendValue[2]);
            webConnection = new WebConnection(sendValue,LINK_CARD_ID_URL);
            JSON = webConnection.connect();
            //結果を表示する
            try {
                Log.d("result",JSON);
                JSONObject rootObject = new JSONObject(JSON);
                String success = String.valueOf(rootObject.get("success"));
                if (success.equals("1")) {
                    Toast.makeText(this, "正常にカード登録が行われました", Toast.LENGTH_SHORT).show();
                    reload();//更新
                } else {
                    Toast.makeText(this, "カード登録に失敗しました", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(this,"カード登録に失敗しました", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "カード登録をキャンセルしました", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * IDmを取得する
     * @param intent
     * @return
     */
    private String getIdm(Intent intent) {
        String idm = null;
        StringBuffer idmByte = new StringBuffer();
        byte[] rawIdm = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        if (rawIdm != null) {
            for (int i = 0; i < rawIdm.length; i++) {
                idmByte.append(Integer.toHexString(rawIdm[i] & 0xff));
            }
            idm = idmByte.toString();
        }
        Log.d("idm",idm);
        return idm;
    }

    /**
     * フォアグラウンドディスパッチシステムで、アプリ起動時には優先的にNFCのインテントを取得するように設定する
     */
    private void setNfcIntentFilter(Activity activity, NfcAdapter nfcAdapter, PendingIntent seder) {
        // NDEF type指定
        IntentFilter typeNdef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            typeNdef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        // NDEF スキーマ(http)指定
        IntentFilter httpNdef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        httpNdef.addDataScheme("http");
        IntentFilter[] filters = new IntentFilter[] {
                typeNdef, httpNdef
        };
        // TECH指定
        String[][] techLists = new String[][] {
                new String[] { IsoDep.class.getName() },
                new String[] { NfcA.class.getName() },
                new String[] { NfcB.class.getName() },
                new String[] { NfcF.class.getName() },
                new String[] { NfcV.class.getName() },
                new String[] { Ndef.class.getName() },
                new String[] { NdefFormatable.class.getName() },
                new String[] { MifareClassic.class.getName() },
                new String[] { MifareUltralight.class.getName() }
        };
        nfcAdapter.enableForegroundDispatch(activity, seder, filters, techLists);
    }


    /**
     * リストの席が選択された場合の処理
     * @param l
     * @param v
     * @param position
     * @param id
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final SeatInfo item = (SeatInfo) l.getAdapter().getItem(position);

        selectSeatNum = item.getNum();
        selectCardId = item.getCardId();
        selectCardIdentityId = item.getIdentityId();

        cardReadFlag = true;//読み取りモードフラグをオンにする

        /** カード読み取り待機ダイヤログの表示 */
        Bundle args = new Bundle();
        args.putString(ReadCardIdFragment.FIELD_TITLE, item.getNum() + "番座席のカードID登録");
        args.putString(ReadCardIdFragment.FIELD_MESSAGE, "端末で登録したいカードを読み取ってください");
        // 自分で定義したレイアウト
        //args.putInt(ReadCardIdFragment.FIELD_LAYOUT, R.layout.fragment_read_card_id);
        //args.putString(ReadCardIdFragment.FIELD_LABEL_POSITIVE, "ID登録を行う");
        args.putString(ReadCardIdFragment.FIELD_LABEL_NEGATIVE, "キャンセル");
        dialogFragment = new ReadCardIdFragment();
        dialogFragment.setArguments(args);
        dialogFragment.setCancelable(false);
        dialogFragment.show(getFragmentManager(), "dialog2");
    }

    /**
     * JSON形式の文字列から座席のリストを作成するメソッド
     * @param JSON
     * @return 作成されるリスト
     */
    public ArrayList<HashMap> conversionSeatList(String JSON){
        ArrayList<HashMap> seatList = new ArrayList<HashMap>();
        String seatNum;
        String cardId;
        String identityId;
        String count;

        try {
            JSONObject rootObject = new JSONObject(JSON);
            count = String.valueOf(rootObject.get("count"));
            Log.d("count",count);//デバック用

            if(Integer.parseInt(count) > 0) {
                for (int i = 0; i < Integer.parseInt(count); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();//一時的に格納する
                    JSONObject classroomObject = rootObject.getJSONObject("" + i);
                    seatNum = String.valueOf(classroomObject.get("seat_number"));
                    cardId = String.valueOf(classroomObject.get("card_id"));
                    identityId = String.valueOf(classroomObject.get("identity_id"));

                    map.put("seatNum", seatNum);
                    map.put("cardId", cardId);
                    map.put("identityId", identityId);

                    seatList.add(map);

                    Log.d("seatInfo", seatNum + ":" + cardId);
                }
            }
        } catch (JSONException e) {
            Toast.makeText(this,"リストの作成に失敗しました", Toast.LENGTH_SHORT).show();
        }
        return seatList;
    }

    /**
     * カードID登録後にアクティビティの更新を行う関数
     */
    private void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }
}
