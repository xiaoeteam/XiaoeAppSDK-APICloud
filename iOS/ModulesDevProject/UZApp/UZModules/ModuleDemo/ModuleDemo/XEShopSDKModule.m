//
//  UZModuleDemo.m
//  UZModule
//
//  Created by kenny on 14-3-5.
//  Copyright (c) 2014年 APICloud. All rights reserved.
//

#import "XEShopSDKModule.h"
#import "NSDictionaryUtils.h"
#import <XEShopSDK/XEShopSDK.h>
#import "UZAppDelegate.h"
#import "XEWebViewController.h"

#define NSNull2Nil(_x_) if([_x_ isKindOfClass: NSNull.class]) _x_ = nil;

@interface XEShopSDKModule() <XEWebViewControllerDelegate>

@property (nonatomic, strong) UZModuleMethodContext *context;

// 网页
@property (nonatomic, strong) XEWebView *wbView;

// UI 配置
@property(nonatomic, copy) NSString *title;
@property(nonatomic, strong) UIColor *titleColor;
@property(nonatomic, strong) UIColor *navViewColor;
@property(nonatomic, assign) CGFloat titleFontSize;
@property(nonatomic, copy) NSString *backImageName;
@property(nonatomic, copy) NSString *shareImageName;
@property(nonatomic, copy) NSString *closeImageName;

@end

@implementation XEShopSDKModule

#pragma mark - Override

+ (void)onAppLaunch:(NSDictionary *)launchOptions {
    // 方法在应用启动时被调用
    NSDictionary *feature = [[UZAppDelegate appDelegate] getFeatureByName:@"xeShopSDK"];
    NSString *clientId = [feature stringValueForKey:@"clientId" defaultValue:@""];
    NSString *appId = [feature stringValueForKey:@"appId" defaultValue:@""];
    NSString *scheme = [feature stringValueForKey:@"scheme" defaultValue:@""];
    BOOL isLog = [feature boolValueForKey:@"isLog" defaultValue:false];

    // 创建配置 clientId 从小鹅通申请的 sdk 应用 Id， appId 从小鹅通申请的店铺 Id
    XEConfig *config = [[XEConfig alloc] initWithClientId:clientId appId: appId];
    // 关闭 SDK 日志输出
    config.enableLog = isLog;
    // 配置 Scheme 以便微信支付完成后跳转返回
    config.scheme = scheme; // 你的scheme
    // 初始化 XESDK
    [XESDK.shared initializeSDKWithConfig:config];
}

- (id)initWithUZWebView:(UZWebView *)webView {
    if (self = [super initWithUZWebView:webView]) {
    }
    return self;
}

- (void)dispose {
    // 方法在模块销毁之前被调用
}

#pragma mark - js methods
/**
 普通方法，方法会在主线程执行，结果通过回调的方式回传js，方法名以jsmethod_作为前缀，如：- (void)jsmethod_showAlert:(UZModuleMethodContext *)context，为了方便一般使用JS_METHOD宏来定义
 */
// MARK: 初始化
JS_METHOD(init:(UZModuleMethodContext *)context) {
}

// MARK: 同步登录态
JS_METHOD(synchronizeToken:(UZModuleMethodContext *)context) {
    NSDictionary *param = context.param;
    NSString *token_key = [param stringValueForKey:@"token_key" defaultValue:nil];
    NSString *token_value = [param stringValueForKey:@"token_value" defaultValue:nil];
    
    NSNull2Nil(token_key);
    NSNull2Nil(token_value);
    
    [XESDK.shared synchronizeCookieKey:token_key cookieValue:token_value];
}

// MARK: 获取 SDK 版本号
JS_METHOD_SYNC(getSdkVersion:(UZModuleMethodContext *)context) {
    NSString *version = XESDK.shared.version;
    return version;
}

// MARK: 是否开启日志
JS_METHOD(isLog:(UZModuleMethodContext *)context) {
    NSDictionary *param = context.param;
    BOOL isLog = [param boolValueForKey:@"isLog" defaultValue: false];
    XESDK.shared.config.enableLog = isLog;
}

// MARK: 进入店铺页面
JS_METHOD(open:(UZModuleMethodContext *)context) {
    self.context = context;
    
    NSDictionary *param = context.param;
    NSString *url = [param stringValueForKey:@"url" defaultValue:nil];
    
    // 初始化 webview
    XEWebViewController *_XEWebViewVC = [[XEWebViewController alloc] init];
    _XEWebViewVC.delegate = self;
    _XEWebViewVC.url = url;
    _XEWebViewVC.navTitle = _title;
    _XEWebViewVC.titleFontSize = _titleFontSize;
    _XEWebViewVC.titleColor = _titleColor;
    _XEWebViewVC.navViewColor = _navViewColor;
    _XEWebViewVC.backImageName = _backImageName;
    _XEWebViewVC.shareImageName = _shareImageName;
    _XEWebViewVC.closeImageName = _closeImageName;
            
    _XEWebViewVC.modalPresentationStyle = UIModalPresentationFullScreen;
            
    UIViewController *vc = [self frontWindow].rootViewController;
    [vc presentViewController:_XEWebViewVC animated:YES completion:nil];
}

// MARK: 设置导航栏样式
JS_METHOD(setNavStyle:(UZModuleMethodContext *)context) {
    NSDictionary *param = context.param;
    NSString *titleColor = [param stringValueForKey:@"titleColor" defaultValue:nil];
    NSString *titleFontSize = [param stringValueForKey:@"titleFontSize" defaultValue:nil];
    NSString *backgroundColor = [param stringValueForKey:@"backgroundColor" defaultValue:nil];

    NSString *backIconImageName = [param stringValueForKey:@"backIconImageName" defaultValue:nil];
    NSString *closeIconImageName = [param stringValueForKey:@"closeIconImageName" defaultValue:nil];
    NSString *shareIconImageName = [param stringValueForKey:@"shareIconImageName" defaultValue:nil];
    
    NSNull2Nil(titleColor);
    NSNull2Nil(titleFontSize);
    NSNull2Nil(backgroundColor);
    
    NSNull2Nil(backIconImageName);
    NSNull2Nil(closeIconImageName);
    NSNull2Nil(shareIconImageName);
    
    self.titleColor = [self colorWithHexString:titleColor alpha:1.0];
    if (titleFontSize) {
        self.titleFontSize = [titleFontSize floatValue];
    }
    self.navViewColor = [self colorWithHexString:backgroundColor alpha:1.0];

    _backImageName = backIconImageName;
    _closeImageName = closeIconImageName;
    _shareImageName = shareIconImageName;
}

// MARK: 设置标题
JS_METHOD(setTitle:(UZModuleMethodContext *)context) {
    NSDictionary *param = context.param;
    NSString *title = [param stringValueForKey:@"title" defaultValue:nil];
    self.title = title;
}

#pragma mark - XEWebViewControllerDelegate

- (void)messagePost:(NSDictionary *)dict {
    [self.context callbackWithRet:dict err:nil delete:NO];
}

// MARK: 工具类

/**
16进制颜色转换为UIColor

@param hexColor 16进制字符串（可以以0x开头，可以以#开头，也可以就是6位的16进制）
@param opacity 透明度
@return 16进制字符串对应的颜色
*/
- (UIColor *)colorWithHexString:(NSString *)hexColor alpha:(float)opacity{
    
    if (hexColor == nil) return nil;
    
    NSString * cString = [[hexColor stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] uppercaseString];

    // String should be 6 or 8 characters
    if ([cString length] < 6) return nil;

    // strip 0X if it appears
    if ([cString hasPrefix:@"0X"]) cString = [cString substringFromIndex:2];
    if ([cString hasPrefix:@"#"]) cString = [cString substringFromIndex:1];

    if ([cString length] != 6) return nil;

    // Separate into r, g, b substrings
    NSRange range;
    range.location = 0;
    range.length = 2;
    NSString * rString = [cString substringWithRange:range];

    range.location = 2;
    NSString * gString = [cString substringWithRange:range];

    range.location = 4;
    NSString * bString = [cString substringWithRange:range];

    // Scan values
    unsigned int r, g, b;
    [[NSScanner scannerWithString:rString] scanHexInt:&r];
    [[NSScanner scannerWithString:gString] scanHexInt:&g];
    [[NSScanner scannerWithString:bString] scanHexInt:&b];

    return [UIColor colorWithRed:((float)r / 255.0f)
                           green:((float)g / 255.0f)
                            blue:((float)b / 255.0f)
                           alpha:opacity];
}

// 获取 window
- (UIWindow *)frontWindow {
    NSEnumerator *frontToBackWindows = [UIApplication.sharedApplication.windows reverseObjectEnumerator];
    for (UIWindow *window in frontToBackWindows) {
        BOOL windowOnMainScreen = window.screen == UIScreen.mainScreen;
        BOOL windowIsVisible = !window.hidden && window.alpha > 0;
        BOOL windowLevelSupported = window.windowLevel >= UIWindowLevelNormal;
        BOOL windowKeyWindow = window.isKeyWindow;
            
        if(windowOnMainScreen && windowIsVisible && windowLevelSupported && windowKeyWindow) {
            return window;
        }
    }
    return UIApplication.sharedApplication.keyWindow;
}

@end
