//
//  Survey.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 17/09/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "Survey.h"
#import "SurveyAttribute.h"


@implementation Survey

@dynamic id;
@dynamic startDate;
@dynamic endDate;
@dynamic name;
@dynamic surveyDescription;
@dynamic mapX;
@dynamic mapY;
@dynamic lastSync;
@dynamic attributes;
@dynamic zoom;
@dynamic order;
@dynamic speciesIds;

-(SurveyAttribute *)getAttributeByType:(NSString *)attributeType
{
    NSSet *subset = [self.attributes objectsPassingTest:^BOOL(id obj, BOOL *stop) {
        return [((SurveyAttribute *)obj).typeCode isEqualToString:attributeType];
    }];
    return (SurveyAttribute *)[subset anyObject];
}


@end
