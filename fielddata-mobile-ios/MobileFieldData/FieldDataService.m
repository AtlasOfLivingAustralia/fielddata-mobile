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

@implementation FieldDataService

#define kDownloadUrl @"survey/download?uuid="

@synthesize delegate;

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
        
        NSLog(@"%@", response);
        NSError *error;
        NSDictionary* survey =  [NSJSONSerialization JSONObjectWithData:response.dataValue 
                                                                options:kNilOptions error:&error];
        
        // extract the species
        for (NSDictionary* speciesDict in [survey objectForKey:@"indicatorSpecies"]) {
            [self persistSpecies:speciesDict];
        }
        
        [self persistSurvey:survey error:error];
        
        if (error == NULL) {
            [delegate downloadSurveyDetailsSuccessful:YES survey:survey];
        } else {
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
    
    survey.id = [surveyDetails objectForKey:@"id"];
    survey.name = [surveyDetails objectForKey:@"name"];
    survey.surveyDescription = [surveyDetails objectForKey:@"description"];
    survey.lastSync = [NSDate date];
    
    NSNumber* startDate = [surveyDetails objectForKey:@"startDate"];
    if (startDate != (id)[NSNull null]) {
        survey.startDate = [NSDate dateWithTimeIntervalSince1970:([startDate doubleValue] / 1000)];
    }
    NSNumber* endDate = [surveyDetails objectForKey:@"endDate"];
    if (endDate != (id)[NSNull null]) {
        survey.endDate = [NSDate dateWithTimeIntervalSince1970:([endDate doubleValue] / 1000)];
    }
    // get the map details
    NSDictionary* mapDefaults = [surveyDict objectForKey:@"map"];
    NSDictionary* center = [mapDefaults objectForKey:@"center"];
    NSString* x = [center objectForKey:@"x"];
    survey.mapX = [NSNumber numberWithDouble:[x doubleValue]];
    NSString* y = [center objectForKey:@"y"];
    survey.mapY = [NSNumber numberWithDouble:[y doubleValue]];
    survey.zoom = [mapDefaults objectForKey:@"zoom"];
    
    for (NSDictionary* recordProperty in [surveyDict objectForKey:@"recordProperties"]) {
        [self persistRecordProperty:recordProperty survey:survey error:e];
    }
    
    for (NSDictionary* attribute in [surveyDict objectForKey:@"attributesAndOptions"]) {
        [self persistAttribute:attribute survey:survey error:e];
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
    
    SurveyAttribute* attribute = [NSEntityDescription insertNewObjectForEntityForName:@"SurveyAttribute" inManagedObjectContext:context];

    NSString* typeCode = [surveyAttribute objectForKey:@"typeCode"];
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
        [typeCode isEqualToString:kMultiCheckbox]) {
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

-(void)persistSpecies:(NSDictionary*)speciesDict {
    
    Species *species = [NSEntityDescription insertNewObjectForEntityForName:@"Species" inManagedObjectContext:context];
    
    /*
    for (NSString* key in [speciesDict keyEnumerator]) {
        NSLog(@"Key: %@ Value: %@", key, [speciesDict objectForKey:key]);
    }
    
    NSLog(@"Scientfic Name: %@", [speciesDict objectForKey:@"scientificName"]);
    NSLog(@"Common Name: %@", [speciesDict objectForKey:@"commonName"]);
    */
    
    species.scientificName = [speciesDict objectForKey:@"scientificName"];
    species.commonName = [speciesDict objectForKey:@"commonName"];
    
    NSString* thumbnailPath;
    NSArray* infoItems = [speciesDict objectForKey:@"infoItems"];
    for (NSDictionary* infoItem in infoItems) {
        NSString* type = [infoItem objectForKey:@"type"];
        if ([type isEqualToString:@"thumb"]) {
            thumbnailPath = [infoItem objectForKey:@"content"];
        }
    }
    
    //species.imageFileName = thumbnailPath;
    
    // save the image to local storage
    NSString* imageURL = [NSString stringWithFormat:@"%@%@%@", [preferences getFieldDataURL], kDownloadUrl,thumbnailPath];
    UIImage* image = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:imageURL]]];
    
    NSString* docDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    
    NSString* pngFilePath = [NSString stringWithFormat:@"%@/%@.png", docDir, species.commonName];
	NSData* data = [NSData dataWithData:UIImagePNGRepresentation(image)];
	[data writeToFile:pngFilePath atomically:YES];
    
    species.imageFileName = pngFilePath;
    
	NSLog(@"%f,%f",image.size.width,image.size.height);
    
    NSError *error;
    if (![context save:&error]) {
        NSLog(@"Error saving Species: %@", [error localizedDescription]);
    }
}

-(NSArray*)loadSurveys {
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"Survey" inManagedObjectContext:context];
    [fetchRequest setEntity:entity];
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

-(void)createRecord:(NSArray*)attributes survey:(Survey*)survey inputFields:(NSMutableDictionary*)inputFields {
    
    Record *record = [NSEntityDescription insertNewObjectForEntityForName:@"Record" inManagedObjectContext:context];
    record.date = [NSDate date];
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
                   [attribute.typeCode isEqualToString:kSpeciesRP] ||
                   [attribute.typeCode isEqualToString:kPoint]) {
            
            NSMutableString* value = [inputFields objectForKey:attribute.weight];
            NSLog(@"%@ %@", attribute.question, value);
            
            recordAttribute.value = value;
            
        } else if ([attribute.typeCode isEqualToString:kImage]) {
            
            NSMutableString* filePath = [inputFields objectForKey:attribute.weight];
            NSLog(@"%@ %@", attribute.question, filePath);
            
            recordAttribute.value = filePath;
            
        } else {
            
            UITextField* textField = [inputFields objectForKey:attribute.weight];
            NSLog(@"%@ %@", attribute.question, textField.text);
            
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
                      
                          

@end
