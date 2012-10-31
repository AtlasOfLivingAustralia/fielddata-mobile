//
//  LocationCell.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 2/10/12.
//
//

#import "LocationCell.h"
#import "AlertService.h"

@implementation LocationCell

@synthesize label, startGPS, latitude, longitude, accuracy, value;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        label = [[UILabel alloc] initWithFrame:CGRectMake(10, 0, self.bounds.size.width-20, 24)];
        label.font = [UIFont boldSystemFontOfSize:12.0];
        label.numberOfLines = 0;
        label.text = @"My Current Location";
        [self.contentView addSubview:label];
        
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
        startGPS.frame = CGRectMake(200, 24, 100, 44);
        //[startGPS setImage:gpsImg forState:UIControlStateNormal];
        [self.contentView addSubview:startGPS];
        
        value = [[NSMutableString alloc]init];
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
    NSArray* locDescArr = [locationString componentsSeparatedByString:@","];
    
    if (locDescArr.count == 3) {
        latitude.text = [NSString stringWithFormat:@"Latitude: %@", [locDescArr objectAtIndex:0]];
        longitude.text = [NSString stringWithFormat:@"Latitude: %@", [locDescArr objectAtIndex:1]];
        accuracy.text = [NSString stringWithFormat:@"Accuracy: %@m", [locDescArr objectAtIndex:2]];
    }
    
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
            [locMgr startUpdatingLocation];
        });
    });
}

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation
                                                     fromLocation:(CLLocation *)oldLocation {
    if (newLocation.horizontalAccuracy <= 20.0) {
        
        [self setFoundLocation:newLocation];
        [timer invalidate];
        
    } else {
        tempLocation = newLocation;
    }
	NSLog(@"%f", newLocation.horizontalAccuracy);
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
    
    [value setString:locDescription];
}



@end
