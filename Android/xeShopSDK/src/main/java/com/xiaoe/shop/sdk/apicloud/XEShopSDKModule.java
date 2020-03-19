package com.xiaoe.shop.sdk.apicloud;

import android.text.TextUtils;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.ModuleResult;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.xiaoe.shop.sdk.apicloud.internal.APICloudUtils;
import com.xiaoe.shop.sdk.apicloud.internal.XEShopDecoration;
import com.xiaoe.shop.sdk.apicloud.internal.XEShopEventEmitter;
import com.xiaoe.shop.sdk.apicloud.internal.XETokenModel;
import com.xiaoe.shop.webcore.XiaoEWeb;
import com.xiaoe.shop.webcore.bridge.JsBridgeListener;
import com.xiaoe.shop.webcore.bridge.JsCallbackResponse;
import com.xiaoe.shop.webcore.bridge.JsInteractType;

import org.json.JSONException;
import org.json.JSONObject;

public class XEShopSDKModule extends UZModule {
    private static final String FEATURE_NAME = "xeShopSDK";

    // initConfig 接口参数

    private static final String APP_ID = "appId";
    private static final String CLIENT_ID = "clientId";
    private static final String IS_LOG = "isLog";

    private static final String TITLE = "title";

    private static final String TITLE_COLOR = "titleColor";
    private static final String TITLE_FONT_SIZE = "titleFontSize";
    private static final String BACKGROUND_COLOR = "backgroundColor";
    private static final String BACK_ICON_IMAGE_NAME = "backIconImageName";
    private static final String CLOSE_ICON_IMAGE_NAME = "closeIconImageName";
    private static final String SHARE_ICON_IMAGE_NAME = "shareIconImageName";

    private static final String SHOP_URL = "shopUrl";

    public XEShopSDKModule(UZWebView webView) {
        super(webView);
    }

    public void jsmethod_init(final UZModuleContext moduleContext) {
        // 优先获取接口配置
        String appId = moduleContext.optString(APP_ID);
        String clientId = moduleContext.optString(CLIENT_ID);
        boolean isOpenLog = moduleContext.optBoolean(IS_LOG, false);

        // 接口未传则从config.xml中读取配置
        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(clientId)) {
            appId = getFeature(APP_ID);
            clientId = getFeature(CLIENT_ID);
            isOpenLog = getBooleanFeature(IS_LOG, false);
        }

        // 检查必要参数
        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(clientId)) {
            APICloudUtils.error(moduleContext, "Missing appId or clientId");
            return;
        }

        // 初始化SDK
        XiaoEWeb.init(context().getApplicationContext(), appId, clientId);
        XiaoEWeb.isOpenLog(isOpenLog);

        // 初始化样式配置
        final String title = getFeature(TITLE);
        if (title != null) {
            XEShopDecoration.sTitle = title;
        }
        final String titleColor = getFeature(TITLE_COLOR);
        if (titleColor != null) {
            XEShopDecoration.sNavStyle.titleColor = titleColor;
        }
        final Integer titleFontSize = getIntFeature(TITLE_FONT_SIZE);
        if (titleFontSize != null) {
            XEShopDecoration.sNavStyle.titleFontSize = titleFontSize;
        }
        final String backgroundColor = getFeature(BACKGROUND_COLOR);
        if (backgroundColor != null) {
            XEShopDecoration.sNavStyle.backgroundColor = backgroundColor;
        }
        final String backIconImageName = getFeature(BACK_ICON_IMAGE_NAME);
        if (backIconImageName != null) {
            XEShopDecoration.sNavStyle.backIconImageName = backIconImageName;
        }
        final String closeIconImageName = getFeature(CLOSE_ICON_IMAGE_NAME);
        if (closeIconImageName != null) {
            XEShopDecoration.sNavStyle.backIconImageName = closeIconImageName;
        }
        final String shareIconImageName = getFeature(SHARE_ICON_IMAGE_NAME);
        if (backIconImageName != null) {
            XEShopDecoration.sNavStyle.shareIconImageName = shareIconImageName;
        }

        APICloudUtils.success(moduleContext);
    }

    public void jsmethod_setTitle(final UZModuleContext moduleContext) {
        XEShopDecoration.sTitle = moduleContext.optString(TITLE);
        APICloudUtils.success(moduleContext);
    }

    public void jsmethod_setNavStyle(final UZModuleContext moduleContext) {
        XEShopDecoration.sNavStyle.titleColor(moduleContext.optString(TITLE_COLOR));
        XEShopDecoration.sNavStyle.titleFontSize(moduleContext.optInt(TITLE_FONT_SIZE));
        XEShopDecoration.sNavStyle.backgroundColor(moduleContext.optString(BACKGROUND_COLOR));
        XEShopDecoration.sNavStyle.backIconImageName(moduleContext.optString(BACK_ICON_IMAGE_NAME));
        XEShopDecoration.sNavStyle.closeIconImageName(moduleContext.optString(CLOSE_ICON_IMAGE_NAME));
        XEShopDecoration.sNavStyle.shareIconImageName(moduleContext.optString(SHARE_ICON_IMAGE_NAME));
        APICloudUtils.success(moduleContext);
    }

    public void jsmethod_open(final UZModuleContext moduleContext) {
        String shopUrl = moduleContext.optString("url");
        if (shopUrl == null) {
            shopUrl = getFeature(SHOP_URL);
        }

        if (TextUtils.isEmpty(shopUrl)) {
            APICloudUtils.error(moduleContext, "Missing shop url");
            return;
        }

        XEShopEventEmitter.getInstance().observe(new JsBridgeListener() {
            @Override
            public void onJsInteract(int action, JsCallbackResponse jsCallbackResponse) {
                JSONObject event = new XEShopEvent(action, jsCallbackResponse).asResult();
                if (event != null) {
                    APICloudUtils.sendResult(moduleContext, event);
                }
            }
        });

        XEShopActivity.launch(context(), shopUrl);
    }

    public void jsmethod_synchronizeToken(final UZModuleContext moduleContext) {
        final String tokenKey = moduleContext.optString("token_key");
        final String tokenValue = moduleContext.optString("token_value");

        XETokenModel.getInstance().setToken(tokenKey, tokenValue);
    }

    public void jsmethod_logoutSDK(final UZModuleContext moduleContext) {
        XiaoEWeb.userLogout(context());
    }

    public void jsmethod_isLog(final UZModuleContext moduleContext) {
        final boolean isLog = moduleContext.optBoolean("isLog");
        XiaoEWeb.isOpenLog(isLog);
    }

    public ModuleResult jsmethod_getSdkVersion_sync(final UZModuleContext moduleContext) {
        final String version = XiaoEWeb.getSdkVersion();

        return new ModuleResult(version);
    }

    @Override
    protected void onClean() {
        super.onClean();
        XEShopEventEmitter.getInstance().release();
    }

    public String getFeature(String key) {
        return getFeatureValue(FEATURE_NAME, key);
    }

    public boolean getBooleanFeature(String key, boolean fallback) {
        String value = getFeature(key);
        return value != null ? "true".equalsIgnoreCase(value) : fallback;
    }

    public Integer getIntFeature(String key) {
        final String value = getFeature(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static class XEShopEvent {
        private int mCode;
        private String mMessage;
        private String mData;

        XEShopEvent(int action, JsCallbackResponse response) {
            switch (action) {
                case JsInteractType.LOGIN_ACTION:
                    mCode = 501;
                    mMessage = "登录通知";
                    break;
                case JsInteractType.SHARE_ACTION:
                    mCode = 503;
                    mMessage = "分享通知";
                    break;
                default:
                    mCode = action;
                    mMessage = "";
                    break;
            }
            mData = response.getResponseData();
        }

        JSONObject asResult() {
            try {
                JSONObject result = new JSONObject();
                result.put("code", mCode);
                result.put("message", mMessage);
                result.put("data", mData);
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
