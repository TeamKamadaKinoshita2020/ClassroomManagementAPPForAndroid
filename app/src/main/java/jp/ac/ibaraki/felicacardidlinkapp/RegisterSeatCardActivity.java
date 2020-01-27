package jp.ac.ibaraki.felicacardidlinkapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;

public class RegisterSeatCardActivity extends Activity implements View.OnClickListener,ReadCardIdFragment.MyListener{
    static final String REGISTER_CARD_URL = "register-card-info.php";

    private ReadCardIdFragment dialogFragment;

    private Intent intent;
    private HashMap<String,String> userInfo;
    private TextView userText;
    private EditText identityIdText;
    private TextView cardIdText;
    private Button readCardButton;
    private Button registerButton;
    private Button logoutButton;

    private String readCardId;//読み取った座席カードID

    private String[] sendValue;
    private WebConnection webConnection;
    private String JSON;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

    private boolean cardReadFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_seat_card);

        intent = getIntent();
        userInfo = (HashMap<String,String>)intent.getSerializableExtra("userInfo");
        userText = (TextView) findViewById(R.id.userText);
        userText.setText(getString(R.string.login_user) + "：" + userInfo.get("name") + "【" + userInfo.get("position") + "】");

        /*readCardButton = (Button) findViewById(R.id.readCardIdButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);*/

        cardIdText = (TextView) findViewById(R.id.cardIdText);
        identityIdText = (EditText) findViewById(R.id.identityIdText);

        findViewById(R.id.readCardIdButton) .setOnClickListener(this);
        findViewById(R.id.registerButton) .setOnClickListener(this);
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
    public void onClick(View v) {
        if (v != null) {
            switch (v.getId()) {
                case R.id.readCardIdButton:/** カード読み取り待機ダイヤログの表示 */
                    cardReadFlag = true;
                    Bundle args = new Bundle();
                    args.putString(ReadCardIdFragment.FIELD_TITLE, "カードIDの読み取り");
                    args.putString(ReadCardIdFragment.FIELD_MESSAGE, "端末で登録したいカードを読み取ってください");
                    // 自分で定義したレイアウト
                    //args.putInt(ReadCardIdFragment.FIELD_LAYOUT, R.layout.fragment_read_card_id);
                    //args.putString(ReadCardIdFragment.FIELD_LABEL_POSITIVE, "ID登録を行う");
                    args.putString(ReadCardIdFragment.FIELD_LABEL_NEGATIVE, getString(R.string.cancel));
                    dialogFragment = new ReadCardIdFragment();
                    dialogFragment.setArguments(args);
                    dialogFragment.setCancelable(false);
                    dialogFragment.show(getFragmentManager(), "dialog2");
                    break;

                case R.id.registerButton:/** カード登録処理 */
                    if(!cardReadFlag || isEmpty(identityIdText.getText().toString())){//空なのでエラー表示
                        Toast.makeText(this,"登録内容に不備があります",Toast.LENGTH_SHORT).show();
                    }
                    else{//登録処理
                        sendValue = new String[2];
                        sendValue[0] = cardIdText.getText().toString();
                        sendValue[1] = identityIdText.getText().toString();
                        webConnection = new WebConnection(sendValue,REGISTER_CARD_URL);
                        JSON = webConnection.connect();
                        //結果を表示する
                        try {
                            Log.d("result",JSON);
                            JSONObject rootObject = new JSONObject(JSON);
                            String success = String.valueOf(rootObject.get("success"));
                            if (success.equals("1")) {
                                Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
                                reload();//更新
                            } else {
                                Toast.makeText(this, R.string.failed, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(this,R.string.failed, Toast.LENGTH_SHORT).show();
                        }

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
        //読取モードかつ読みとれていればダイヤログを閉じてレイアウトに反映させる
        if(this.readCardId != null && cardReadFlag){
            dialogFragment.dismiss();//現在表示しているダイヤログを閉じる

            cardIdText = (TextView)findViewById(R.id.cardIdText);
            cardIdText.setText(readCardId);
        }
    }

    @Override
    // ダイヤログでボタンが押された場合の処理
    public void onClickButton(boolean positiveFlag) {
        cardReadFlag = false;//読み取りモードフラグをオフにする
        /*if(positiveFlag) {
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
        }*/
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


    /**
     * 指定された String が null または空文字列かどうかを返します。
     *
     * @param value チェックする String
     * @return null または空文字列かどうか。null または空文字列なら true 、それ以外なら false 。
     */
    public static boolean isEmpty(String value) {

        if ( value == null || value.length() == 0 )
            return true;
        else
            return false;
    }


}
