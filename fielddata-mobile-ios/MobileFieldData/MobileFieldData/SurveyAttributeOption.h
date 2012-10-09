//
//  SurveyAttributeOption.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 17/09/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface SurveyAttributeOption : NSManagedObject

@property (nonatomic, retain) NSNumber * serverId;
@property (nonatomic, retain) NSString * value;
@property (nonatomic, retain) NSNumber * weight;
@property (nonatomic, retain) NSManagedObject *attribute;

@end
