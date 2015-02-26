//
//  LocationCell.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 2/10/12.
//
//

#import "LocationCell.h"
#import "AlertService.h"
#import "SurveyViewController.h"
#import "Constant.h"

@implementation LocationCell

@synthesize startGPS, latitude, longitude, accuracy, showMap, delegate, polygon ;

- (id)initWithStyleEnablePolygon:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    
    if (self) {
        self.label.text = @"Location *";
        polygon = [[UILabel alloc] initWithFrame:CGRectMake(100, 66, 250, 24)];
        polygon.font = [UIFont systemFontOfSize:12.0];
        polygon.text = @"Total polygon points : 0" ;
        
        showMap = [UIButton buttonWithType:UIButtonTypeRoundedRect];
        [showMap setTitle:@"Select locations using Map >" forState:UIControlStateNormal];
        [showMap addTarget:self action:@selector(showMap:) forControlEvents:UIControlEventTouchUpInside];
        showMap.frame = CGRectMake(10, 24, 300, 44);

        [self.contentView addSubview:showMap];
        [self.contentView addSubview:polygon];
    }
    return self;
}

- (void) buildComponents
{
    self.label.text = @"Location *";
    
    latitude = [[UILabel alloc] initWithFrame:CGRectMake(10, 24, 200, 24)];
    latitude.font = [UIFont systemFontOfSize:12.0];
    latitude.text = @"Latitude: Not Found";
    [self.contentView addSubview:latitude];
    
    longitude = [[UILabel alloc] initWithFrame:CGRectMake(10, 48, 200, 24)];
    longitude.font = [UIFont systemFontOfSize:12.0];
    longitude.text = @"Longitude: Not Found";
    [self.contentView addSubview:longitude];
    
    accuracy = [[UILabel alloc] initWithFrame:CGRectMake(10, 72, 200, 24)];
    accuracy.font = [UIFont systemFontOfSize:12.0];
    accuracy.text = @"Accuracy: NA";
    [self.contentView addSubview:accuracy];
    
    //UIImage *gpsImg = [UIImage imageNamed:@"gps.png"];
    startGPS = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    [startGPS setTitle:@"Find Me" forState:UIControlStateNormal];
    [startGPS addTarget:self action:@selector(findCurrentLocation:) forControlEvents:UIControlEventTouchUpInside];
    startGPS.frame = CGRectMake(200, 10, 100, 44);
    //[startGPS setImage:gpsImg forState:UIControlStateNormal];
    [self.contentView addSubview:startGPS];
    
    showMap = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    [showMap setTitle:@"Map" forState:UIControlStateNormal];
    [showMap addTarget:self action:@selector(showMap:) forControlEvents:UIControlEventTouchUpInside];
    showMap.frame = CGRectMake(200, 64, 100, 44);
    [self.contentView addSubview:showMap];

}

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        [self buildComponents];
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(void)setLocation:(NSString*)locationString
{
    if (locationString == nil) {
        locationString = @"";
    }
    NSArray* locDescArr = [locationString componentsSeparatedByString:@","];
    
    if (locDescArr.count == 3) {
        latitude.text = [NSString stringWithFormat:@"Latitude: %@", [locDescArr objectAtIndex:0]];
        longitude.text = [NSString stringWithFormat:@"Latitude: %@", [locDescArr objectAtIndex:1]];
        accuracy.text = [NSString stringWithFormat:@"Accuracy: %@m", [locDescArr objectAtIndex:2]];
    }
    self.value = locationString;
    
}

-(void)showProgressIndicator
{
    // add the progress indicator to the view
    progressIndicator = [MBProgressHUD showHUDAddedTo:self.superview animated:YES];
    
    // Set properties
    //self.progressIndicator.delegate = self;
    progressIndicator.labelText = @"Finding Location";
}

-(void)hideProgressIndicator
{
    [MBProgressHUD hideAllHUDsForView:self.superview animated:YES];
}

-(IBAction)findCurrentLocation:(id)sender
{
    // make the query execute on another thread so that the progress indicator will be shown....
    dispatch_queue_t dispatchQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    
    dispatch_async(dispatchQueue, ^(void){
        dispatch_sync(dispatch_get_main_queue(), ^{
            // Show the progress indicator
            [self showProgressIndicator];
        });
            
        dispatch_sync(dispatch_get_main_queue(), ^{
    
            timer = [NSTimer scheduledTimerWithTimeInterval:15.0
                                                     target:self
                                                   selector:@selector(gpsTimeout:)
                                                   userInfo:nil
                                                    repeats:NO];
            
            locMgr = [[CLLocationManager alloc] init];
            locMgr.delegate = self;
            locMgr.desiredAccuracy = 20;
           
            // Check for iOS 8. Without this guard the code will crash with "unknown selector" on iOS 7.
            if ([locMgr respondsToSelector:@selector(requestWhenInUseAuthorization)]) {
                [locMgr requestWhenInUseAuthorization];
            }
            
            [locMgr startUpdatingLocation];
        });
    });
}

-(IBAction)showMap:(id)sender
{
    [delegate showMap];
}

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation
                                                     fromLocation:(CLLocation *)oldLocation {
    if (newLocation.horizontalAccuracy <= 20.0) {
        
        [self setFoundLocation:newLocation];
        [delegate locationFound:newLocation];
        [timer invalidate];
        
    } else {
        tempLocation = newLocation;
    }
	
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
	NSLog(@"%@", error);
}


-(void) gpsTimeout:(NSTimer*) theTimer {
    
    [self setFoundLocation:tempLocation];
    [AlertService DisplayMessageWithTitle:@"GPS Accuracy Error" message:@"GPS Location not within 20m, please try again."];
}


-(void)setFoundLocation:(CLLocation*)location {
    
    latitude.text = [NSString stringWithFormat:@"Latitude: %f", location.coordinate.latitude];
    longitude.text = [NSString stringWithFormat:@"Latitude: %f", location.coordinate.longitude];
    accuracy.text = [NSString stringWithFormat:@"Accuracy: %.1fm", location.horizontalAccuracy];
    
    [locMgr stopUpdatingLocation];
    [self hideProgressIndicator];
    
    NSString* locDescription = [NSString stringWithFormat:@"%f,%f,%.1f",
                                    location.coordinate.latitude,
                                    location.coordinate.longitude,
                                    location.horizontalAccuracy];
    self.value = locDescription;
}

/*
    Example polygon values for the attribute locationWkt
    MULTIPOLYGON (((149.11545284092426 -35.27517999592148, 149.11045286804438 -35.27794775826639, 149.11560237407684 -35.27968194090294, 149.11545284092426 -35.27517999592148)))
*/
- (void)setPolygonValues:(NSString*)polygonStr {
    
    if(polygonStr != nil) {
        //Model
        self.value = polygonStr;

        //Single point,
        NSString *regEx = [NSString stringWithFormat:@".*%@.*", @kPOLYGON_STR];
        NSRange range = [polygonStr rangeOfString:regEx options:NSRegularExpressionSearch];
        if(range.location == NSNotFound){
             NSArray* polyArray = [polygonStr componentsSeparatedByString:@","];
             if(polyArray != nil && [polyArray count] == kPOINT_FIELDS)
                  polygon.text = [[NSString alloc]initWithFormat:@"Total polygon points : 1"];
             else
                  polygon.text = [[NSString alloc]initWithFormat:@"Total polygon points : 0"];
            return;
        }

        //Multi point
        polygonStr = [polygonStr stringByReplacingOccurrencesOfString:@kPOLYGON_START withString:@""];
        polygonStr = [polygonStr stringByReplacingOccurrencesOfString:@kPOLYGON_END withString:@""];
        NSArray* polyArray = [polygonStr componentsSeparatedByString:@","];
        if(polyArray != nil && [polyArray count] > 1)
            polygon.text = [[NSString alloc]initWithFormat:@"Total polygon points : %d",[polyArray count]-1];
        else
            polygon.text = [[NSString alloc]initWithFormat:@"Total polygon points : %d",[polyArray count]];
        
    }
}

@end
