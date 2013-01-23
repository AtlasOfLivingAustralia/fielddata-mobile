//
//  MapAnnotation.m
//  MobileFieldData
//
//  Created by Chris Godwin on 11/01/13.
//
//

#import "MapAnnotation.h"

@implementation MapAnnotation


@synthesize title, coordinate;

- (id)initWithTitle:(NSString *)ttl andCoordinate:(CLLocationCoordinate2D)c2d {
	self = [super init];
	title = ttl;
	coordinate = c2d;
    
    self.subtitle = @"Press and drag to move pin";
	return self;
}

- (void)setCoordinate:(CLLocationCoordinate2D)newCoordinate NS_AVAILABLE(NA, 4_0)
{
    coordinate.latitude = newCoordinate.latitude;
    coordinate.longitude = newCoordinate.longitude;
}



@end
