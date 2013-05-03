//
//  SurveyDownloadProgressDelegate.h
//  MobileFieldData
//
//  Created by Chris Godwin on 3/05/13.
//
//

#import <Foundation/Foundation.h>
#import "FieldDataService.h"

@protocol SurveyDownloadDelegate <NSObject>
@required
- (void)downloadSurveysSuccessful;
- (void)downloadSurveysFailed;
@end


@interface SurveyDownloadController : NSObject <FieldDataServiceDelegate>

@property (nonatomic, retain) id<SurveyDownloadDelegate> delegate;
-(id)initWithView:(UIView*)view;
-(void)downloadSurveys;

@end
