package jp.ac.ibaraki.felicacardidlinkapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class ConfirmLayoutActivity extends Activity {
    private final String CONFIRM_LAYOUT_URL = "confirm-classroom-layout.php";

    private TextView textView;
    private WebView webView;
    private Intent intent;
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_layout);
        intent = getIntent();
        textView = (TextView) findViewById(R.id.textView1);
        textView.setText(intent.getStringExtra("name") + "【ID:" + intent.getStringExtra("roomId") + "】のレイアウト");
        webView = (WebView) findViewById(R.id.webView);
        //クライアント上書き。
        //webView.setWebViewClient(new ViewClient(webView));
        //ユーザーエージェント設定
        String userAgent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent + "fc.app.native Android Mobile AppleWebKit Device");

        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);//javascriptを有効化
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);


        url = "http://13.114.37.66/attendancesystem/" + CONFIRM_LAYOUT_URL + "?room_id=" + intent.getStringExtra("roomId");//aws
        //url = "http://157.80.87.23/attendancesystem/" + CONFIRM_LAYOUT_URL + "?room_id=" + intent.getStringExtra("roomId");//けんきうしつ
        //url = "http://192.168.11.5/attendancesystem/" + CONFIRM_LAYOUT_URL + "?room_id=" + intent.getStringExtra("roomId");//家

        Log.d("url",url);
        webView.loadUrl(url);
        webView.zoomOut();
        webView.zoomOut();
    }
}
