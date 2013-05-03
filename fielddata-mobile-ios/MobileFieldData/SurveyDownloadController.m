//
//  SurveyDownloadProgressDelegate.m
//  MobileFieldData
//
//  Created by Chris Godwin on 3/05/13.
//
//
#import "FieldDataService.h"
#import "SurveyDownloadController.h"
#import "AlertService.h"
#import "MBProgressHUD.h"


@interface SurveyDownloadController() {
    FieldDataService* _fieldDataService;
    int numSurveys;
    int surveyCount;
    MBProgressHUD *progressIndicator;
    UIView* _view;
}

@end

@implementation SurveyDownloadController

@synthesize delegate;

-(id)initWithView:(UIView*)view
{
    self = [super init];
    if (self) {
        _view = view;
        _fieldDataService = [[FieldDataService alloc]init];
        _fieldDataService.delegate = self;
    }
    return self;
}

-(void)downloadSurveys {
    [_fieldDataService begin];
    [_fieldDataService downloadSurveys:YES];
    [self showProgressIndicator];
}


- (void)downloadSurveysSuccessful:(BOOL)success surveyArray:(NSArray*)surveys {
    
    if (!success) {
        [self hideProgressIndicator];
        [AlertService DisplayMessageWithTitle:@"Network Error" message:@"Downloading Field Data surveys failed."];
        [_fieldDataService rollback];
        [self.delegate downloadSurveysFailed];
    } else {
        
        numSurveys = [surveys count];
        [self updateProgress];
        NSMutableArray *downloadedSurveys = [[NSMutableArray alloc] init];
        for (NSDictionary* survey in surveys) {
            
            //NSString* surveyId = [survey objectForKey:@"id"];
            NSNumber* surveyId = [survey objectForKey:@"id"];
            @try {
                NSLog(@"Downloading survey %@",surveyId);
                [_fieldDataService downloadSurveyDetails:surveyId.stringValue downloadedSurveys:[downloadedSurveys copy]];
                [downloadedSurveys addObject:surveyId.stringValue];
            }
            @catch (NSException *e) {
                NSLog(@"Exception %@", e);
                break;
                [_fieldDataService rollback];
                [self.delegate downloadSurveysFailed];
            }
        }
    }
}

- (void)downloadSurveyDetailsSuccessful:(BOOL)success survey:(NSDictionary*)survey {
    
    surveyCount++;
    if (surveyCount >= numSurveys) {
        [_fieldDataService commit];
        [self hideProgressIndicator];
        [self.delegate downloadSurveysSuccessful];
    }
    else {
        [self updateProgress];
    }
}

-(void)updateProgress
{
    progressIndicator.labelText = [NSString stringWithFormat:@"Downloading survey %d of %d", surveyCount+1, numSurveys];
    
}


-(void)showProgressIndicator
{
    // add the progress indicator to the view
    progressIndicator = [MBProgressHUD showHUDAddedTo:_view animated:YES];
    
    // Set properties
    progressIndicator.labelText = @"Downloading surveys";
}

-(void)hideProgressIndicator
{
    [MBProgressHUD hideAllHUDsForView:_view animated:YES];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return NO;
}


@end
