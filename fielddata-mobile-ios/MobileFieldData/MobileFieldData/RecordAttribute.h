//
//  RecordAttribute.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 4/10/12.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Record, SurveyAttribute;

@interface RecordAttribute : NSManagedObject

@property (nonatomic, retain) NSString * value;
@property (nonatomic, retain) Record *record;
@property (nonatomic, retain) SurveyAttribute *surveyAttribute;

@end
