//
//  RecordValidator.h
//  MobileFieldData
//
//  Created by Chris Godwin on 5/02/13.
//
//

#import <Foundation/Foundation.h>
@class Record;
@class ValidationResult;
@class AttributeError;
@class SurveyAttribute;


@interface RecordValidator : NSObject
-(ValidationResult*)validate:(Record*)record;
-(AttributeError*)validate:(NSString*)value forAttribute:(SurveyAttribute*)attribute;
@end
