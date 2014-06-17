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
-(void)polygonValues:(NSString *) polygonStr;

@end

@interface MapViewController : UIViewController <CLLocationManagerDelegate, MKMapViewDelegate>{
    IBOutlet MKMapView *mapView;
    IBOutlet UIBarButtonItem *myLocation;
    IBOutlet UIBarButtonItem *dropPin;
    IBOutlet UIToolbar *myToolBar;
    CLLocationCoordinate2D centre;
    NSUInteger zoom;
    int polygonStatus;
    CLLocationManager *locationManager;
}

-(id)initWithSurveyDefaults:(Survey *)survey polygon:(int)isPolygonEnabled polygonValue: (NSString*) polygonStr;
-(id)initWithLocation:(CLLocation *)location polygon:(int)isPolygonEnabled polygonValue: (NSString*) polygonStr;

-(IBAction) showMyLocation:(id)button;
-(IBAction) removePolygonOverlays:(id)button;
-(IBAction) dropPinAtCurrentLocation:(id)button;

@property (nonatomic, retain) IBOutlet MKMapView *mapView;
@property (nonatomic, retain) IBOutlet UIBarButtonItem *myLocation;
@property (nonatomic, retain) IBOutlet UIBarButtonItem *dropPin;
@property (nonatomic, retain) IBOutlet UIToolbar *myToolBar;
@property (nonatomic, retain) CLLocation *selectedLocation;
@property (nonatomic, assign) int polygonStatus;
@property (nonatomic, weak) id<MapViewControllerDelegate> delegate;
@property (nonatomic, retain) CLLocationManager *locationManager;
@property (nonatomic, strong) NSMutableArray *userSelectedPoints;

- (void)addGestureRecogniserToMapView;
- (void)addPinToMap:(UIGestureRecognizer *)gestureRecognizer;
@end
