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


@interface RecordValidator : NSObject
-(ValidationResult*)validate:(Record*)record;
@end
