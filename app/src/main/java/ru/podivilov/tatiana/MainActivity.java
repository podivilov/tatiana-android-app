package ru.podivilov.tatiana;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

public class MainActivity extends Activity {

    private WebView mWebView;
    private ImageView mImageView;
    private Boolean doNotShowMeAgain = false;

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        mWebView = findViewById(R.id.activity_main_webview);
        mImageView = findViewById(R.id.imageView);

        CookieManager cookieManager = CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(mWebView, true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                try {
                    view.stopLoading();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    view.clearView();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!isOnline()) {
                    mWebView.loadUrl("about:blank");
                    mWebView.loadUrl("file:///android_asset/error_pages/no_internet_connection.html");
                    mWebView.invalidate();
                }
                else
                {
                    mWebView.loadUrl("about:blank");
                    mWebView.loadUrl("file:///android_asset/error_pages/unexpected_error.html");
                    mWebView.invalidate();
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
            @Override
            public void onPageCommitVisible(WebView view, String url)
            {
                if (!doNotShowMeAgain) {
                    try {
                        Handler handler = new Handler();
                        Runnable r=new Runnable() {
                            public void run() {
                                View decorView = getWindow().getDecorView();
                                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                                decorView.setSystemUiVisibility(uiOptions);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    Window window = getWindow();
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                    window.setStatusBarColor(Color.TRANSPARENT);
                                }

                                mImageView.setVisibility(View.GONE);
                                mWebView.setVisibility(View.VISIBLE);                            }
                        };
                        handler.postDelayed(r, 1000);
                    } catch (Exception e) {
                        System.exit(1);
                    }
                }
                doNotShowMeAgain = true;
            }
        });
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.setVerticalScrollBarEnabled(false);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mWebView.loadUrl("https://tatiana-app.podivilov.ru/android/");

        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        mWebView.setLongClickable(false);
    }

    @Override
    public void onBackPressed() { }
}