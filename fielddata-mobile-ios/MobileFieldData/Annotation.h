//
//  Annotation.h
//  PolygonOverlaySample
//
//  Created by Sathish Babu Sathyamoorthy on 23/03/2014.
//
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>

@interface Annotation : NSObject <MKAnnotation> {
    NSString *title;
    NSString *subtitle;
    int index;
    CLLocationCoordinate2D coordinate;
}

@property (nonatomic, copy) NSString * title;
@property (nonatomic, copy) NSString * subtitle;
@property (nonatomic, assign) int index;
@property (nonatomic, assign)CLLocationCoordinate2D coordinate;

@end
