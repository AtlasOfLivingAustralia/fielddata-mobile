//
//  LoginViewController.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 10/09/12.
//  Copyright (c) 2012 CSIRO. All rights reserved.
//

#import "LoginViewController.h"
#import "RFRequest.h"
#import "RFResponse.h"
#import "RFService.h"
#import "Preferences.h"
#import "AlertService.h"

@interface LoginViewController ()

@end

@implementation LoginViewController

@synthesize username, password;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
        preferences = [[Preferences alloc]init];
        fieldDataService = [[FieldDataService alloc]init];
        fieldDataService.delegate = self;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Do any additional setup after loading the view from its nib.
    if ([preferences getFieldDataSessionKey]) {
        [AlertService DisplayMessageWithTitle:@"Warning"
                                      message:@"Changing the logged in user will delete any survey recordings that have not yet been uploaded."];
        [self.cancelButton setTitle:@" Cancel" forState:UIControlStateNormal];
    } //else {
        //self.cancelButton.hidden = YES;
    //}
    
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.username = nil;
    self.password = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (IBAction)onClickLogin:(id)sender
{
    [username resignFirstResponder];
    [password resignFirstResponder];
    
    // make the query execute on another thread so that the progress indicator will be shown....
    dispatch_queue_t dispatchQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    
    dispatch_async(dispatchQueue, ^(void){
        
        dispatch_sync(dispatch_get_main_queue(), ^{
            // Show the progress indicator
            [self showProgressIndicator];
        });
        
        dispatch_sync(dispatch_get_main_queue(), ^{
        
            NSString *portalName = [preferences getPortalName];
            
            NSString *url = [preferences getFieldDataURL];
            
            RFRequest *r = [RFRequest requestWithURL:[NSURL URLWithString:url] type:RFRequestMethodGet
                              resourcePathComponents:@"survey", @"login", nil];
            
            [r addParam:portalName forKey:@"portalName"];
            [r addParam:self.username.text forKey:@"username"];
            [r addParam:self.password.text forKey:@"password"];
            
            //now execute this request and fetch the response in a block
            [RFService execRequest:r completion:^(RFResponse *response){
                [self handleLoginResponse:response];
            }];
        });
    });
}

- (IBAction)onClickCancel:(id)sender
{
    if ([preferences getFieldDataSessionKey]) {
        [self dismissModalViewControllerAnimated:YES];
    } else {
        NSString* path = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"Portal path"];

        NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"http://root.ala.org.au%@/vanilla/usersignup.htm", path]];
        [[UIApplication sharedApplication] openURL:url];
    }
}

- (void)handleLoginResponse:(RFResponse*)response
{
    [self hideProgressIndicator];
    if (!response.error) {
        NSLog(@"%@", response); //print out full response
        
        NSError *error;
        NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:response.dataValue
                                                                   options:kNilOptions error:&error];
        NSString *ident = [dictionary valueForKey:@"ident"];
        NSLog(@"%@", ident);
        
        NSDictionary *user = [dictionary valueForKey:@"user"];
        
        NSString *name = [NSString stringWithFormat:@"%@ %@",
                          [user valueForKey:@"firstName"],
                          [user valueForKey:@"lastName"]];
        NSNumber *userId = [user valueForKey:@"server_id"];
        
        if (ident == NULL) {
            [AlertService DisplayMessageWithTitle:@"Login Failed" message:@"Username or Password not recognised"];
        } else {
            [preferences setFieldDataSessionKey:ident];
            [preferences setUsersName:name];
            [preferences setUserId:userId];
            
            
            // download the new surveys
            SurveyDownloadController* downloadController = [[SurveyDownloadController alloc]initWithView:self.view];
            downloadController.delegate = self;
            [downloadController downloadSurveys];
        }
    } else {
        [AlertService DisplayAlertWithError:response.error title:@"Login Error"];
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return NO;
}

-(void)downloadSurveysSuccessful {
    [self dismissModalViewControllerAnimated:YES];
}

-(void)downloadSurveysFailed {
    
}

-(void)showProgressIndicator
{
    // add the progress indicator to the view
    progressIndicator = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    // Set properties
    progressIndicator.labelText = @"Logging in";
}

-(void)hideProgressIndicator
{
    [MBProgressHUD hideAllHUDsForView:self.view animated:YES];
}

@end
