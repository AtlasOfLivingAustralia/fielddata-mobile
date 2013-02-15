//
//  RecordValidatorTest.m
//  MobileFieldData
//
//  Created by Chris Godwin on 5/02/13.
//
//
#import <CoreData/CoreData.h>
#import "RecordValidatorTest.h"
#import "RecordValidator.h"
#import "ValidationResult.h"
#import "Record.h"
#import "SurveyAttribute.h"
#import "RecordAttribute.h"
#import "FieldDataService.h"


@interface RecordValidatorTest() {
    RecordValidator *recordValidator;
}
@end

@implementation RecordValidatorTest
@synthesize managedObjectContext;

- (void)setUp
{
    [super setUp];
    NSManagedObjectModel *mom = [NSManagedObjectModel mergedModelFromBundles:[NSArray arrayWithObject:[NSBundle mainBundle]]];
    NSPersistentStoreCoordinator *psc = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:mom];
    STAssertTrue([psc addPersistentStoreWithType:NSInMemoryStoreType configuration:nil URL:nil options:nil error:NULL] ? YES : NO, @"Should be able to add in-memory store");
    self.managedObjectContext = [[NSManagedObjectContext alloc] init];
    self.managedObjectContext.persistentStoreCoordinator = psc;
    recordValidator = [[RecordValidator alloc] init];
}

- (void)tearDown
{
    // Tear-down code here.
    
    [super tearDown];
}

- (void)testDateValidation
{
    NSNumber* weight = [NSNumber numberWithInt:1];
    Record *record = [self createTestRecordWithAttributeType:kWhen attributeWeight:weight andValue:@""];
    ValidationResult *result = [recordValidator validate:record];
    
    NSLog(@"Result=%d, %@", result.valid, result.errors);
    
    STAssertEquals(NO, result.valid, @"The date is a mandatory value");
    STAssertEquals((uint)1, [result.errors count], @"One error produced");
    
    AttributeError* error = result.errors[0];
    STAssertEquals(weight, error.attributeId, @"The date attribute was invalid");
    
    NSDate *today = [NSDate date];
    NSString* todayString = [Record dateToString:today];
    record = [self createTestRecordWithAttributeType:kWhen attributeWeight:weight andValue:todayString];
    result = [recordValidator validate:record];
    STAssertEquals(YES, result.valid, @"The date is valid");
    STAssertEquals((uint)0, [result.errors count], @"No errors produced");
    
    today = [today dateByAddingTimeInterval:60*60*24];
    todayString = [Record dateToString:today];
    record = [self createTestRecordWithAttributeType:kWhen attributeWeight:weight andValue:todayString];
    result = [recordValidator validate:record];
    
    STAssertEquals(NO, result.valid, @"The date is a mandatory value");
    STAssertEquals((uint)1, [result.errors count], @"One error produced");
    
}


// Creates a dummy Record object and associated dummy SurveyAttribute to allow specific Record values to
// be populated for testing purposes.
-(Record*)createTestRecordWithAttributeType:(NSString*)typeCode attributeWeight:(NSNumber*)weight andValue:(NSString*)value
{
    Record *record = [NSEntityDescription insertNewObjectForEntityForName:@"Record" inManagedObjectContext:self.managedObjectContext];
    
    RecordAttribute *attribute = [NSEntityDescription insertNewObjectForEntityForName:@"RecordAttribute" inManagedObjectContext:self.managedObjectContext];
    SurveyAttribute *surveyAttr = [NSEntityDescription insertNewObjectForEntityForName:@"SurveyAttribute" inManagedObjectContext:self.managedObjectContext];
    surveyAttr.typeCode = typeCode;
    surveyAttr.required = [[NSNumber alloc] initWithBool:YES];
    surveyAttr.weight = weight;
    
    attribute.surveyAttribute = surveyAttr;
    attribute.value = value;
    
    [record addRecordAttributesObject:attribute];
    return record;
}


@end
