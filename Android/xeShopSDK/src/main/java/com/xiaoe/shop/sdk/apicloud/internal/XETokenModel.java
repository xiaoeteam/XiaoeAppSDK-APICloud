package com.xiaoe.shop.sdk.apicloud.internal;

public class XETokenModel {
    private static volatile XETokenModel sInstance = new XETokenModel();

    private String mTokenKey;
    private String mTokenValue;

    private XETokenObserver mObserver;

    private XETokenModel() {
    }

    public static XETokenModel getInstance() {
        return sInstance;
    }

    public void setToken(String tokenKey, String tokenValue) {
        mTokenKey = tokenKey;
        mTokenValue = tokenValue;
        dispatchingValue();
    }

    public void observe(XETokenObserver observer) {
        mObserver = observer;
        dispatchingValue();
    }

    public void removeObserver(XETokenObserver observer) {
        mObserver = null;
    }

    private void dispatchingValue() {
        if (mObserver == null) {
            return;
        }

        mObserver.onChanged(mTokenKey, mTokenValue);
    }

    public interface XETokenObserver {

        void onChanged(String tokenKey, String tokenValue);
    }
}
