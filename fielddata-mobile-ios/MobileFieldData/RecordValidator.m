//
//  RecordValidator.m
//  MobileFieldData
//
//  Created by Chris Godwin on 5/02/13.
//
//

#import "RecordValidator.h"
#import "Record.h"
#import "RecordAttribute.h"
#import "SurveyAttribute.h"
#import "ValidationResult.h"
#import "FieldDataService.h"

@interface RecordValidator()

-(BOOL)validateRequired:(RecordAttribute*)recordAttribute withResults:(NSArray*)results;
-(BOOL)validateDate:(RecordAttribute*)recordAttribute withResults:(NSArray*)results;

@end

@implementation RecordValidator
-(ValidationResult*)validate:(Record*)record
{
    NSMutableArray *results = [[NSMutableArray alloc] init];
    for (RecordAttribute* att in record.recordAttributes) {
        
        if ([self validateRequired:att withResults:results]) {
            // Don't do the date validation if the date is required and blank.
            [self validateDate:att withResults:results];
        }
    }
    
    return [[ValidationResult alloc] initWithErrors:results];
}

-(BOOL)validateRequired:(RecordAttribute*)recordAttribute withResults:(NSMutableArray*)results
{
    BOOL valid = YES;
    if ([recordAttribute.surveyAttribute.required intValue] == YES &&
        (recordAttribute.value == NULL || [recordAttribute.value isEqualToString:@""])) {
        
        valid = NO;
        AttributeError *error = [[AttributeError alloc] init];
        error.attributeId = recordAttribute.surveyAttribute.weight;
        error.errorText = @"Please enter a value";
        [results addObject:error];
        
    }
    return valid;
}
-(BOOL)validateDate:(RecordAttribute*)recordAttribute withResults:(NSMutableArray*)results
{
    BOOL valid = YES;
    if ([recordAttribute.surveyAttribute.typeCode isEqualToString:kWhen]) {
        
        NSString *dateString = recordAttribute.value;
        if (dateString) {
            NSDate *today = [NSDate date];
            NSDate *date = [Record stringToDate:recordAttribute.value];
            if ([date laterDate:today] == date) {
        
                valid = NO;
                AttributeError *error = [[AttributeError alloc] init];
                error.attributeId = recordAttribute.surveyAttribute.weight;
                error.errorText = @"The date cannot be in the future";
                [results addObject:error];
            }
        }
    }
    return valid;

}

@end
