//
//  FieldDataService.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 12/09/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Preferences.h"
#import "Survey.h"
#import "Record.h"
#import "Species.h"

// record properties
#define kSpeciesRP @"Species"
#define kNumber @"Number"
#define kPoint @"Point"
#define kLocation @"Location"
#define kAccuracy @"AccuracyInMeters"
#define kWhen @"When"
#define kTimeRP @"Time"
#define kNotes @"Notes"

// attribute types
#define kIntegerType @"IN"
#define kIntegerWithRange @"IR"
#define kDecimal @"DE"
#define kBarcode @"BC"
#define kRegEx @"RE"
#define kDate @"DA"
#define kTime @"TM"

#define kString @"ST"
#define kStringAutoComplete @"SA"
#define kText @"TA"

#define kHtml @"HL"
#define kHtmlNoValidation @"HV"
#define kHtmlComment @"CM"
#define kHtmlHorizontalRule @"HR"

#define kStringWithValidValues @"SV"

#define kSingleCheckbox @"SC"
#define kMultiCheckbox @"MC"
#define kMultiSelect @"MS"

#define kImage @"IM"
#define kAudio @"AU"
#define kFile @"FI"

#define kSpecies @"SP"
#define kCensusMethodRow @"CR"
#define kCensusMethodCol @"CC"

#define kModeratorScope @"SURVEY_MODERATION"

@protocol FieldDataServiceDelegate <NSObject>
@required
- (void)downloadSurveysSuccessful:(BOOL)success surveyArray:(NSArray*)surveys;
- (void)downloadSurveyDetailsSuccessful:(BOOL)success survey:(NSDictionary*)survey;
@end

@protocol FieldDataServiceUploadDelegate <NSObject>
@required
- (void)uploadSurveysSuccessful:(BOOL)success;
@end

@interface FieldDataService : NSObject {
    
    id <FieldDataServiceDelegate> delegate;
    id <FieldDataServiceUploadDelegate> uploadDelegate;
    
    @private
    Preferences* preferences;
    NSManagedObjectContext *context;
}

@property (retain) id delegate;
@property (retain) id uploadDelegate;

-(void)downloadSurveys;
-(void)downloadSurveyDetails:(NSString*)surveyId downloadedSurveys:(NSArray*)downloadedSurveys;
-(NSArray*)loadSurveys;
-(NSFetchedResultsController*)loadSpecies;
-(NSFetchedResultsController*)loadSpecies:(NSArray*)speciesIds searchText:(NSString*)searchText;
-(NSArray*)loadSpeciesGroups;

-(void)deleteAllEntities:(NSString*)entityName;
-(Record*)createRecord:(NSArray*)attributes survey:(Survey*)survey inputFields:(NSMutableDictionary*)inputFields;
-(void)updateRecord:(Record*)record attributes:(NSArray*)attributes inputFields:(NSMutableDictionary*)inputFields;
-(NSArray*)loadRecords;
-(BOOL)isSupported:(NSString*)typeCode;
-(BOOL)isRecordComplete:(Record*)record;
-(void)uploadRecord:(Record*)record;
-(Species*)findSpeciesByCommonName:(NSString*)commonName;

@end
