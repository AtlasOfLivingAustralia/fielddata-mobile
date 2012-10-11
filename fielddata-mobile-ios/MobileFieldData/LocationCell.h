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

@interface LocationCell : UITableViewCell <CLLocationManagerDelegate> {
    
    @private
    NSTimer *timer;
    CLLocationManager *locMgr;
    CLLocation *tempLocation;
    MBProgressHUD *progressIndicator;
}

@property (nonatomic, retain) UILabel* label;
@property (nonatomic, retain) UILabel* latitude;
@property (nonatomic, retain) UILabel* longitude;
@property (nonatomic, retain) UILabel* accuracy;
@property (nonatomic, retain) UIButton* startGPS;
@property (nonatomic, retain) NSMutableString* value;

-(void)setLocation:(NSString*)locationString;

@end
