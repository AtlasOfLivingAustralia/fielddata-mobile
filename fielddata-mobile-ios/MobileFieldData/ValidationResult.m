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

-(id)init
{
    return [self initWithErrors:[[NSArray alloc] init]];
}

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

-(void)removeErrorForId:(NSNumber*)attributeId
{
    NSPredicate* predicate = [NSPredicate predicateWithBlock:^BOOL(id evaluatedObject, NSDictionary *bindings) {
        return ![((AttributeError*)evaluatedObject).attributeId isEqualToNumber:attributeId];
    }];
    _errors = [_errors filteredArrayUsingPredicate:predicate];
}

-(void)addError:(AttributeError*)error
{
    _errors = [_errors arrayByAddingObject:error];
}

-(NSDictionary*)messagesAndFields
{
    NSMutableDictionary *messages = [[NSMutableDictionary alloc] init];
    for (AttributeError *error in self.errors) {
        NSMutableArray *fieldNames = [messages objectForKey:error.errorText];
        if (fieldNames == nil) {
            fieldNames = [[NSMutableArray alloc] init];
            [messages setObject:fieldNames forKey:error.errorText];
        }
        [fieldNames addObject:error.attributeId];
        
    }
    return messages;
}

@end
