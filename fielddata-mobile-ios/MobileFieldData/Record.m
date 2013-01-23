//
//  Record.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 4/10/12.
//
//

#import "Record.h"
#import "RecordAttribute.h"
#import "Survey.h"
#import "FieldDataService.h"
#import "SurveyAttribute.h"


@implementation Record

@dynamic date;
@dynamic survey;
@dynamic recordAttributes;

-(CLLocation *)getLocation
{
    RecordAttribute *locationAttribute = nil;
    for (RecordAttribute* att in self.recordAttributes) {
        if ([att.surveyAttribute.typeCode isEqualToString:kPoint]) {
            locationAttribute = att;
            break;
        }
    }
    
    if (locationAttribute) {
        return [Record stringToLocation:locationAttribute.value];
    }
    return nil;

}

+(CLLocation *)stringToLocation:(NSString *)locationString
{
    NSArray* locDescArr = [locationString componentsSeparatedByString:@","];
    
    
    if (locDescArr.count == 3) {
        CLLocationCoordinate2D coordinate;
        coordinate.latitude = [[locDescArr objectAtIndex:0] doubleValue];
        coordinate.longitude = [[locDescArr objectAtIndex:1] doubleValue];
        CLLocationAccuracy accuracy = [[locDescArr objectAtIndex:2] doubleValue];
        CLLocation *location = [[CLLocation alloc] initWithCoordinate:coordinate altitude:0 horizontalAccuracy:accuracy verticalAccuracy:0 timestamp:nil];
        return location;
        
    }
    
    return nil;

}

+(NSString *)locationToString:(CLLocation *)location
{
    return nil;
}

@end
