//
//  MapViewController.m
//  MobileFieldData
//
//  Created by Chris Godwin on 11/01/13.
//
//

#import "MapViewController.h"
#import "MapAnnotation.h"
#import <MapKit/MapKit.h>
#import "Record.h"
#import "Survey.h"

#define MERCATOR_OFFSET 268435456
#define MERCATOR_RADIUS 85445659.44705395

@interface MapViewController () {
    
}

-(void)setCenterCoorindate:(CLLocationCoordinate2D)centerCoordinate
       zoomLevel:(NSUInteger)zoomLevel
       animated:(BOOL)animated;

-(void)handleTap:(UITapGestureRecognizer *)gestureRecognizer;

@end

@implementation MapViewController

@synthesize mapView, selectedLocation, delegate;

-(id)initWithSurveyDefaults:(Survey *)survey
{
    CLLocationCoordinate2D defaultCentre;
    defaultCentre.latitude =  [survey.mapY floatValue];
    defaultCentre.longitude = [survey.mapX floatValue];
    NSInteger defaultZoom = [survey.zoom intValue];
    
    selectedLocation = nil;
    return [self initWithCoordinate:defaultCentre zoomLevel:defaultZoom];
    
}

-(id)initWithLocation:(CLLocation *)location;
{
    self.selectedLocation = location;
    return [self initWithCoordinate:location.coordinate zoomLevel:DEFAULT_ZOOM];
    
}

- (id)initWithCoordinate:(CLLocationCoordinate2D)centreCoordinate zoomLevel:(NSUInteger)zoomLevel
{
    self = [super initWithNibName:@"MapViewController" bundle:nil];
    
    if (self) {
        centre = centreCoordinate;
        zoom = zoomLevel;
        
        self.title = @"Select a location";
        UIBarButtonItem *doneButton = [[UIBarButtonItem alloc]
                                       initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(done:)];
        
        self.navigationItem.rightBarButtonItem = doneButton;
        
        UIBarButtonItem *cancelButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel target:self action:@selector(cancel:)];
        
        self.navigationItem.leftBarButtonItem = cancelButton;
        
        
        
    }
    return self;

}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    UITapGestureRecognizer *gestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
    gestureRecognizer.numberOfTapsRequired = 1;
    [self.mapView addGestureRecognizer:gestureRecognizer];
    self.mapView.mapType = MKMapTypeHybrid;

    if (selectedLocation) {
        [self setUserLocation:selectedLocation.coordinate];
    }
    [self setCenterCoorindate:centre zoomLevel:zoom animated:YES];
    
}



- (MKAnnotationView *)mapView:(MKMapView *)mv viewForAnnotation:(id <MKAnnotation>)annotation {
    if( annotation == mv.userLocation ) return nil;
    
    MKPinAnnotationView *annotationView;
    annotationView = (MKPinAnnotationView*)[mv dequeueReusableAnnotationViewWithIdentifier:@"AnnotationIdentifier"];
    if( annotationView == nil ){
        annotationView = [[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"AnnotationIdentifier"];
    }
    annotationView.draggable = YES;
    annotationView.pinColor = MKPinAnnotationColorPurple;
  
    annotationView.canShowCallout = YES;
    annotationView.selected = YES;
    
    return annotationView;
}

- (void)mapView:(MKMapView *)aMapView didAddAnnotationViews:(NSArray *)views {
    for (int i=0; i<views.count; i++) {
        if ([views[i] isKindOfClass:[MKPinAnnotationView class]]) {
            [aMapView selectAnnotation:[views[i] annotation] animated:YES];
        }
    }
}


- (void)mapView:(MKMapView *)mapView annotationView:(MKAnnotationView *)annotationView
didChangeDragState:(MKAnnotationViewDragState)newState fromOldState:(MKAnnotationViewDragState)oldState
{
    if (newState == MKAnnotationViewDragStateEnding) {
        CLLocationCoordinate2D droppedAt = annotationView.annotation.coordinate;
        // Update the location selection.
        selectedLocation = [[CLLocation alloc] initWithLatitude:droppedAt.latitude longitude:droppedAt.longitude];
    }
}

- (double)longitudeToPixelSpaceX:(double)longitude
{
    return round(MERCATOR_OFFSET + MERCATOR_RADIUS * longitude * M_PI / 180.0);
}

- (double)latitudeToPixelSpaceY:(double)latitude
{
    return round(MERCATOR_OFFSET - MERCATOR_RADIUS * logf((1 + sinf(latitude * M_PI / 180.0)) / (1 - sinf(latitude * M_PI / 180.0))) / 2.0);
}

- (double)pixelSpaceXToLongitude:(double)pixelX
{
    return ((round(pixelX) - MERCATOR_OFFSET) / MERCATOR_RADIUS) * 180.0 / M_PI;
}

- (double)pixelSpaceYToLatitude:(double)pixelY
{
    return (M_PI / 2.0 - 2.0 * atan(exp((round(pixelY) - MERCATOR_OFFSET) / MERCATOR_RADIUS))) * 180.0 / M_PI;
}

- (MKCoordinateSpan)coordinateSpanWithMapView:(MKMapView *)mv
                             centerCoordinate:(CLLocationCoordinate2D)centerCoordinate
                                 andZoomLevel:(NSUInteger)zoomLevel
{
    // convert center coordiate to pixel space
    double centerPixelX = [self longitudeToPixelSpaceX:centerCoordinate.longitude];
    double centerPixelY = [self latitudeToPixelSpaceY:centerCoordinate.latitude];
    
    // determine the scale value from the zoom level
    NSInteger zoomExponent = 20 - zoomLevel;
    double zoomScale = pow(2, zoomExponent);
    
    // scale the map’s size in pixel space
    CGSize mapSizeInPixels = mv.bounds.size;
    double scaledMapWidth = mapSizeInPixels.width * zoomScale;
    double scaledMapHeight = mapSizeInPixels.height * zoomScale;
    
    // figure out the position of the top-left pixel
    double topLeftPixelX = centerPixelX - (scaledMapWidth / 2);
    double topLeftPixelY = centerPixelY - (scaledMapHeight / 2);
    
    // find delta between left and right longitudes
    CLLocationDegrees minLng = [self pixelSpaceXToLongitude:topLeftPixelX];
    CLLocationDegrees maxLng = [self pixelSpaceXToLongitude:topLeftPixelX + scaledMapWidth];
    CLLocationDegrees longitudeDelta = maxLng - minLng;
    
    // find delta between top and bottom latitudes
    CLLocationDegrees minLat = [self pixelSpaceYToLatitude:topLeftPixelY];
    CLLocationDegrees maxLat = [self pixelSpaceYToLatitude:topLeftPixelY + scaledMapHeight];
    CLLocationDegrees latitudeDelta = -1 * (maxLat - minLat);
    
    // create and return the lat/lng span
    MKCoordinateSpan span = MKCoordinateSpanMake(latitudeDelta, longitudeDelta);
    return span;
}

-(void)setCenterCoorindate:(CLLocationCoordinate2D)centerCoordinate
                 zoomLevel:(NSUInteger)zoomLevel
                  animated:(BOOL)animated
{
    // clamp large numbers to 28
    zoomLevel = MIN(zoomLevel, 28);
    
    // use the zoom level to compute the region
    MKCoordinateSpan span = [self coordinateSpanWithMapView:self.mapView centerCoordinate:centerCoordinate andZoomLevel:zoomLevel];
    MKCoordinateRegion region = MKCoordinateRegionMake(centerCoordinate, span);
    
    // set the region like normal
    [self.mapView setRegion:region animated:animated];
}

-(void)handleTap:(UITapGestureRecognizer *)gestureRecognizer
{
    if (selectedLocation) {
        return;
    }

    CGPoint point = [gestureRecognizer locationInView:self.mapView];
    
    CLLocationCoordinate2D coordinate = [self.mapView convertPoint:point toCoordinateFromView:self.mapView];
    
    [self setUserLocation:coordinate];
}


-(void)setUserLocation:(CLLocationCoordinate2D)coordinate {
    
    selectedLocation = [[CLLocation alloc] initWithLatitude:coordinate.latitude longitude:coordinate.longitude];
    MapAnnotation *annotation = [[MapAnnotation alloc] initWithTitle:@"Selected location" andCoordinate:coordinate];
    
    [self.mapView addAnnotation:annotation];
    
}

// Callback for the "My Location" button.
// Asks the map to display the user's location.
-(void)showMyLocation:(UIBarButtonItem *)button {
    
    self.mapView.showsUserLocation = YES;
    
}

- (void)mapView:(MKMapView *)mapView didUpdateUserLocation:(MKUserLocation *)userLocation
{
    [self setCenterCoorindate:userLocation.location.coordinate zoomLevel:DEFAULT_ZOOM animated:YES];
    if (!selectedLocation) {
        [self setUserLocation:userLocation.location.coordinate];
    }
}

- (void)mapView:(MKMapView *)mapView didFailToLocateUserWithError:(NSError *)error
{
    
}

// Callback for the "cancel" navigation button.
// Dismisses this controller without updating the Record location.
-(void)cancel:(id)sender {
    [self dismissModalViewControllerAnimated:YES];
}

-(void)done:(id)sender {
    [delegate locationSelected:selectedLocation];
    [self dismissModalViewControllerAnimated:YES];
}

@end
