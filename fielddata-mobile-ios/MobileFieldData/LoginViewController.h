//
//  LoginViewController.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 10/09/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Preferences.h"
#import "FieldDataService.h"
#import "SurveyDownloadController.h"
#import "MBProgressHUD.h"

@interface LoginViewController : UIViewController <SurveyDownloadDelegate, UITextFieldDelegate> {
    
    @private
    Preferences *preferences;
    FieldDataService *fieldDataService;
    MBProgressHUD *progressIndicator;
    
}

@property (nonatomic, retain) IBOutlet UILabel *appName;
@property (nonatomic, retain) IBOutlet UILabel *version;
@property (nonatomic, retain) IBOutlet UITextField *username;
@property (nonatomic, retain) IBOutlet UITextField *password;
@property (nonatomic, retain) IBOutlet UIButton *cancelButton;
@property (nonatomic, retain) IBOutlet UIButton *submit;

- (IBAction)onClickLogin:(id)sender;
- (IBAction)onClickCancel:(id)sender;

@end
