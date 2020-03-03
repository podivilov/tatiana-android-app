package ru.podivilov.tatiana;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends Activity {

    private WebView mWebView;
    private ImageView mImageView;
    private Boolean doNotShowMeAgain = false;
    private static final String TAG = "MainActivity";

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public class BootUpReceiver extends BroadcastReceiver {
        @SuppressLint("UnsafeProtectedBroadcastReceiver")
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_DEFAULT));
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }

        FirebaseMessaging.getInstance().subscribeToTopic(String.valueOf(R.string.default_notification_channel_id))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        //Toast.makeText(ru.podivilov.tatiana.MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        //Toast.makeText(ru.podivilov.tatiana.MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

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
            @Override
            public void onPageFinished(WebView view, String url) {
                CookieSyncManager.getInstance().sync();
            }
        });
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.setVerticalScrollBarEnabled(false);

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
    public void onBackPressed()
    {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN, null);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(homeIntent);
    }
}