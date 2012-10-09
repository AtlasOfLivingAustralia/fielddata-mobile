//
//  SurveyAttribute.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 17/09/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Survey, SurveyAttributeOption;

@interface SurveyAttribute : NSManagedObject

@property (nonatomic, retain) NSString * typeCode;
@property (nonatomic, retain) NSNumber * visible;
@property (nonatomic, retain) NSNumber * required;
@property (nonatomic, retain) NSNumber * serverId;
@property (nonatomic, retain) NSNumber * weight;
@property (nonatomic, retain) NSString * question;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) Survey *survey;
@property (nonatomic, retain) NSSet *options;
@end

@interface SurveyAttribute (CoreDataGeneratedAccessors)

- (void)addOptionsObject:(SurveyAttributeOption *)value;
- (void)removeOptionsObject:(SurveyAttributeOption *)value;
- (void)addOptions:(NSSet *)values;
- (void)removeOptions:(NSSet *)values;

@end
