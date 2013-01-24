//
//  Record.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 4/10/12.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>
#import <MapKit/MapKit.h>

@class RecordAttribute, Survey;

@interface Record : NSManagedObject

@property (nonatomic, retain) NSDate * date;
@property (nonatomic, retain) Survey *survey;
@property (nonatomic, retain) NSSet *recordAttributes;
@end

@interface Record (CoreDataGeneratedAccessors)

+(CLLocation *)stringToLocation:(NSString *)locationString;
+(NSDate *)stringToDate:(NSString *)dateString;
+(NSString *)dateToString:(NSDate *)date;

- (void)addRecordAttributesObject:(RecordAttribute *)value;
- (void)removeRecordAttributesObject:(RecordAttribute *)value;
- (void)addRecordAttributes:(NSSet *)values;
- (void)removeRecordAttributes:(NSSet *)values;

-(CLLocation *)getLocation;

@end
