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
#import "SurveyInputCell.h"

@protocol LocationCellDelegate
-(void)showMap;
-(void)locationFound:(CLLocation*)location;
@end

@interface LocationCell : SurveyInputCell <CLLocationManagerDelegate> {
    
    @private
    NSTimer *timer;
    CLLocationManager *locMgr;
    CLLocation *tempLocation;
    MBProgressHUD *progressIndicator;
}

@property (nonatomic, retain) UILabel* latitude;
@property (nonatomic, retain) UILabel* longitude;
@property (nonatomic, retain) UILabel* accuracy;
@property (nonatomic, retain) UIButton* startGPS;
@property (nonatomic, retain) UIButton* showMap;
@property (nonatomic, retain) NSMutableString* value;
@property (nonatomic, weak) id<LocationCellDelegate> delegate;

-(void)setFoundLocation:(CLLocation*)location;
-(void)setLocation:(NSString*)locationString;
- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier;

@end
