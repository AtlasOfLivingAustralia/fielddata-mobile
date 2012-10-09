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
#import "MBProgressHUD.h"

@interface LoginViewController : UIViewController <FieldDataServiceDelegate, UITextFieldDelegate> {
    
    @private
    Preferences *preferences;
    FieldDataService *fieldDataService;
    int numSurveys;
    int surveyCount;
    MBProgressHUD *progressIndicator;
}

@property (nonatomic, retain) IBOutlet UITextField *username;
@property (nonatomic, retain) IBOutlet UITextField *password;

- (IBAction)onClickLogin:(id)sender;

@end
