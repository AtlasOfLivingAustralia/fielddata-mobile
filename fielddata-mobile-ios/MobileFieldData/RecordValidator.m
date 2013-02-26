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

@end

@implementation RecordValidator
-(ValidationResult*)validate:(Record*)record
{
    NSMutableArray *results = [[NSMutableArray alloc] init];
    for (RecordAttribute* att in record.recordAttributes) {
       
        AttributeError* error = [self validate:att.value forAttribute:att.surveyAttribute];
        if (error) {
            [results addObject:error];
        }
    }
    
    return [[ValidationResult alloc] initWithErrors:results];
}


-(AttributeError*)validate:(NSString*)value forAttribute:(SurveyAttribute*)attribute
{
    AttributeError* error = [self validateRequired:value forAttribute:attribute];
    if (!error) {
        error = [self validateDate:value forAttribute:attribute];
    }
    return error;
}

-(AttributeError*)validateRequired:(NSString*)value forAttribute:(SurveyAttribute*)attribute
{
    AttributeError* error = nil;
    if ([attribute.required intValue] == YES &&
        (value == nil || [value isEqualToString:@""])) {
        
        error = [[AttributeError alloc] init];
        error.attributeId = attribute.weight;
        error.errorText = @"Please enter a value";
    }
    return error;
}

-(AttributeError*)validateDate:(NSString*)value forAttribute:(SurveyAttribute*)attribute
{
    AttributeError* error = nil;
    
    if ([attribute.typeCode isEqualToString:kWhen]) {
        
        if (value != nil && ![value isEqualToString:@""]) {
            NSDate *today = [NSDate date];
            NSDate *date = [Record stringToDate:value];
            if ([date laterDate:today] == date) {
        
                error = [[AttributeError alloc] init];
                error.attributeId = attribute.weight;
                error.errorText = @"The date cannot be in the future";
            }
        }
    }
    return error;

}

@end
