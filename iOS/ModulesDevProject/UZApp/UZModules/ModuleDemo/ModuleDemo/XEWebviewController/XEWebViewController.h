//
//  XEWebViewController.h
//  Pods-Runner
//
//  Created by page on 2019/12/16.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol XEWebViewControllerDelegate <NSObject>

- (void)messagePost:(NSDictionary *)dict;

@end

@interface XEWebViewController : UIViewController

@property (nonatomic, assign) id<XEWebViewControllerDelegate> delegate;

@property(nonatomic, copy) NSString *url;

@property(nonatomic, copy) NSString *navTitle;
@property(nonatomic, strong) UIColor *titleColor;
@property(nonatomic, strong) UIColor *navViewColor;
@property(nonatomic, assign) CGFloat titleFontSize;

@property(nonatomic, copy) NSString *backImageName;
@property(nonatomic, copy) NSString *shareImageName;
@property(nonatomic, copy) NSString *closeImageName;

@end

NS_ASSUME_NONNULL_END
