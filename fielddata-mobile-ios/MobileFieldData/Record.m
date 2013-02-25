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

#define DATE_FORMAT NSDateFormatterLongStyle

@interface Record()
-(RecordAttribute *)attributeOfType:(NSString*)attributeType;

@end

@implementation Record 

@dynamic survey;
@dynamic recordAttributes;


-(CLLocation *)getLocation
{
    RecordAttribute *locationAttribute = [self attributeOfType:kPoint];
    
    if (locationAttribute) {
        return [Record stringToLocation:locationAttribute.value];
    }
    return nil;

}

-(NSDate *)date
{
    RecordAttribute *attribute = [self attributeOfType:kWhen];
    if (attribute) {
        return [Record stringToDate:attribute.value];
    }
    return nil;
}

-(void)setDate:(NSDate *)date
{
    RecordAttribute *attribute = [self attributeOfType:kWhen];
    if (!attribute)
    {
        attribute = [NSEntityDescription insertNewObjectForEntityForName:@"RecordAttribute" inManagedObjectContext:[self managedObjectContext]];
        attribute.record = self;
        attribute.surveyAttribute = [self.survey getAttributeByType:kWhen];
        
        
    }
    attribute.value = [Record dateToString:date];
}


-(RecordAttribute *)attributeOfType:(NSString*)attributeType
{
    RecordAttribute *recordAttribute = nil;
    for (RecordAttribute* att in self.recordAttributes) {
        if ([att.surveyAttribute.typeCode isEqualToString:attributeType]) {
            recordAttribute = att;
            break;
        }
    }
    return recordAttribute;
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

+(NSDate *)stringToDate:(NSString *)dateString
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateStyle = DATE_FORMAT;
    return [dateFormatter dateFromString:dateString];
    
}
+(NSString *)dateToString:(NSDate *)date
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateStyle = DATE_FORMAT;
    return [dateFormatter stringFromDate:date];
}



@end
