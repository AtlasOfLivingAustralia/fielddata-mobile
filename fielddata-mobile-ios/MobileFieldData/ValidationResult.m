//
//  ValidationResult.m
//  MobileFieldData
//
//  Created by Chris Godwin on 5/02/13.
//
//

#import "ValidationResult.h"

@implementation AttributeError
@synthesize attributeId, errorText;
@end

@implementation ValidationResult

@synthesize valid;


-(id)initWithErrors:(NSArray*)errors
{
    self = [super init];
    _errors = errors;
    
    return self;
}

-(BOOL)valid
{
    return _errors.count == 0 ? YES : NO;
}

@end
