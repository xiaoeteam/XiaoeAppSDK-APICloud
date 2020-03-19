package com.xiaoe.shop.sdk.apicloud;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaoe.shop.sdk.apicloud.internal.APICloudUtils;
import com.xiaoe.shop.sdk.apicloud.internal.XEShopDecoration;
import com.xiaoe.shop.sdk.apicloud.internal.XEShopEventEmitter;
import com.xiaoe.shop.sdk.apicloud.internal.XETokenModel;
import com.xiaoe.shop.webcore.XEToken;
import com.xiaoe.shop.webcore.XiaoEWeb;

public class XEShopActivity extends Activity {
    public static String EXTRA_SHOP_URL = "shop_url";

    private ViewGroup mWebLayout;

    private XiaoEWeb mXiaoEWeb;

    public static boolean launch(Context context, String shopUrl) {
        if (context == null || TextUtils.isEmpty(shopUrl)) {
            return false;
        }

        Intent intent = new Intent(context, XEShopActivity.class);
        intent.putExtra(EXTRA_SHOP_URL, shopUrl);

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        setContentView(R.layout.mo_xeshopsdk_activity_xe_shop);

        final Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        final String shopUrl = intent.getStringExtra(EXTRA_SHOP_URL);
        if (TextUtils.isEmpty(shopUrl)) {
            finish();
            return;
        }

        initView();

        mXiaoEWeb = XiaoEWeb.with(this)
                .setWebParent(mWebLayout)
                .useDefaultUI()
                .useDefaultTopIndicator()
                .buildWeb()
                .loadUrl(shopUrl);

        mXiaoEWeb.setJsBridgeListener(XEShopEventEmitter.getInstance());

        // 启动时同步登录态
        XETokenModel.getInstance().observe(mTokenObserver);
    }

    private void initView() {
        mWebLayout = (ViewGroup) findViewById(R.id.web_layout);
        ImageView backView = (ImageView) findViewById(R.id.xe_sdk_back_img);
        ImageView closeView = (ImageView) findViewById(R.id.xe_sdk_close_img);
        ImageView shareView = (ImageView) findViewById(R.id.xe_sdk_share_img);
        ViewGroup titleLayout = (ViewGroup) findViewById(R.id.xe_sdk_title_layout);
        TextView titleView = (TextView) findViewById(R.id.xe_sdk_title_tv);

        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mXiaoEWeb == null || !mXiaoEWeb.handlerBack()) {
                    finish();
                }
            }
        });

        shareView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mXiaoEWeb != null) {
                    mXiaoEWeb.share();
                }
            }
        });

        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Title
        final String title = XEShopDecoration.sTitle;
        titleView.setText(title);

        // NavStyle
        final NavStyle style = XEShopDecoration.sNavStyle;
        if (style.titleFontSize > 0) {
            titleView.setTextSize(style.titleFontSize);
        }
        if (style.titleColor != null) {
            titleView.setTextColor(Color.parseColor(style.titleColor));
        }
        if (style.backgroundColor != null) {
            titleLayout.setBackgroundColor(Color.parseColor(style.backgroundColor));
        }
        setImage(this, backView, style.backIconImageName, R.mipmap.mo_xeshopsdk_back_icon);
        setImage(this, closeView, style.closeIconImageName, R.mipmap.mo_xeshopsdk_close_icon);
        setImage(this, shareView, style.shareIconImageName, R.mipmap.mo_xeshopsdk_share_icon);
    }

    private void setImage(Context context, ImageView imageView, String imageName, int fallback) {
        if (imageName != null) {
            final int size = context.getResources().getDimensionPixelSize(R.dimen.mo_xeshopsdk__icon_size);
            Bitmap image = APICloudUtils.getImageFromAssets(context, imageName, size, size);
            if (image != null) {
                imageView.setImageBitmap(image);
                return;
            }
        }

        imageView.setImageResource(fallback);
    }

    private XETokenModel.XETokenObserver mTokenObserver = new XETokenModel.XETokenObserver() {
        @Override
        public void onChanged(String tokenKey, String tokenValue) {
            syncToken(tokenKey, tokenValue);
        }
    };

    private void syncToken(String tokenKey, String tokenValue) {
        if (TextUtils.isEmpty(tokenKey) || TextUtils.isEmpty(tokenValue)) {
            return;
        }

        if (mXiaoEWeb != null) {
            mXiaoEWeb.sync(new XEToken(tokenKey, tokenValue));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mXiaoEWeb != null) {
            mXiaoEWeb.webLifeCycle().onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mXiaoEWeb != null) {
            mXiaoEWeb.webLifeCycle().onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XETokenModel.getInstance().removeObserver(mTokenObserver);

        if (mXiaoEWeb != null) {
            mXiaoEWeb.webLifeCycle().onDestroy();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mXiaoEWeb != null && mXiaoEWeb.handlerKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static class NavStyle {
        String titleColor;
        int titleFontSize;
        String backgroundColor;
        String backIconImageName;
        String closeIconImageName;
        String shareIconImageName;

        public NavStyle titleColor(String color) {
            if (checkColor(color)) {
                this.titleColor = color;
            }
            return this;
        }

        public NavStyle titleFontSize(int fontSize) {
            if (checkFontSize(fontSize)) {
                this.titleFontSize = fontSize;
            }
            return this;
        }

        public NavStyle backgroundColor(String color) {
            if (checkColor(color)) {
                this.backgroundColor = color;
            }
            return this;
        }

        public NavStyle backIconImageName(String imageName) {
            if (checkImageName(imageName)) {
                this.backIconImageName = imageName;
            }
            return this;
        }

        public NavStyle closeIconImageName(String imageName) {
            if (checkImageName(imageName)) {
                this.closeIconImageName = imageName;
            }
            return this;
        }

        public NavStyle shareIconImageName(String imageName) {
            if (checkImageName(imageName)) {
                this.shareIconImageName = imageName;
            }
            return this;
        }

        private boolean checkColor(String color) {
            if (TextUtils.isEmpty(color)) return false;
            try {
                Color.parseColor(color);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }

        private boolean checkFontSize(int fontSize) {
            return fontSize >= 0;
        }

        private boolean checkImageName(String name) {
            return !TextUtils.isEmpty(name);
        }
    }
}
