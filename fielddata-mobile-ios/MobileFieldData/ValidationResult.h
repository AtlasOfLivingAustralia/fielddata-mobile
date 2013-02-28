//
//  ValidationResult.h
//  MobileFieldData
//
//  Created by Chris Godwin on 5/02/13.
//
//

#import <Foundation/Foundation.h>


@interface AttributeError : NSObject
@property (strong) NSNumber* attributeId;
@property NSString *errorText;
@end

@interface ValidationResult : NSObject

-(id)initWithErrors:(NSArray*)errors;

// Clears any errors created after validating the attribute
// with the supplied attributeId.  Used to support partial
// validations.
-(void)removeErrorForId:(NSNumber*)attributeId;
-(void)addError:(AttributeError*)error;
-(NSDictionary*)messagesAndFields;

// Whether the record is valid or not - a YES value indicates a valid record.
@property (assign, readonly) BOOL valid;

// Returns an array of AttributeError objects which provide information about the validation errors that occured.
@property (readonly, strong) NSArray* errors;

@end
