//
//  MapViewController.h
//  MobileFieldData
//
//  Created by Chris Godwin on 11/01/13.
//
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import "Record.h"

#define DEFAULT_ZOOM 14

@protocol MapViewControllerDelegate <NSObject>

-(void)locationSelected:(CLLocation *)selectedLocation;

@end

@interface MapViewController : UIViewController <MKMapViewDelegate> {
    IBOutlet MKMapView *mapView;
    
    CLLocationCoordinate2D centre;
    NSUInteger zoom;
}


-(id)initWithSurveyDefaults:(Survey *)survey;
-(id)initWithLocation:(CLLocation *)location;

-(IBAction) showMyLocation:(id)button;

@property (nonatomic, retain) IBOutlet MKMapView *mapView;
@property (nonatomic, retain) CLLocation *selectedLocation;
@property (nonatomic, weak) id<MapViewControllerDelegate> delegate;

@end
