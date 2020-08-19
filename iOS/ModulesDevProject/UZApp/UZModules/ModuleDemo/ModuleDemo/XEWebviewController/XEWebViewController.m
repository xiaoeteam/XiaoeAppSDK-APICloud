//
//  XEWebViewController.m
//  Pods-Runner
//
//  Created by page on 2019/12/16.
//

#import "XEWebViewController.h"
#import <XEShopSDK/XEShopSDK.h>

#define ResourcePath @"res_xeShopSDK/"

@interface XEWebViewController ()<XEWebViewDelegate, XEWebViewNoticeDelegate>

@property(nonatomic, strong) XEWebView *webView;

@property(nonatomic, strong) UIButton *backBtn;
@property(nonatomic, strong) UIButton *closeBtn;
@property(nonatomic, strong) UIButton *shareBtn;

@property(nonatomic, strong) UIView *navView;

@end

@implementation XEWebViewController

- (void)dealloc {
    _webView.noticeDelegate = nil;
    _webView.delegate = nil;
    _webView = nil;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self setUp];
}

- (void)setUp {
    
    self.navigationController.navigationBarHidden = YES;
    self.view.backgroundColor = [UIColor whiteColor];
    
    CGFloat navHeight = 64;
    if ([UIScreen mainScreen].bounds.size.height >= 812
        && [UIScreen mainScreen].bounds.size.height < 1024) {
        navHeight = 88;
    }
    
    CGFloat statusHeight = UIApplication.sharedApplication.statusBarFrame.size.height;
    
    // nav view
    _navView = [[UIView alloc] initWithFrame: CGRectMake(0, 0, [UIScreen mainScreen].bounds.size.width, navHeight)];
    _navView.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:_navView];
    
    if (_navViewColor) {
        _navView.backgroundColor = _navViewColor;
    }
        
    NSString *imagePath = [[NSBundle mainBundle].bundlePath stringByAppendingPathComponent: [self getImageName:@"nav_icon_back"]];
    NSString *shareImagePath = [[NSBundle mainBundle].bundlePath stringByAppendingPathComponent: [self getImageName:@"nav_icon_share"]];
    NSString *closeImagePath = [[NSBundle mainBundle].bundlePath stringByAppendingPathComponent: [self getImageName:@"close"]];
    
    UIImage *backImage = [UIImage imageWithContentsOfFile:imagePath];
    UIImage *shareImage = [UIImage imageWithContentsOfFile:shareImagePath];
    UIImage *closeImage = [UIImage imageWithContentsOfFile:closeImagePath];
    
    // 读取 MainBundle 图片
    if (_backImageName.length > 0) {
        backImage = [self getImage:_backImageName];
    }
    
    if (_shareImageName.length > 0) {
        shareImage = [self getImage:_shareImageName];
    }
    
    if (_closeImageName.length > 0) {
        closeImage = [self getImage:_closeImageName];
    }
    
    // back button
    _backBtn = [[UIButton alloc] initWithFrame:CGRectMake(15, statusHeight, 44, 44)];
    _backBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
    [_backBtn setImage:backImage forState:UIControlStateNormal];
    [_backBtn addTarget:self action:@selector(backBtnAction) forControlEvents:UIControlEventTouchUpInside];
    [_navView addSubview:_backBtn];
    
    _closeBtn = [[UIButton alloc] initWithFrame:CGRectMake(15 + 44, statusHeight, 44, 44)];
    _closeBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
    [_closeBtn setImage:closeImage forState:UIControlStateNormal];
    [_closeBtn addTarget:self action:@selector(closeBtnAction) forControlEvents:UIControlEventTouchUpInside];
    [_navView addSubview:_closeBtn];
    
    // share button
    _shareBtn = [[UIButton alloc] initWithFrame:CGRectMake([UIScreen mainScreen].bounds.size.width - 15 - 44,  statusHeight, 44, 44)];
    _shareBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentRight;
    [_shareBtn setImage:shareImage forState:UIControlStateNormal];
    [_shareBtn addTarget:self action:@selector(shareBtnAction) forControlEvents:UIControlEventTouchUpInside];
    [_navView addSubview:_shareBtn];
    
    // title
    UILabel *title = [[UILabel alloc] init];
    title.textAlignment = NSTextAlignmentCenter;
    title.text = self.navTitle;
    title.textColor = [UIColor blackColor];
    title.frame = CGRectMake(80, statusHeight, [[UIScreen mainScreen] bounds].size.width - 160, 44);
    [_navView addSubview:title];
    
    if (_titleColor) {
        title.textColor = _titleColor;
    }
    
    if (_titleFontSize) {
        title.font = [UIFont systemFontOfSize:_titleFontSize];
    }
    
    CGRect webFrame = CGRectMake(0, navHeight, [UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.height - navHeight);
    UIView *contentView = [[UIView alloc] initWithFrame:webFrame];
    [self.view addSubview:contentView];
    
    _webView = [[XEWebView alloc] initWithFrame:contentView.bounds];
    _webView.delegate = self;
    _webView.noticeDelegate = self;
    [contentView addSubview:_webView];
    
    NSURL *requestUrl = [NSURL URLWithString:_url];
    NSURLRequest *request = [NSURLRequest requestWithURL:requestUrl];
    [_webView loadRequest:request];
}

- (UIImage *)getImage:(NSString *)imageName {
    NSString *imagePath = [[NSBundle mainBundle].bundlePath stringByAppendingPathComponent: [self getImageName:imageName]];
    return [UIImage imageWithContentsOfFile:imagePath];
}

- (NSString *)getImageName:(NSString *)imageName {
    NSString *name = [[NSString alloc] initWithFormat:@"%@%@@2x", ResourcePath, imageName];
    if (UIScreen.mainScreen.scale == 1.0) {
        name = [[NSString alloc] initWithFormat:@"%@%@", ResourcePath, imageName];
    } else if (UIScreen.mainScreen.scale == 2.0) {
        name = [[NSString alloc] initWithFormat:@"%@%@@2x", ResourcePath, imageName];
    } else if (UIScreen.mainScreen.scale == 3.0) {
        name = [[NSString alloc] initWithFormat:@"%@%@@3x", ResourcePath, imageName];
    }
    
    return name;
}

- (UIModalPresentationStyle)modalPresentationStyle {
    return UIModalPresentationFullScreen;
}

- (void)backBtnAction {
     if (_webView.canGoBack) {
         [_webView goBack];
     } else {
         [self dismissViewControllerAnimated:YES completion:nil];
     }
}

- (void)closeBtnAction {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)shareBtnAction {
    [_webView share];
}

- (void)messagePost:(NSDictionary *)dict {
    dispatch_async(dispatch_get_main_queue(), ^{
         if ([self.delegate respondsToSelector:@selector(messagePost:)]) {
             [self.delegate messagePost:dict];
         }
    });
}

 #pragma mark - XEWebViewNotice Delegate

 - (void)webView:(id<XEWebView>)webView didReceiveNotice:(XENotice *)notice
 {
     
     switch (notice.type) {
         case XENoticeTypeLogin:
         {
             // 登录通知
             NSMutableDictionary *dict = [NSMutableDictionary dictionary];
             [dict setObject:[NSNumber numberWithInt:501] forKey:@"code"];
             [dict setObject:@"登录通知" forKey:@"message"];
             [dict setObject:@"" forKey:@"data"];

             [self messagePost:dict];
             
         }
             break;
         case XENoticeTypeShare:
         {
             // 接收到分享请求的结果回调
             NSMutableDictionary *dict = [NSMutableDictionary dictionary];
             NSDictionary *response = (NSDictionary *)notice.response;
             [dict setObject:[NSNumber numberWithInt:503] forKey:@"code"];
             [dict setObject:@"分享通知" forKey:@"message"];
             response ? [dict setObject:response forKey:@"data"] :  [dict setObject:@"" forKey:@"data"];
             [self messagePost:dict];
         }
             break;
         default:
             break;
     }
 }


 #pragma mark - XEWebViewDelegate Delegate

 - (BOOL)webView:(id<XEWebView>)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
 {
     return YES;
 }

 - (void)webViewDidStartLoad:(id<XEWebView>)webView
 {
     NSMutableDictionary *dict = [NSMutableDictionary dictionary];
     [dict setObject:[NSNumber numberWithInt:401] forKey:@"code"];
     [dict setObject:@"开始加载" forKey:@"message"];
     [dict setObject:@"success" forKey:@"data"];
 }

 - (void)webViewDidFinishLoad:(id<XEWebView>)webView
 {
     NSMutableDictionary *dict = [NSMutableDictionary dictionary];
     [dict setObject:[NSNumber numberWithInt:402] forKey:@"code"];
     [dict setObject:@"加载完成" forKey:@"message"];
     [dict setObject:@"success" forKey:@"data"];
 }

 - (void)webView:(id<XEWebView>)webView didFailLoadWithError:(NSError *)error
 {
     NSMutableDictionary *dict = [NSMutableDictionary dictionary];
     [dict setObject:[NSNumber numberWithInt:403] forKey:@"code"];
     [dict setObject:@"加载出错" forKey:@"message"];
     [dict setObject:@"error" forKey:@"data"];
 }

#pragma mark - Autorotate

- (BOOL)shouldAutorotate {
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations{
    return UIInterfaceOrientationMaskPortrait;
}

@end
