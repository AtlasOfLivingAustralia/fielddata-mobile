//
//  FieldDataService.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 12/09/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "FieldDataService.h"
#import "RFRequest.h"
#import "RFResponse.h"
#import "RFService.h"
#import "Species.h"
#import "Survey.h"
#import "SurveyAttribute.h"
#import "SurveyAttributeOption.h"
#import "AppDelegate.h"
#import "Record.h"
#import "RecordAttribute.h"
#import "NSData+Base64.h"

@implementation FieldDataService

#define kDownloadUrl @"survey/download?uuid="

@synthesize delegate, uploadDelegate;

-(id)init
{
    preferences = [[Preferences alloc]init];
    
    AppDelegate *appDelegate = [[UIApplication sharedApplication] delegate];
    context = [appDelegate managedObjectContext];
    
    return self;
}

-(void)downloadSurveys
{
    NSString* url = [preferences getFieldDataURL];
    
    RFRequest *r = [RFRequest requestWithURL:[NSURL URLWithString:url] type:RFRequestMethodGet 
                      resourcePathComponents:@"survey", @"list", nil];
    
    [r addParam:[preferences getFieldDataSessionKey] forKey:@"ident"];
     
    //now execute this request and fetch the response in a block
    [RFService execRequest:r completion:^(RFResponse *response){
        
        NSError *error;
        NSArray* surveys = [NSJSONSerialization JSONObjectWithData:response.dataValue 
                                                           options:kNilOptions error:&error];
        if (error == NULL) {
            [delegate downloadSurveysSuccessful:YES surveyArray:surveys];
        } else {
            [delegate downloadSurveysSuccessful:NO surveyArray:nil];
        }
    }];
}

-(void)downloadSurveyDetails:(NSString*)surveyId
{
    NSString* url = [preferences getFieldDataURL];
    
    RFRequest *r = [RFRequest requestWithURL:[NSURL URLWithString:url] type:RFRequestMethodGet 
                      resourcePathComponents:@"survey", @"get", surveyId, nil];
    
    [r addParam:[preferences getFieldDataSessionKey] forKey:@"ident"];
    [RFService execRequest:r completion:^(RFResponse *response){
        
        if (response.httpCode == 200) {
            NSError *error;
            NSDictionary* survey =  [NSJSONSerialization JSONObjectWithData:response.dataValue
                                                                options:kNilOptions error:&error];
        
            NSLog(@"Downloaded: %@", survey);
        
            NSLog(@"Persisting species:");
            // extract the species
            for (NSDictionary* speciesDict in [survey objectForKey:@"indicatorSpecies"]) {
                [self persistSpecies:speciesDict];
            }
        
            NSLog(@"Persisting survey:");
            [self persistSurvey:survey error:error];
        
            if (error == NULL) {
                [delegate downloadSurveyDetailsSuccessful:YES survey:survey];
            } else {
                [delegate downloadSurveyDetailsSuccessful:NO survey:nil];
            }
        }
        else {
            [delegate downloadSurveyDetailsSuccessful:NO survey:nil];
        }
        /*
        for (NSString* key in [survey keyEnumerator]) {
            NSLog(@"Key: %@ Value: %@", key, [survey objectForKey:key]);
        }*/
    }];

}

-(void)persistSurvey:(NSDictionary*)surveyDict error:(NSError*)e {
    
    Survey *survey = [NSEntityDescription insertNewObjectForEntityForName:@"Survey" inManagedObjectContext:context];
    
    NSDictionary* surveyDetails = [surveyDict objectForKey:@"indicatorSpecies_server_ids"]; 
    if (!surveyDetails) {
        NSLog(@"No survey details for survey: %@", surveyDict);
    }
    survey.id = [surveyDetails objectForKey:@"id"];
    survey.name = [surveyDetails objectForKey:@"name"];
    survey.surveyDescription = [surveyDetails objectForKey:@"description"];
    survey.lastSync = [NSDate date];
    survey.order = [surveyDetails objectForKey:@"weight"];
    
    NSNumber* startDate = [surveyDetails objectForKey:@"startDate"];
    if (startDate != (id)[NSNull null]) {
        survey.startDate = [NSDate dateWithTimeIntervalSince1970:([startDate doubleValue] / 1000)];
    }
    NSNumber* endDate = [surveyDetails objectForKey:@"endDate"];
    if (endDate != (id)[NSNull null]) {
        survey.endDate = [NSDate dateWithTimeIntervalSince1970:([endDate doubleValue] / 1000)];
    }
    // get the map details
    id mapDefaults = [surveyDict objectForKey:@"map"];
    
    if ([mapDefaults isKindOfClass:[NSDictionary class]]) {
        id center = [mapDefaults objectForKey:@"center"];
        if ([center isKindOfClass:[NSDictionary class]]) {
            NSString* x = [center objectForKey:@"x"];
            survey.mapX = [NSNumber numberWithDouble:[x doubleValue]];
            NSString* y = [center objectForKey:@"y"];
            survey.mapY = [NSNumber numberWithDouble:[y doubleValue]];
        }
        id zoom = [mapDefaults objectForKey:@"zoom"];
        if ([zoom isKindOfClass:[NSNumber class]]) {
            survey.zoom = zoom;
        }
    }
    for (NSDictionary* recordProperty in [surveyDict objectForKey:@"recordProperties"]) {
        [self persistRecordProperty:recordProperty survey:survey error:e];
    }
    
    for (NSDictionary* attribute in [surveyDict objectForKey:@"attributesAndOptions"]) {
        
        // ignore moderator scoped fields
        NSString* scope = [attribute objectForKey:@"scope"];
        
        if (![scope isEqualToString:kModeratorScope]) {
            NSLog(@"Saving attribute: %@", attribute);
            [self persistAttribute:attribute survey:survey error:e];
        }
    }
    
    if (![context save:&e]) {
        NSLog(@"Error saving Survey: %@", [e localizedDescription]);
    }
}

-(void)persistRecordProperty:(NSDictionary*)recordProperty survey:(Survey*)survey error:(NSError*)e {
    
    SurveyAttribute* rpAttribute = [NSEntityDescription insertNewObjectForEntityForName:@"SurveyAttribute" inManagedObjectContext:context];
    
    rpAttribute.typeCode = [recordProperty objectForKey:@"name"];
    rpAttribute.name = [recordProperty objectForKey:@"name"];
    
    rpAttribute.required = [recordProperty objectForKey:@"required"];
    rpAttribute.weight = [recordProperty objectForKey:@"weight"];
    rpAttribute.question = [recordProperty objectForKey:@"description"];
    rpAttribute.survey = survey;
    
    if (![context save:&e]) {
        NSLog(@"Error saving Survey: %@", [e localizedDescription]);
    }
    
}

-(void)persistAttribute:(NSDictionary*)surveyAttribute survey:(Survey*)survey error:(NSError*)e {
    
    NSString* typeCode = [surveyAttribute objectForKey:@"typeCode"];
    if (![self isSupported:typeCode]) {
        return;
    }
    SurveyAttribute* attribute = [NSEntityDescription insertNewObjectForEntityForName:@"SurveyAttribute" inManagedObjectContext:context];
    attribute.typeCode = typeCode;
    
    NSString* visibility = [surveyAttribute objectForKey:@"visibility"];
    attribute.visible = [visibility isEqualToString:@"ALWAYS"] ? [NSNumber numberWithInt:1] : [NSNumber numberWithInt:0];
    attribute.required = [surveyAttribute objectForKey:@"required"];
    attribute.serverId = [surveyAttribute objectForKey:@"server_id"];
    attribute.weight = [surveyAttribute objectForKey:@"weight"];
    attribute.question = [surveyAttribute objectForKey:@"description"];
    attribute.name = [surveyAttribute objectForKey:@"name"];
    
    attribute.survey = survey;
    
    if ([typeCode isEqualToString:kMultiSelect] ||
        [typeCode isEqualToString:kMultiCheckbox] ||
        [typeCode isEqualToString:kStringWithValidValues]) {
        int weight = 100;
        for (NSDictionary* option in [surveyAttribute objectForKey:@"options"]) {
            [self persistAttributeOption:option surveyAttribute:attribute weight:weight error:e];
            weight = weight + 100;
        }
    }
    
    if (![context save:&e]) {
        NSLog(@"Error saving Survey: %@", [e localizedDescription]);
    }
}

-(void)persistAttributeOption:(NSDictionary*)surveyOption surveyAttribute:(SurveyAttribute*)attribute
                       weight:(int)weight error:(NSError*)e {
    
    SurveyAttributeOption* option = [NSEntityDescription insertNewObjectForEntityForName:@"SurveyAttributeOption" inManagedObjectContext:context];
    
    option.serverId = [surveyOption objectForKey:@"server_id"];
    option.value = [surveyOption objectForKey:@"value"];
    option.attribute = attribute;
    option.weight = [NSNumber numberWithInt:weight];
    
    if (![context save:&e]) {
        NSLog(@"Error saving Survey: %@", [e localizedDescription]);
    }
}

-(BOOL)isSupported:(NSString *)typeCode {
    return ![typeCode isEqualToString:@"HR"];
}

-(void)persistSpecies:(NSDictionary*)speciesDict {

    
    NSLog(@"Finding species with id=%@", [speciesDict objectForKey:@"commonName"]);
    
    // Check if we have a species with the same id already in the database.
    Species *species = [self findSpeciesByTaxonId:[speciesDict objectForKey:@"server_id"]];

    if (!species) {
        species = [NSEntityDescription insertNewObjectForEntityForName:@"Species" inManagedObjectContext:context];
    }
    /*
    for (NSString* key in [speciesDict keyEnumerator]) {
        NSLog(@"Key: %@ Value: %@", key, [speciesDict objectForKey:key]);
    }
    
    NSLog(@"Scientfic Name: %@", [speciesDict objectForKey:@"scientificName"]);
    NSLog(@"Common Name: %@", [speciesDict objectForKey:@"commonName"]);
    */
    
    species.scientificName = [speciesDict objectForKey:@"scientificName"];
    species.commonName = [speciesDict objectForKey:@"commonName"];
    species.taxonId = [speciesDict objectForKey:@"server_id"];
    
    NSString* thumbnailPath;
    NSArray* infoItems = [speciesDict objectForKey:@"infoItems"];
    for (NSDictionary* infoItem in infoItems) {
        NSString* type = [infoItem objectForKey:@"type"];
        if ([type isEqualToString:@"thumb"]) {
            thumbnailPath = [infoItem objectForKey:@"content"];
        }
    }
    
    // save the image to local storage
    NSString* imageURL = [NSString stringWithFormat:@"%@%@%@", [preferences getFieldDataURL], kDownloadUrl,thumbnailPath];
    NSLog(@"Getting image from URL: %@", imageURL);
    UIImage* image = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:imageURL]]];
    
    NSString* docDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    
    NSString* pngFilePath = [NSString stringWithFormat:@"%@/%@.png", docDir, species.commonName];
	NSData* data = [NSData dataWithData:UIImagePNGRepresentation(image)];
	[data writeToFile:pngFilePath atomically:YES];
    
    species.imageFileName = pngFilePath;
    
	
    NSError *error;
    if (![context save:&error]) {
        NSLog(@"Error saving Species: %@", [error localizedDescription]);
    }
}

-(NSArray*)loadSurveys {
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"Survey" inManagedObjectContext:context];
    [fetchRequest setEntity:entity];
    NSSortDescriptor *sortOrder = [[NSSortDescriptor alloc] initWithKey:@"name" ascending:YES];
    [fetchRequest setSortDescriptors:@[sortOrder]];
    
    NSError *error;
    NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
    
    return fetchedObjects;
}

-(NSArray*)loadSpecies {
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"Species" inManagedObjectContext:context];
    [fetchRequest setEntity:entity];
    NSError *error;
    NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
    
    return fetchedObjects;
}

-(Species*)findSpeciesByCommonName:(NSString*)commonName {
    return [self findSpeciesByProperty:@"commonName" propertyValue:commonName];
}

-(Species*)findSpeciesByTaxonId:(NSNumber*)taxonId {
    return [self findSpeciesByProperty:@"taxonId" propertyValue:taxonId];
}

-(Species*)findSpeciesByProperty:(NSString*)propertyName propertyValue:(id)propertyValue
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"Species" inManagedObjectContext:context];
    [fetchRequest setEntity:entity];
    
    NSString* precidateString = [NSString stringWithFormat:@"%@ = %%@", propertyName];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:precidateString, propertyValue];
    [fetchRequest setPredicate:predicate];
    
    NSError *error = nil;
    Species *result = [[context executeFetchRequest:fetchRequest error:&error] lastObject];
    if (error) {
        NSLog(@"Error finding species with %@=%@ : %@", propertyName, propertyValue, error);
    }
    return result;
}

-(void)deleteAllEntities:(NSString*)entityName {
    
    NSFetchRequest * allEntities = [[NSFetchRequest alloc] init];
    [allEntities setEntity:[NSEntityDescription entityForName:entityName inManagedObjectContext:context]];
    [allEntities setIncludesPropertyValues:NO]; //only fetch the managedObjectID
    
    NSError * error = nil;
    NSArray * entities = [context executeFetchRequest:allEntities error:&error];
    
    //error handling goes here
    for (NSManagedObject * entity in entities) {
        [context deleteObject:entity];
    }
    NSError *saveError = nil;
    [context save:&saveError];
}

-(Record*)createRecord:(NSArray*)attributes survey:(Survey*)survey inputFields:(NSMutableDictionary*)inputFields {
    
    Record *record = [NSEntityDescription insertNewObjectForEntityForName:@"Record" inManagedObjectContext:context];
    record.survey = survey;
    
    for (SurveyAttribute* attribute in attributes) {
        
        RecordAttribute* recordAttribute = [NSEntityDescription insertNewObjectForEntityForName:@"RecordAttribute" inManagedObjectContext:context];
        
        recordAttribute.record = record;
        recordAttribute.surveyAttribute = attribute;
        
        if ([attribute.typeCode isEqualToString:kIntegerType] ||
            [attribute.typeCode isEqualToString:kText]) {
            
            UITextField* textField = [inputFields objectForKey:attribute.weight];
            NSLog(@"%@ %@", attribute.question, textField.text);
            
            recordAttribute.value = textField.text;
            
        } else if ([attribute.typeCode isEqualToString:kMultiSelect] ||
                   [attribute.typeCode isEqualToString:kMultiCheckbox] ||
                   [attribute.typeCode isEqualToString:kStringWithValidValues] ||
                   [attribute.typeCode isEqualToString:kSpeciesRP] ||
                   [attribute.typeCode isEqualToString:kPoint] ||
                   [attribute.typeCode isEqualToString:kWhen]) {
            
            NSMutableString* value = [inputFields objectForKey:attribute.weight];
            NSLog(@"%@ %@", attribute.question, value);
            
            recordAttribute.value = value;
            
        } else if ([attribute.typeCode isEqualToString:kImage]) {
            
            NSMutableString* filePath = [inputFields objectForKey:attribute.weight];
            NSLog(@"%@ %@", attribute.question, filePath);
            
            recordAttribute.value = filePath;
            
        } 
        else {
            
            UITextField* textField = [inputFields objectForKey:attribute.weight];
            NSLog(@"%@ %@", attribute.question, textField.text);
            
            recordAttribute.value = textField.text;
        }
        
    }

    NSError *error;
    if (![context save:&error]) {
        NSLog(@"Error saving Record: %@", [error localizedDescription]);
    }
    
    return record;
}

-(void)updateRecord:(Record*)record attributes:(NSArray*)attributes inputFields:(NSMutableDictionary*)inputFields {
    
    for (NSNumber* weight in inputFields.keyEnumerator) {
        
        RecordAttribute* recordAttribute;
        
        for (RecordAttribute* recAtt in record.recordAttributes) {
            
            if ([recAtt.surveyAttribute.weight isEqualToNumber:weight]) {
                recordAttribute = recAtt;
                NSLog(@"Attribute weight: %@ type: %@", recAtt.surveyAttribute.weight, recAtt.surveyAttribute.typeCode);
                break;
            }
        }
        if ([recordAttribute.surveyAttribute.typeCode isEqualToString:kIntegerType] ||
            [recordAttribute.surveyAttribute.typeCode isEqualToString:kText]) {
            
            UITextField* textField = [inputFields objectForKey:recordAttribute.surveyAttribute.weight];
            recordAttribute.value = textField.text;
            
        } else if ([recordAttribute.surveyAttribute.typeCode isEqualToString:kMultiSelect] ||
                   [recordAttribute.surveyAttribute.typeCode isEqualToString:kMultiCheckbox] ||
                   [recordAttribute.surveyAttribute.typeCode isEqualToString:kStringWithValidValues] ||
                   [recordAttribute.surveyAttribute.typeCode isEqualToString:kSpeciesRP] ||
                   [recordAttribute.surveyAttribute.typeCode isEqualToString:kPoint] ||
                   [recordAttribute.surveyAttribute.typeCode isEqualToString:kWhen]) {
            
            NSMutableString* value = [inputFields objectForKey:recordAttribute.surveyAttribute.weight];
            if (![value isEqualToString:@""]) {
                recordAttribute.value = value;
            }
            
        } else if ([recordAttribute.surveyAttribute.typeCode isEqualToString:kImage]) {
            
            NSMutableString* filePath = [inputFields objectForKey:recordAttribute.surveyAttribute.weight];
            if (![filePath isEqualToString:@""]) {
                recordAttribute.value = filePath;
            }
        } else {
            
            UITextField* textField = [inputFields objectForKey:recordAttribute.surveyAttribute.weight];
            recordAttribute.value = textField.text;
        }
        
    }
    
    NSError *error;
    if (![context save:&error]) {
        NSLog(@"Error saving Record: %@", [error localizedDescription]);
    }
}
      

-(NSArray*)loadRecords {
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"Record" inManagedObjectContext:context];
    [fetchRequest setEntity:entity];
    NSError *error;
    NSArray *fetchedObjects = [context executeFetchRequest:fetchRequest error:&error];
    
    return fetchedObjects;
}

-(BOOL)isRecordComplete:(Record*)record {
    
    BOOL complete = YES;
    for (RecordAttribute* att in record.recordAttributes) {
        if ([att.surveyAttribute.required intValue] == 1 &&
            (att.value == NULL || [att.value isEqualToString:@""])) {
            NSLog(@"Attribute: %@  Value: %@", att.surveyAttribute.question, att.value);
            complete = NO;
            break;
        }
    }
    return complete;
}

-(void)uploadRecord:(Record*)record {
    
    RecordAttribute* species;
    RecordAttribute* location;
    RecordAttribute* notes;
    RecordAttribute* when;
    RecordAttribute* number;
    
    
    NSMutableArray* attributeValues = [[NSMutableArray alloc] init];
    
    for (RecordAttribute* att in record.recordAttributes) {
        if ([att.surveyAttribute.typeCode isEqualToString:kSpeciesRP]){
            species = att;
        } else if ([att.surveyAttribute.typeCode isEqualToString:kPoint]) {
            location = att;
        } else if ([att.surveyAttribute.typeCode isEqualToString:kNotes]) {
            notes = att;
        }
        else if ([att.surveyAttribute.typeCode isEqualToString:kWhen]) {
            when = att;
        }
        else if ([att.surveyAttribute.typeCode isEqualToString:kNumber]) {
            number = att;
            
        } else {
        
            NSNumber* attributeId = att.surveyAttribute.serverId;
            
            if (attributeId.intValue != 0) {
                
                NSMutableDictionary* attributeValue = [[NSMutableDictionary alloc] init];
                
                [attributeValue setObject:attributeId forKey:@"attribute_id"];
                [attributeValue setObject:[[NSNumber alloc]initWithInt:-1] forKey:@"id"];
                
                if ([att.surveyAttribute.typeCode isEqualToString:kImage]) {
                    NSString* imageUrl = att.value;
                    if (imageUrl != NULL && ![imageUrl isEqualToString:@""]) {
                        
                        @autoreleasepool {
                        
                            UIImage* photo = [UIImage imageWithContentsOfFile:imageUrl];
                            NSData *imageData = UIImageJPEGRepresentation(photo, 0.8);
                            NSString *imageString = [imageData base64EncodedString];
                    
                            photo = nil;
                            imageData = nil;
                            
                            [attributeValue setObject:imageString forKey:@"value"];
                            [attributeValues addObject:attributeValue];
                        }
                    }
                } else {
                    if (att.value != NULL) {
                        [attributeValue setObject:att.value forKey:@"value"];
                        [attributeValues addObject:attributeValue];
                    } else {
                        //[attributeValue setObject:@"NA" forKey:@"value"];
                    }
                }
                //[attributeValues addObject:attributeValue];
            }
        }
    }
    
    Species *spec = [self findSpeciesByCommonName:species.value];
    NSString* scientificName;
    NSNumber* taxonId = nil;
    
    if (spec) {
        scientificName = spec.scientificName;
        taxonId = spec.taxonId;
    }
    
    NSArray* locDescArr = [location.value componentsSeparatedByString:@","];
    
    NSString* lat;
    NSString* lon;
    NSString* accuracy;
    
    if (locDescArr.count == 3) {
        lat = [locDescArr objectAtIndex:0];
        lon = [locDescArr objectAtIndex:1];
        accuracy = [locDescArr objectAtIndex:2];
    }
    
    NSMutableDictionary* uploadDict = [[NSMutableDictionary alloc] init];
    [uploadDict setObject:lat forKey:@"latitude"];
    [uploadDict setObject:lon forKey:@"longitude"];
    [uploadDict setObject:accuracy forKey:@"accuracy"];
    if (scientificName) {
        [uploadDict setObject:scientificName forKey:@"scientificName"];
    }
    [uploadDict setObject:record.survey.id forKey:@"survey_id"];
    
    if (taxonId) {
        [uploadDict setObject:taxonId forKey:@"taxon_id"];
    }
    if (notes.value != NULL) {
        [uploadDict setObject:notes.value forKey:@"notes"];
    } 
    
    NSDate *date = record.date;
    if (!date) {
        date = [NSDate date];
    }
    // get the long date
    NSNumber *nowDouble = [NSNumber numberWithDouble: 1000.0 * [date timeIntervalSince1970]];
    NSString *nowString = [NSString stringWithFormat:@"%.0f", nowDouble.doubleValue];
    
    [uploadDict setObject:nowString forKey:@"when"];
    
    [uploadDict setObject: [NSNumber numberWithInteger:[number.value integerValue]] forKey:@"number"];
    
    CFUUIDRef newUniqueId = CFUUIDCreate(kCFAllocatorDefault);
    NSString * uuidString = (__bridge_transfer NSString*)CFUUIDCreateString(kCFAllocatorDefault, newUniqueId);
    CFRelease(newUniqueId);
    [uploadDict setObject:uuidString forKey:@"id"];
    
    [uploadDict setObject:[[NSNumber alloc]initWithInt:0] forKey:@"location"];
    //[uploadDict setObject:[[NSNumber alloc]initWithInt:0] forKey:@"number"];
    [uploadDict setObject:[[NSNumber alloc]initWithInt:0] forKey:@"server_id"];
    [uploadDict setObject:[[NSNumber alloc]initWithInt:3] forKey:@"_id"];
    
    [uploadDict setObject:attributeValues forKey:@"attributeValues"];
    
    NSArray* uploadArray = [NSArray arrayWithObject:uploadDict];
    
    NSError* error;
    //NSData* jsonData = [NSJSONSerialization dataWithJSONObject:uploadArray options:NSJSONWritingPrettyPrinted error:&error];
    NSData* jsonData = [NSJSONSerialization dataWithJSONObject:uploadArray options:kNilOptions error:&error];
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    // upload the survey through the REST webservice
    NSString* url = [preferences getFieldDataURL];
    
    RFRequest *r = [RFRequest requestWithURL:[NSURL URLWithString:url] type:RFRequestMethodPost
                      resourcePathComponents:@"survey", @"upload", nil];
    
    [r addParam:[preferences getFieldDataSessionKey] forKey:@"ident"];
    [r addParam:@"false" forKey:@"inFrame"];
    
    //jsonString = [jsonString stringByReplacingOccurrencesOfString:@"\\/" withString:@"/"]; //%2F
    jsonString = [jsonString stringByReplacingOccurrencesOfString:@"+" withString:@"%2B"];
    [r addParam:jsonString forKey:@"syncData" alreadyEncoded:YES];
    
    //now execute this request and fetch the response in a block
    [RFService execRequest:r completion:^(RFResponse *response){
        
        if (response.dataValue) {
        
            NSError * error = nil;
            NSDictionary* respDict =  [NSJSONSerialization JSONObjectWithData:response.dataValue
                                                                      options:kNilOptions error:&error];
            
            NSNumber* status = [respDict objectForKey:@"status"];
            if (status.intValue == 200) {
                // delete the record
                NSError * saveError = nil;
                [context deleteObject:record];
                [context save:&saveError];
                [uploadDelegate uploadSurveysSuccessful:YES];
            } else {
                [uploadDelegate uploadSurveysSuccessful:NO];
                NSLog(@"%@", respDict);
            }
        } else {
            [uploadDelegate uploadSurveysSuccessful:NO];
        }
        
    }];
}

                          

@end
