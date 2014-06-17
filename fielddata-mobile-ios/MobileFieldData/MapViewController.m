//
//  MapViewController.m
//  MobileFieldData
//
//  Created by Chris Godwin on 11/01/13.
//
//

#import "MapViewController.h"
#import "MapAnnotation.h"
#import "Annotation.h"
#import <MapKit/MapKit.h>
#import "Record.h"
#import "Survey.h"
#import "Constant.h"
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

@synthesize mapView, selectedLocation, delegate, myToolBar, myLocation, dropPin, polygonStatus, locationManager,userSelectedPoints;

-(id)initWithSurveyDefaults:(Survey *)survey polygon:(int)isPolygonEnabled polygonValue: (NSString*) polygonStr {
    
    CLLocationCoordinate2D defaultCentre;
    defaultCentre.latitude =  [survey.mapY floatValue];
    defaultCentre.longitude = [survey.mapX floatValue];
    NSInteger defaultZoom = [survey.zoom intValue];
    self.polygonStatus = isPolygonEnabled;
    selectedLocation = nil;
    return [self initWithCoordinate:defaultCentre zoomLevel:defaultZoom polygonValue : polygonStr];
}

-(id)initWithLocation:(CLLocation *)location polygon:(int)isPolygonEnabled  polygonValue: (NSString*) polygonStr {
    self.selectedLocation = location;
    self.polygonStatus = isPolygonEnabled;
    return [self initWithCoordinate:location.coordinate zoomLevel:DEFAULT_ZOOM polygonValue:polygonStr];
}

- (id)initWithCoordinate:(CLLocationCoordinate2D)centreCoordinate zoomLevel:(NSUInteger)zoomLevel polygonValue: (NSString*) polygonStr{
    self = [super initWithNibName:@"MapViewController" bundle:nil];
    if (self) {
        if(self.polygonStatus == 1){
            self.title = @"Touch or Drop pin";
            [self loadPolygonValueArray :polygonStr];
        }
        else
            self.title = @"Select a location";

        
        centre = centreCoordinate;
        zoom = zoomLevel;
        UIBarButtonItem *doneButton = [[UIBarButtonItem alloc]
                                       initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(done:)];
        self.navigationItem.rightBarButtonItem = doneButton;
        UIBarButtonItem *cancelButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel target:self action:@selector(cancel:)];
        self.navigationItem.leftBarButtonItem = cancelButton;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    if(self.polygonStatus == 1) {

        [self addGestureRecogniserToMapView];

        // Remove single point specific MyLocation UIBarButtonItem.
        NSMutableArray  *items = [myToolBar.items mutableCopy];
        [items removeObject: myLocation];
        [myToolBar setItems: items];
        
        // Track user location.
        locationManager = [[CLLocationManager alloc] init];
        locationManager.distanceFilter = kCLDistanceFilterNone; // whenever we move
        locationManager.desiredAccuracy = kCLLocationAccuracyBest;
        locationManager.delegate = self;
        [locationManager startUpdatingLocation];
        
        // Add pins to the new location.
        for(int i=0; i < [userSelectedPoints count]; i++)
            [self loadPolygonArea: [[userSelectedPoints objectAtIndex:i] MKCoordinateValue] indexValue:i];
        
    }
    else{
        // Remove single point specific MyLocation UIBarButtonItem.
        NSMutableArray  *items = [myToolBar.items mutableCopy];
        for (UIView *v  in [myToolBar items])
        {
           if ([v isKindOfClass:[UIBarButtonItem class]])
           {
               UIBarButtonItem *b = (UIBarButtonItem*)v;
               if(b != myLocation)
                  [items removeObject:b];
           }
        }
        [myToolBar setItems: items];
    }
    
    // TODO: 1. Check sinhgle point entry 2.Check whether survey location need to alter the initial point.
    if(!self.polygonStatus) {
        // Handling Single point gesture for single point operation.
        UITapGestureRecognizer *gestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
        gestureRecognizer.numberOfTapsRequired = 1;
        [self.mapView addGestureRecognizer:gestureRecognizer];
    }
    if([userSelectedPoints count] > 0)
    {
        MKCoordinateSpan span;
        span.latitudeDelta= 40;
        span.longitudeDelta=40;
        MKCoordinateRegion region;
        region.span=span;
        CLLocationCoordinate2D ausCoordinate = [[userSelectedPoints objectAtIndex: [userSelectedPoints count]-1] MKCoordinateValue];

        region.center = ausCoordinate;
        region.span.longitudeDelta  = 0.005;
        region.span.latitudeDelta  = 0.005;
        [mapView setRegion:region animated:YES];
        [mapView regionThatFits:region];
    }
    else if (selectedLocation)
        [self setUserLocation:selectedLocation.coordinate];
    else if (centre.latitude != 0.0 && centre.latitude != 0.0)
        [self setCenterCoorindate:centre zoomLevel:zoom animated:YES];

    self.mapView.mapType = MKMapTypeHybrid;
}


/* Add gesture recogniser support to handle touch operation */
- (void)addGestureRecogniserToMapView {
    UILongPressGestureRecognizer *lpgr = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(addPinToMap:)];
    lpgr.minimumPressDuration = 0.5;
    [mapView addGestureRecognizer:lpgr];
}


/* Get touch coordinate value from gesture recognizer. */
- (void)addPinToMap:(UIGestureRecognizer *)gestureRecognizer {
    
    if (gestureRecognizer.state != UIGestureRecognizerStateBegan)
        return;
    
    CGPoint touchPoint = [gestureRecognizer locationInView:mapView];
    CLLocationCoordinate2D touchMapCoordinate = [mapView convertPoint:touchPoint toCoordinateFromView:mapView];
    [self addCoordinateToMap:touchMapCoordinate];
    
}


/* Add the given coordinate to the map. */
- (void)loadPolygonArea:(CLLocationCoordinate2D)coordinate2D indexValue: (int) index{
    
    Annotation *toAdd = [[Annotation alloc]init];
    toAdd.coordinate = coordinate2D;
    toAdd.title = [NSString stringWithFormat:@"Location %d", index+1];;
    toAdd.subtitle = @"";
    toAdd.index = index;
    [mapView addAnnotation:toAdd];
    
    if(index == [self.userSelectedPoints count]-1)
    {
        CLLocationCoordinate2D coordinate[[self.userSelectedPoints count]];
        
        for(int i = 0; i < [self.userSelectedPoints count]; i++ )
            coordinate[i] = [[self.userSelectedPoints objectAtIndex:i] MKCoordinateValue];
        
        MKPolygon *polygon =[MKPolygon polygonWithCoordinates:coordinate count:[self.userSelectedPoints count]];
        [self removePolygonOverlays];
        [mapView addOverlay:polygon];
    }
    
}


/* Add the given coordinate to the map. */
- (void)addCoordinateToMap:(CLLocationCoordinate2D)coordinate2D {
    
    Annotation *toAdd = [[Annotation alloc]init];
    toAdd.coordinate = coordinate2D;
    toAdd.title = [NSString stringWithFormat:@"Location %d", [self.userSelectedPoints count]+1];;
    toAdd.subtitle = @"";
    toAdd.index = [self.userSelectedPoints count];
    [mapView addAnnotation:toAdd];
    
    //Use NSValue to store struct to array.
    [self.userSelectedPoints addObject:[NSValue valueWithMKCoordinate:coordinate2D]];
    
    if([self.userSelectedPoints count] >= 3)
    {
        CLLocationCoordinate2D coordinate[[self.userSelectedPoints count]];
        
        for(int i = 0; i < [self.userSelectedPoints count]; i++ )
            coordinate[i] = [[self.userSelectedPoints objectAtIndex:i] MKCoordinateValue];
        
        MKPolygon *polygon =[MKPolygon polygonWithCoordinates:coordinate count:[self.userSelectedPoints count]];
        [self removePolygonOverlays];
        [mapView addOverlay:polygon];
    }
    
    MKCoordinateSpan span;
    span.latitudeDelta=.001;
    span.longitudeDelta=.001;
    
    MKCoordinateRegion region;
    region.span=span;
    region.center = coordinate2D;
    
    [mapView setRegion:region animated:YES];
    [mapView regionThatFits:region];

}
-(void) dropPinAtCurrentLocation:(UIBarButtonItem *)button {
    NSLog(@"%@ Current location: ", [NSString stringWithFormat:@"latitude: %f longitude: %f", locationManager.location.coordinate.latitude, locationManager.location.coordinate.longitude]);
    [self addCoordinateToMap:locationManager.location.coordinate] ;
}


-(void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
    /*  CLLocationManagerDelegate delegate to update new location. */
}

// Returns view with polygon values.
-(MKOverlayView *)mapView:(MKMapView *)mapView viewForOverlay:(id <MKOverlay>)overlay {
	
    if([overlay isKindOfClass:[MKPolygon class]]) {
		MKPolygonView *view = [[MKPolygonView alloc] initWithOverlay:overlay];
		view.lineWidth=2;
		view.strokeColor=[UIColor greenColor];
		view.fillColor=[[UIColor greenColor] colorWithAlphaComponent:0.5];
        return view;
	}
	return nil;
}

/* Update polygon view */
- (void) removePolygonOverlays {
    [mapView removeOverlays: mapView.overlays];
}

/* Update polygon model */
- (void) resetPolygonValues {
    [self.userSelectedPoints removeAllObjects];
}

- (MKAnnotationView *)mapView:(MKMapView *)mv viewForAnnotation:(id <MKAnnotation>)annotation {

    if( annotation == mv.userLocation )
        return nil;
    
    MKPinAnnotationView *annotationView;
    annotationView = (MKPinAnnotationView*)[mv dequeueReusableAnnotationViewWithIdentifier:@"AnnotationIdentifier"];
    if( annotationView == nil ){
        annotationView = [[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"AnnotationIdentifier"];
    }
    annotationView.draggable = YES;
    annotationView.pinColor = MKPinAnnotationColorGreen;
    annotationView.canShowCallout = YES;
    annotationView.selected = YES;
    
    // Create a UIButton object to add on the
    if(polygonStatus){
        UIButton *rightButton = [UIButton buttonWithType:UIButtonTypeDetailDisclosure];
        [rightButton setTitle:annotation.title forState:UIControlStateNormal];
        [annotationView setRightCalloutAccessoryView:rightButton];
    }
    return annotationView;
}

- (void)mapView:(MKMapView *)mapView annotationView:(MKAnnotationView *)view calloutAccessoryControlTapped:(UIControl *)control {
    
    if (control == view.rightCalloutAccessoryView) {
        Annotation * temp = view.annotation;
        NSLog(@"Clicked right click button %d",temp.index);
        NSString *heading = [[NSString alloc] initWithFormat:@"Location %d",temp.index+1];
        NSString *message = [[NSString alloc] initWithFormat:@"Latitude : %.10f\n Longitude: %0.10f",temp.coordinate.latitude,temp.coordinate.longitude];
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:heading
                                                        message:message
                                                       delegate:nil
                                              cancelButtonTitle:@"Dismiss"
                                              otherButtonTitles:nil];
        [alert show];
    }
}

- (void)mapView:(MKMapView *)aMapView didAddAnnotationViews:(NSArray *)views {
    for (int i=0; i<views.count; i++)
    {
        if ([views[i] isKindOfClass:[MKPinAnnotationView class]])
        {
            [aMapView selectAnnotation:[views[i] annotation] animated:YES];
        }
    }
}


- (void)mapView:(MKMapView *)mapView annotationView:(MKAnnotationView *)annotationView
didChangeDragState:(MKAnnotationViewDragState)newState fromOldState:(MKAnnotationViewDragState)oldState{
    if (newState == MKAnnotationViewDragStateEnding)
    {
        CLLocationCoordinate2D droppedAt = annotationView.annotation.coordinate;
        // Update the location selection.
        selectedLocation = [[CLLocation alloc] initWithLatitude:droppedAt.latitude longitude:droppedAt.longitude];
    }
}

- (double)longitudeToPixelSpaceX:(double)longitude {
    return round(MERCATOR_OFFSET + MERCATOR_RADIUS * longitude * M_PI / 180.0);
}

- (double)latitudeToPixelSpaceY:(double)latitude{
    return round(MERCATOR_OFFSET - MERCATOR_RADIUS * logf((1 + sinf(latitude * M_PI / 180.0)) / (1 - sinf(latitude * M_PI / 180.0))) / 2.0);
}

- (double)pixelSpaceXToLongitude:(double)pixelX {
    return ((round(pixelX) - MERCATOR_OFFSET) / MERCATOR_RADIUS) * 180.0 / M_PI;
}

- (double)pixelSpaceYToLatitude:(double)pixelY{
    return (M_PI / 2.0 - 2.0 * atan(exp((round(pixelY) - MERCATOR_OFFSET) / MERCATOR_RADIUS))) * 180.0 / M_PI;
}

- (MKCoordinateSpan)coordinateSpanWithMapView:(MKMapView *)mv
                             centerCoordinate:(CLLocationCoordinate2D)centerCoordinate
                                 andZoomLevel:(NSUInteger)zoomLevel {
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
                  animated:(BOOL)animated {
    // clamp large numbers to 28
    zoomLevel = MIN(zoomLevel, 28);
    
    // use the zoom level to compute the region
    MKCoordinateSpan span = [self coordinateSpanWithMapView:self.mapView centerCoordinate:centerCoordinate andZoomLevel:zoomLevel];
    MKCoordinateRegion region = MKCoordinateRegionMake(centerCoordinate, span);
    
    // set the region like normal
    [self.mapView setRegion:region animated:animated];
}

-(void)handleTap:(UITapGestureRecognizer *)gestureRecognizer{
    
    if (selectedLocation)
    {
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

/* Remove polygon overlay and update the polygon array model */
-(void) removePolygonOverlays:(UIBarButtonItem *)button {

    [self resetPolygonValues];
    [self removePolygonOverlays];
    [mapView removeAnnotations:[mapView annotations]];
    
    NSLog(@"Removed all the polygon overlay and pins");
}



- (void)mapView:(MKMapView *)mapView didUpdateUserLocation:(MKUserLocation *)userLocation
{
    [self setCenterCoorindate:userLocation.location.coordinate zoomLevel:DEFAULT_ZOOM animated:YES];
    if (!selectedLocation) {
        [self setUserLocation:userLocation.location.coordinate];
    }
}

- (void)mapView:(MKMapView *)mapView didFailToLocateUserWithError:(NSError *)error {
    
}

// Callback for the "cancel" navigation button.
// Dismisses this controller without updating the Record location.
-(void)cancel:(id)sender {
    [self dismissModalViewControllerAnimated:YES];
}

-(void)done:(id)sender {

    if(self.polygonStatus)
        [delegate polygonValues:[self buildPolygonString]];
    else
        [delegate locationSelected:selectedLocation];
    
    [locationManager stopUpdatingLocation];
    [self dismissModalViewControllerAnimated:YES];
}

-(NSString*) buildPolygonString {
    
    NSMutableString *locationFormat = [[NSMutableString alloc] init];
    
    // For a single location store as a point
    if([userSelectedPoints count] <= 1) {
        // Then it must be a single point entry
        // Comma seperated - Format latitude, lonitude,accuracy
        for (int i = 0; i < [userSelectedPoints count]; i++) {
            CLLocationCoordinate2D points = [[userSelectedPoints objectAtIndex:i] MKCoordinateValue];
            [locationFormat appendFormat:@"%.20f, %.20f,0.0",points.latitude,points.longitude];
        }
    }
    else
    {
        // Polygon format
        [locationFormat appendFormat:@kPOLYGON_START];
        for (int i = 0; i < [userSelectedPoints count]; i++) {
            CLLocationCoordinate2D points = [[userSelectedPoints objectAtIndex:i] MKCoordinateValue];
            
            [locationFormat appendFormat:@"%.20f %.20f",points.longitude,points.latitude];
            [locationFormat appendFormat:@", "];
            
            // Last value must be the initial value.
            if(i == [userSelectedPoints count] - 1) {
                CLLocationCoordinate2D first = [[userSelectedPoints objectAtIndex:0] MKCoordinateValue];
                [locationFormat appendFormat:@"%.20f %.20f",first.longitude,first.latitude];
            }
        }
        [locationFormat appendFormat:@kPOLYGON_END];
    }
    return locationFormat;
}

-(void)loadPolygonValueArray : (NSString*) polygonStr {

    userSelectedPoints = [[NSMutableArray alloc]init];
    if(polygonStr != nil && ![polygonStr isEqualToString:@""]){
        
        NSString *regEx = [NSString stringWithFormat:@".*%@.*", @kPOLYGON_STR];
        NSRange range = [polygonStr rangeOfString:regEx options:NSRegularExpressionSearch];

        if(self.polygonStatus == 1 && range.location != NSNotFound){
            polygonStr = [polygonStr stringByReplacingOccurrencesOfString:@kPOLYGON_START withString:@""];
            polygonStr = [polygonStr stringByReplacingOccurrencesOfString:@kPOLYGON_END withString:@""];
            NSArray* polyArray = [polygonStr componentsSeparatedByString:@kPOLYGON_FIELD_DELIMITER];
            for(int i=0; i < [polyArray count]; i++) {
                if([polyArray count] > 1 && i == [polyArray count]-1)
                    break;
                NSString *trimmedString = [[polyArray objectAtIndex:i]  stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceCharacterSet]];
                NSArray* coorArray = [trimmedString componentsSeparatedByString:@" "];
                if(coorArray != nil && [coorArray count] == kPOLYGON_FIELDS) {
                    CLLocationCoordinate2D coordinate;
                    coordinate.longitude = [[coorArray objectAtIndex:0] doubleValue];
                    coordinate.latitude = [[coorArray objectAtIndex:1] doubleValue];
                    [self.userSelectedPoints addObject:[NSValue valueWithMKCoordinate:coordinate]];
                }
            }
        }
        else if(self.polygonStatus == 1) {
            NSArray* polyArray = [polygonStr componentsSeparatedByString:@kPOLYGON_FIELD_DELIMITER];
                if(polyArray!= nil && [polyArray count] == kPOINT_FIELDS) {
                    CLLocationCoordinate2D coordinate;
                    coordinate.latitude = [[polyArray objectAtIndex:0] doubleValue];
                    coordinate.longitude = [[polyArray objectAtIndex:1] doubleValue];
                    [self.userSelectedPoints addObject:[NSValue valueWithMKCoordinate:coordinate]];
                }
        }
        
        
    }
}
    



@end
