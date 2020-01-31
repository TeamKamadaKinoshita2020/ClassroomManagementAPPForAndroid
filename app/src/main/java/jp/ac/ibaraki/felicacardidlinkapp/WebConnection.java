package jp.ac.ibaraki.felicacardidlinkapp;

import android.util.Log;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 大学用 on 2017/09/06.
 * ライブラリOkHttpを用いてwebサーバと通信を行うクラス
 */

public class WebConnection {
    private String[] sendValue;
    private String url;
    private String resultData;
    private Boolean resultFlag = false;

    WebConnection(String[] sendValue,String url){
        this.sendValue = sendValue;
        this.url = url;
        Log.d("a","コンストラクタ生成");
    }

    public String connect(){
        RequestBody formBody = null;
        try {
            Log.d("a","try!!");
            switch (this.url){//アクセスするサイトによってPOSTに入れる値の分岐
                case "login.php":
                    formBody = makeLoginBody(this.sendValue[0],this.sendValue[1]);
                    break;
                case "get-classroom-list.php":
                    formBody = makeClassroomListBody();
                    break;
                case "android-seat-list.php":
                    formBody = makeSeatListBody(this.sendValue[0]);
                    break;
                case "get-card-info.php":
                    formBody = makeGetCardInfoBody(this.sendValue[0]);
                    break;
                case "link-card-id.php":
                    formBody = makeLinkCardIdBody(this.sendValue[0],this.sendValue[1],this.sendValue[2]);
                    break;
                case "register-card-info.php":
                    formBody = makeRegisterCardInfoBody(this.sendValue[0],this.sendValue[1]);
                    break;
                default:
                    Log.d("a","ne-yo");
                    resultData =  "通信に失敗しました";
                    resultFlag = true;
                    break;
            }
            Log.d("a","分岐終了"+this.url);
            Request request = new Request.Builder()
                    .url("hoge"+url) //　 hoge = "http://IP address/attendancesystem/android/"
                    .post(formBody)
                    .build();

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .readTimeout(15 * 1000, TimeUnit.MILLISECONDS)
                    .writeTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                    .connectTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Log.d("tag", "OkHttpのデータ取得失敗");
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //string()が使えるのは1回だけらしい
                    resultData = response.body().string();
                    resultFlag = true;
                    //Log.d("tag", "結果 : " + response.body().string()+"a");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("tag", "DBからの読み込みエラー");
        } finally {
            while(!resultFlag){//結果が取得できるまで待つ
                //Log.d("loop","処理待ち")
                try{
                    //500ミリ秒Sleepする
                    Thread.sleep(500);
                }catch(InterruptedException e){}
            }
            Log.d("return",resultData);
            return resultData;
        }
    }

    /**
     * ログインするためのボディを生成する
     * 引数をPOSTで送る値として設定している
     * @param userId
     * @param pass
     * @return
     */
    public  RequestBody makeLoginBody(String userId,String pass){
        RequestBody formBody = new FormBody.Builder()
                .add("u_id", userId)
                .add("pass", pass)
                .build();
        return formBody;
    }


    /**
     * 登録教室リスト用のRequestBodyを生成する
     * @return
     */
    public RequestBody makeClassroomListBody(){
        RequestBody formBody = new FormBody.Builder()
                .build();

        return formBody;
    }

    /**
     * 指定IDの教室の席リストを取得するためのボディを生成する
     * 引数をPOSTで送る値として設定している
     * @param roomId
     * @return
     */
    public  RequestBody makeSeatListBody(String roomId){
        RequestBody formBody = new FormBody.Builder()
                .add("r_id",roomId)
                .build();

        return formBody;
    }


    /**
     * カード情報取得用のボディを生成する
     * 引数をPOSTで送る値として設定している
     * @param cardId
     * @return
     */
    public RequestBody makeGetCardInfoBody(String cardId){
        RequestBody formBody = new FormBody.Builder()
                .add("c_id",cardId)
                .build();

        return formBody;
    }

    /**
     * 座席ID登録用のボディを生成する
     * 引数をPOSTで送る値として設定している
     * @param cardId
     * @return
     */
    public RequestBody makeLinkCardIdBody(String roomId,String seatNumber,String cardId){
        RequestBody formBody = new FormBody.Builder()
                .add("r_id",roomId)
                .add("seat_num",seatNumber)
                .add("c_id",cardId)
                .build();

        return formBody;
    }

    /**
     * カードの情報登録用のボディを生成する
     * @param cardId 固有のカードID
     * @param identityId 識別用に作ったID
     * @return
     */
    public RequestBody makeRegisterCardInfoBody(String cardId,String identityId){
        RequestBody formBody = new FormBody.Builder()
                .add("c_id",cardId)
                .add("i_id",identityId)
                .build();

        return formBody;
    }
}
