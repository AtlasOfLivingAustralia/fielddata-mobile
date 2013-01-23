//
//  LocationCell.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 2/10/12.
//
//

#import <UIKit/UIKit.h>
#import <CoreLocation/CoreLocation.h>
#import "MBProgressHUD.h"
@class SurveyViewController;

@interface LocationCell : UITableViewCell <CLLocationManagerDelegate> {
    
    @private
    NSTimer *timer;
    CLLocationManager *locMgr;
    CLLocation *tempLocation;
    MBProgressHUD *progressIndicator;
    SurveyViewController *parentController;
}

@property (nonatomic, retain) UILabel* label;
@property (nonatomic, retain) UILabel* latitude;
@property (nonatomic, retain) UILabel* longitude;
@property (nonatomic, retain) UILabel* accuracy;
@property (nonatomic, retain) UIButton* startGPS;
@property (nonatomic, retain) UIButton* showMap;
@property (nonatomic, retain) NSMutableString* value;

-(void)setFoundLocation:(CLLocation*)location;
-(void)setLocation:(NSString*)locationString;
- (id)initWithStyleAndParent:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier parent:(SurveyViewController *) parent;

@end
