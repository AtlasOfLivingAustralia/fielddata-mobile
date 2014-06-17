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
#import "SpeciesGroup.h"
#import "Survey.h"
#import "SurveyAttribute.h"
#import "SurveyAttributeOption.h"
#import "AppDelegate.h"
#import "Record.h"
#import "RecordAttribute.h"
#import "NSData+Base64.h"
#import "FileService.h"
#import "Constant.h"

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

-(void)downloadSurveys:(BOOL)deleteExisting
{
    if (deleteExisting) {
        [self deleteAll];
    }
    NSString* url = [preferences getFieldDataURL];
    
    RFRequest *r = [RFRequest requestWithURL:[NSURL URLWithString:url] type:RFRequestMethodGet 
                      resourcePathComponents:@"survey", @"list", nil];
    
    [r addParam:[preferences getFieldDataSessionKey] forKey:@"ident"];
     
    //now execute this request and fetch the response in a block
    [RFService execRequest:r completion:^(RFResponse *response){
        
        NSError *error;
        if (response.httpCode == 200 && !response.error) {
            NSArray* surveys = [NSJSONSerialization JSONObjectWithData:response.dataValue
                                                           options:kNilOptions error:&error];
            if (error == NULL) {
                [delegate downloadSurveysSuccessful:YES surveyArray:surveys];
            } else {
                [delegate downloadSurveysSuccessful:NO surveyArray:nil];
            }
        } else {
            [delegate downloadSurveysSuccessful:NO surveyArray:nil];
        }
    }];
    [self downloadSpeciesGroups];
}

-(void)begin {
    NSUndoManager* undoManager = [[NSUndoManager alloc] init];
    context.undoManager = undoManager;
}

-(void)commit
{
    NSError* error;
    if (![context save:&error]) {
        NSLog(@"Error saving Survey: %@", [error localizedDescription]);
    }
}
-(void)rollback
{
    [context rollback];
    context.undoManager = nil;
}

-(void)deleteAll
{
    [self deleteAllEntities:@"Record"];
    [self deleteAllEntities:@"Species"];
    [self deleteAllEntities:@"SpeciesGroup"];
    [self deleteAllEntities:@"Survey"];
}

-(void)downloadSurveyDetails:(NSString*)surveyId downloadedSurveys:(NSArray*)downloadedSurveys
{
    NSString* url = [preferences getFieldDataURL];
    
    RFRequest *r = [RFRequest requestWithURL:[NSURL URLWithString:url] type:RFRequestMethodGet 
                      resourcePathComponents:@"survey", surveyId, nil];
    
    [r addParam:[preferences getFieldDataSessionKey] forKey:@"ident"];
    for (NSString *downloadedSurvey in downloadedSurveys) {
        [r addParam:downloadedSurvey forKey:@"surveysOnDevice"];
    }
     
    [RFService execRequest:r completion:^(RFResponse *response){
        
        if (response.httpCode == 200 && !response.error) {
            
            NSError *error;
            NSDictionary* survey =  [NSJSONSerialization JSONObjectWithData:response.dataValue
                                                                options:kNilOptions error:&error];
        
            [self persistSurvey:survey error:error];
        
            if (error != NULL) {
                [delegate downloadSurveyDetailsSuccessful:NO survey:nil];
            }
            // Download the species associated with the Survey.
            [self downloadSpeciesForSurvey:surveyId downloadedSurveys:downloadedSurveys];
        }
        else {
            if (response.error) {
                NSLog(@"Error while downloading survey %@.  Error=%@", r, response.error);
            }
            [delegate downloadSurveyDetailsSuccessful:NO survey:nil];
        }
        
    }];

}


-(void)downloadSpeciesForSurvey:(NSString*)surveyId downloadedSurveys:(NSArray*)downloadedSurveys {
    
    [self downloadSpeciesForSurvey:surveyId downloadedSurveys:downloadedSurveys startFrom:0 batchSize:50];
}

-(void)downloadSpeciesForSurvey:(NSString*)surveyId downloadedSurveys:(NSArray*)downloadedSurveys startFrom:(NSInteger)first batchSize:(NSInteger)batchSize {
    
    NSString* url = [preferences getFieldDataURL];
    NSArray* speciesGroups = [self loadSpeciesGroups];
    
    RFRequest *r = [RFRequest requestWithURL:[NSURL URLWithString:url] type:RFRequestMethodGet
                      resourcePathComponents:@"species", @"speciesForSurvey", nil];
    
    [r addParam:[preferences getFieldDataSessionKey] forKey:@"ident"];
    [r addParam:surveyId forKey:@"surveyId"];
    [r addParam:[[NSNumber numberWithInteger:first] stringValue] forKey:@"first"];
    [r addParam:[[NSNumber numberWithInteger:batchSize] stringValue] forKey:@"maxResults"];
    
    for (NSString *downloadedSurvey in downloadedSurveys) {
        [r addParam:downloadedSurvey forKey:@"surveysOnDevice"];
    }
    [RFService execRequest:r completion:^(RFResponse *response){
        if (response.httpCode == 200) {
            NSError *error;
            NSDictionary* speciesResponse =  [NSJSONSerialization JSONObjectWithData:response.dataValue
                                                                             options:kNilOptions error:&error];
            if (!error) {
                int count = 0;
                for (NSDictionary* speciesDict in [speciesResponse objectForKey:@"list"]) {
                    [self persistSpecies:speciesDict speciesGroups:speciesGroups];
                    count++;
                }
                NSLog(@"downloaded %d species from request %@", count, [r URL]);
         
                if (count == batchSize) {
                    // There are more species, download the next batch.
                    [self downloadSpeciesForSurvey:surveyId downloadedSurveys:downloadedSurveys startFrom:(first+batchSize) batchSize:batchSize];
                }
                else {
                    if (!error) {
                        // The survey itself is not used by the callback so we can get away with nil.
                        [delegate downloadSurveyDetailsSuccessful:YES survey:nil];
                    }
                }
            }
            else {
                [delegate downloadSurveyDetailsSuccessful:NO survey:nil];
            }
        }
        else {
            [delegate downloadSurveyDetailsSuccessful:NO survey:nil];
        }
    }];
    
}

-(void)downloadSpeciesGroups
{
    NSString* url = [preferences getFieldDataURL];
    
    RFRequest *r = [RFRequest requestWithURL:[NSURL URLWithString:url] type:RFRequestMethodGet
                      resourcePathComponents:@"species", @"speciesGroups", nil];
    
    [RFService execRequest:r completion:^(RFResponse *response){
        if (response.httpCode == 200) {
            NSError *error;
            NSArray* speciesGroupsResponse = [NSJSONSerialization JSONObjectWithData:response.dataValue
                                                                             options:kNilOptions error:&error];
            if (!error) {
                for (NSDictionary* speciesGroup in speciesGroupsResponse) {
                    NSLog(@"Species groups response: %@", speciesGroup);
                    [self persistSpeciesGroup:speciesGroup];
                }
                
            }
        }
    }];
    
}

-(Survey*)persistSurvey:(NSDictionary*)surveyDict error:(NSError*)e {
    
    Survey *survey = [NSEntityDescription insertNewObjectForEntityForName:@"Survey" inManagedObjectContext:context];
    
    NSDictionary* surveyDetails = [surveyDict objectForKey:@"survey"]; 
    if (!surveyDetails) {
        NSLog(@"No survey details for survey: %@", surveyDict);
    }
    survey.id = [surveyDetails objectForKey:@"id"];
    survey.name = [surveyDetails objectForKey:@"name"];
    survey.surveyDescription = [surveyDetails objectForKey:@"description"];
    survey.lastSync = [NSDate date];
    survey.order = [surveyDetails objectForKey:@"weight"];
    survey.speciesIds = [surveyDetails objectForKey:@"species"];
    survey.imageUrl = [surveyDict objectForKey:@"imageUrl"];
    survey.locationPolygon = [surveyDict objectForKey:@"locationPolygon"];
    survey.polygonCensusMethod = [surveyDict objectForKey:@"polygonCensusMethod"];
    
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
        NSString* name = [attribute objectForKey:@"name"];
        NSString* typeCode = [attribute objectForKey:@"typeCode"];
        NSString *ids = nil;
        
        // Now loop the nested attribute and get set the custom field values.
        if(name != nil && typeCode != nil && [name isEqualToString:@"photopoints"] && [typeCode isEqualToString:@"CC"]) {
            NSString *ppLat = @"";
            NSString *ppLong = @"";
            NSString *bearing = @"";
            NSString *photo = @"";
            
            for (NSDictionary* nestedAttribute in [attribute objectForKey:@"nestedAttributes"]) {
                if([[nestedAttribute objectForKey:@"name"] isEqualToString:@"ppLat"]){
                    ppLat = [nestedAttribute objectForKey:@"id"];
                }
                else if([[nestedAttribute objectForKey:@"name"] isEqualToString:@"ppLong"]){
                    ppLong = [nestedAttribute objectForKey:@"id"];
                }
                else if([[nestedAttribute objectForKey:@"name"] isEqualToString:@"bearing"]){
                    bearing = [nestedAttribute objectForKey:@"id"];
                }
                else if([[nestedAttribute objectForKey:@"type"] isEqualToString:@"IMAGE"]){
                    photo = [nestedAttribute objectForKey:@"id"];
                }
            }
            NSLog(@"Survey with photopoints attribute.");
            ids = [NSString stringWithFormat : @"%@,%@,%@,%@",ppLat,ppLong,bearing,photo];
        }

        if (![scope isEqualToString:kModeratorScope] &&
            ![name isEqualToString:@"possible_species"]) { // This field only makes sense on the web form
            [self persistAttribute:attribute survey:survey customIds:ids error:e];
        }
    }
    
    // Retrieve census method information, not needed at this stage.
    for (NSDictionary* attribute in [surveyDict objectForKey:@"censusMethods"]) {
        NSLog(@"Saving census method informaytion: %@", attribute);
        [self persistAttribute:attribute survey:survey customIds:nil error:e];
    }

    return survey;
}

-(void)persistRecordProperty:(NSDictionary*)recordProperty survey:(Survey*)survey error:(NSError*)e {
    
    SurveyAttribute* rpAttribute = [NSEntityDescription insertNewObjectForEntityForName:@"SurveyAttribute" inManagedObjectContext:context];
    
    rpAttribute.typeCode = [recordProperty objectForKey:@"name"];
    rpAttribute.name = [recordProperty objectForKey:@"name"];
    
    rpAttribute.required = [recordProperty objectForKey:@"required"];
    rpAttribute.weight = [recordProperty objectForKey:@"weight"];
    rpAttribute.question = [recordProperty objectForKey:@"description"];
    rpAttribute.survey = survey;
    
}

-(void)persistAttribute:(NSDictionary*)surveyAttribute survey:(Survey*)survey customIds:(NSString *) ids error:(NSError*)e {
    
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
   
    // Store photo points custom field (ppLat,ppLong, bearing and photo id)
    if (ids != nil || [ids length] > 0)
    {
        attribute.custom = ids;
    }
    
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
}

-(void)persistAttributeOption:(NSDictionary*)surveyOption surveyAttribute:(SurveyAttribute*)attribute
                       weight:(int)weight error:(NSError*)e {
    
    SurveyAttributeOption* option = [NSEntityDescription insertNewObjectForEntityForName:@"SurveyAttributeOption" inManagedObjectContext:context];
    
    option.serverId = [surveyOption objectForKey:@"server_id"];
    option.value = [surveyOption objectForKey:@"value"];
    option.attribute = attribute;
    option.weight = [NSNumber numberWithInt:weight];
    
}

-(BOOL)isSupported:(NSString *)typeCode {
    return ![typeCode isEqualToString:@"HR"];
}

-(void)persistSpecies:(NSDictionary*)speciesDict speciesGroups:(NSArray*)speciesGroups {

    //static int speciesCount;
    //NSLog(@"Saved %d speies",speciesCount++);
    //NSLog(@"Finding species with id=%@", [speciesDict objectForKey:@"commonName"]);
    
    // Check if we have a species with the same id already in the database.
    Species *species = [self findSpeciesByTaxonId:[speciesDict objectForKey:@"server_id"]];

    if (!species) {
        species = [NSEntityDescription insertNewObjectForEntityForName:@"Species" inManagedObjectContext:context];
    }
    
    species.scientificName = [speciesDict objectForKey:@"scientificName"];
    species.commonName = [speciesDict objectForKey:@"commonName"];
    species.taxonId = [speciesDict objectForKey:@"server_id"];
    
    NSNumber* speciesGroupId = [speciesDict objectForKey:@"taxonGroupId"];
    NSUInteger index = [speciesGroups indexOfObjectPassingTest:^BOOL(id obj, NSUInteger idx, BOOL *stop) {
        SpeciesGroup* group = (SpeciesGroup*)obj;
        return [group.groupId isEqual:speciesGroupId];
    }];
    if (index != NSNotFound) {
        SpeciesGroup* group = [speciesGroups objectAtIndex:index];
        species.groupName = group.name;
    }
    else {
        species.groupName = @"All Species";
    }
    
    NSString* thumbnailPath = [speciesDict objectForKey:@"profileImageUUID"];
    
    // save the image to local storage
    NSString* imageURL = [NSString stringWithFormat:@"%@%@%@", [preferences getFieldDataURL], kDownloadUrl,thumbnailPath];
    //NSLog(@"Getting image from URL: %@", imageURL);
    UIImage* image = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:imageURL]]];
    
    NSString* docDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    
    NSString* pngFilePath = [NSString stringWithFormat:@"%@/%@.png", docDir, species.commonName];
	NSData* data = [NSData dataWithData:UIImagePNGRepresentation(image)];
	[data writeToFile:pngFilePath atomically:YES];
    
    species.imageFileName = pngFilePath;
	
}

-(SpeciesGroup*)persistSpeciesGroup:(NSDictionary*)speciesGroupDict
{
    
    SpeciesGroup* speciesGroup = [NSEntityDescription insertNewObjectForEntityForName:@"SpeciesGroup" inManagedObjectContext:context];
    
    speciesGroup.groupId = [speciesGroupDict objectForKey:@"id"];
    speciesGroup.name = [speciesGroupDict objectForKey:@"name"];
    NSArray* subgroups = [speciesGroupDict objectForKey:@"subgroups"];
    for (NSDictionary* subgroupDict in subgroups) {
        SpeciesGroup* subgroup = [self persistSpeciesGroup:subgroupDict];
        [speciesGroup addSubgroupsObject:subgroup];
    }
    return speciesGroup;
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

-(NSFetchedResultsController*)loadSpecies {
    
    return [self loadSpecies:nil searchText:nil];
}

-(NSFetchedResultsController*)loadSpecies:(NSArray*)speciesIds searchText:(NSString*)searchText {
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"Species" inManagedObjectContext:context];
    [fetchRequest setEntity:entity];
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"groupName" ascending:YES];
    NSArray *sortDescriptors = [[NSArray alloc] initWithObjects:sortDescriptor, nil];
    [fetchRequest setSortDescriptors:sortDescriptors];

    NSMutableArray *predicates = [[NSMutableArray alloc] init];
    if (speciesIds != nil) {
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"taxonId in %@", speciesIds];
        [predicates addObject:predicate];
        
    }
    if (searchText != nil && ![searchText isEqualToString:@""]) {
        NSPredicate *searchPredicate = [NSPredicate predicateWithFormat:@"(commonName CONTAINS[c] %@) OR (scientificName CONTAINS[c] %@) OR (groupName CONTAINS[c] %@)", searchText, searchText, searchText];
        [predicates addObject:searchPredicate];
        
    }
    
    if (predicates.count > 0) {
        [fetchRequest setPredicate:[NSCompoundPredicate andPredicateWithSubpredicates:predicates]];
    }
    
    NSFetchedResultsController *speciesFetchController = [[NSFetchedResultsController alloc]
            initWithFetchRequest:fetchRequest
            managedObjectContext:context
            sectionNameKeyPath:@"groupName"
            cacheName:nil];
    NSError *error;;
    [speciesFetchController performFetch:&error];
    return speciesFetchController;
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

-(NSArray*)loadSpeciesGroups
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"SpeciesGroup" inManagedObjectContext:context];
    [fetchRequest setEntity:entity];
    
    NSError *error = nil;
    NSArray* groups = [context executeFetchRequest:fetchRequest error:&error];
    if (error) {
        NSLog(@"Error loading species groups: %@", error);
    }
    return groups;
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
}

-(Record*)createRecord:(NSArray*)attributes survey:(Survey*)survey inputFields:(NSMutableDictionary*)inputFields {
    
    Record *record = [NSEntityDescription insertNewObjectForEntityForName:@"Record" inManagedObjectContext:context];
    record.survey = survey;
    
    for (SurveyAttribute* attribute in attributes) {
        
        RecordAttribute* recordAttribute = [NSEntityDescription insertNewObjectForEntityForName:@"RecordAttribute" inManagedObjectContext:context];
        
        recordAttribute.record = record;
        recordAttribute.surveyAttribute = attribute;
        recordAttribute.value = [inputFields objectForKey:attribute.weight];
        
    }

    NSError *error;
    if (![context save:&error]) {
        NSLog(@"Error saving Record: %@", [error localizedDescription]);
    }
    [self movePhotoPointsPhotos];
    return record;
}

- (void) movePhotoPointsPhotos{
    [FileService copyFiles:[FileService getTempFolderPath] :[FileService getSavedFolderPath]];
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
        
        
       NSString* value = [inputFields objectForKey:recordAttribute.surveyAttribute.weight];
       recordAttribute.value = value;
        
    }
    
    NSError *error;
    if (![context save:&error]) {
        NSLog(@"Error saving Record: %@", [error localizedDescription]);
    }
    [self movePhotoPointsPhotos];
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
    RecordAttribute* photopoints;

    
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
        }
        else if ([att.surveyAttribute.typeCode isEqualToString:kCensusMethodCol] &&
                 [att.surveyAttribute.name isEqualToString:@"photopoints"]) {
            photopoints = att;
        }

        else {
            //Handle all attributeValues here.
            NSNumber* attributeId = att.surveyAttribute.serverId;
            if (attributeId.intValue != 0) {
                
                NSMutableDictionary* attributeValue = [[NSMutableDictionary alloc] init];
                
                [attributeValue setObject:attributeId forKey:@"attribute_id"];
                [attributeValue setObject:[[NSNumber alloc]initWithInt:-1] forKey:@"id"];
                
                if ([att.surveyAttribute.typeCode isEqualToString:kImage]) {
                    NSString* imageUrl = att.value;
                    if (imageUrl != nil && ![imageUrl isEqualToString:@""]) {
                        
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
                } else if ([att.surveyAttribute.typeCode isEqualToString:kDate]) {
                    if (att.value != nil) {
                        [attributeValue setObject:[self formatDateStringForUpload:att.value] forKey:@"value"];
                        [attributeValues addObject:attributeValue];
                    }
                }
                else {
                    if (att.value != nil) {
                        [attributeValue setObject:[self urlEncode:att.value] forKey:@"value"];
                        [attributeValues addObject:attributeValue];
                    }
                }
             }
        }
    }

    if(photopoints){
        NSNumber* attributeId = photopoints.surveyAttribute.serverId;
        NSArray *ids = [photopoints.surveyAttribute.custom componentsSeparatedByString:@kPHOTOPOINT_FIELD_DELIMITER];
        NSString *ppLatId;
        NSString *ppLongId;
        NSString *ppBearingId;
        NSString *ppImageId;
        
        if(ids != nil && [ids count] == kPHOTOPOINT_FIELDS) {
            ppLatId = [ids objectAtIndex:0];
            ppLongId = [ids objectAtIndex:1];
            ppBearingId = [ids objectAtIndex:2];
            ppImageId =[ids objectAtIndex:3];
 
            if( [ppLatId length] > 0 && [ppLongId length] > 0 && [ppImageId length] > 0) {
                NSMutableDictionary *attValuesLevel1 = [[NSMutableDictionary alloc] init];
                NSMutableArray *attValuesLevel2 = [[NSMutableArray alloc] init];
                NSArray *pics = [photopoints.value componentsSeparatedByString:@kPHOTOPOINT_DELIMITER];
            
                for(int i =0; i < [pics count] - 1 ; i++) {
                    NSString *pic = [pics objectAtIndex:i];
                    NSString *ppLatValue;
                    NSString *ppLongValue;
                    NSString *ppBearingValue;
                    NSString *ppImageValue;
                    NSArray *tokens = [pic componentsSeparatedByString:@kPHOTOPOINT_FIELD_DELIMITER];
                    NSMutableArray *attValuesLevel4 = [[NSMutableArray alloc] init];
                    NSMutableDictionary *attValuesLevel4_1 = [[NSMutableDictionary alloc] init];
                    NSMutableDictionary *attValuesLevel5 = [[NSMutableDictionary alloc] init];
                    NSMutableDictionary *attValuesLevel6 = [[NSMutableDictionary alloc] init];
                    NSMutableDictionary *attValuesLevel7 = [[NSMutableDictionary alloc] init];
                    
                    if([tokens count] == kPHOTOPOINT_FIELDS) {
                        ppLatValue = [tokens objectAtIndex:0];
                        ppLongValue = [tokens objectAtIndex:1];
                        ppBearingValue = [tokens objectAtIndex:2];
                        ppImageValue = [tokens objectAtIndex:3];
                        
                        [attValuesLevel5 setObject:[[NSNumber alloc]initWithInt:0] forKey:@"key"];
                        [attValuesLevel5 setObject: [[NSNumber alloc] initWithInt: [ppLatId intValue]] forKey:@"attribute_id"];
                        [attValuesLevel5 setObject:[[NSNumber alloc]initWithInt:-1] forKey:@"id"];
                        [attValuesLevel5 setObject:ppLatValue forKey:@"value"];
                        
                        [attValuesLevel6 setObject:[[NSNumber alloc]initWithInt:1] forKey:@"key"];
                        [attValuesLevel6 setObject:[[NSNumber alloc] initWithInt: [ppLongId intValue]] forKey:@"attribute_id"];
                        [attValuesLevel6 setObject:[[NSNumber alloc]initWithInt:-1] forKey:@"id"];
                        [attValuesLevel6 setObject:ppLongValue forKey:@"value"];
                        
                        [attValuesLevel7 setObject:[[NSNumber alloc]initWithInt:2] forKey:@"key"];
                        [attValuesLevel7 setObject:[[NSNumber alloc] initWithInt: [ppImageId intValue]] forKey:@"attribute_id"];
                        [attValuesLevel7 setObject:[[NSNumber alloc]initWithInt:-1] forKey:@"id"];

                        @autoreleasepool {
                            NSString *imageUrl = [FileService getSavedFilePath: ppImageValue];
                            UIImage* photo = [UIImage imageWithContentsOfFile:imageUrl];
                            NSData *imageData = UIImageJPEGRepresentation(photo, 0.8);
                            NSString *imageString = [imageData base64EncodedString];
                            photo = nil;
                            imageData = nil;
                            [attValuesLevel7 setObject:imageString forKey:@"value"];
                        }
                    }
                    [attValuesLevel4  addObject:attValuesLevel5];
                    [attValuesLevel4  addObject:attValuesLevel6];
                    [attValuesLevel4  addObject:attValuesLevel7];
                    [attValuesLevel4_1 setValue:attValuesLevel4 forKeyPath:@"values"];
                    
                    [attValuesLevel4_1 setValue: [[NSNumber alloc] initWithInt:i] forKeyPath:@"row"];
                    [attValuesLevel4_1 setValue: [[NSNumber alloc] initWithInt: [attributeId intValue]] forKeyPath:@"attributeId"];
                    [attValuesLevel2 addObject:attValuesLevel4_1];
                }
                
            [attValuesLevel1 setValue: attributeId forKeyPath:@"attribute_id"];
            [attValuesLevel1 setValue: [[NSNumber alloc] initWithInt:-1] forKeyPath:@"id"];
            [attValuesLevel1 setValue: attValuesLevel2 forKeyPath:@"values"];
            [attributeValues addObject:attValuesLevel1];
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
    NSMutableDictionary* uploadDict = [[NSMutableDictionary alloc] init];
    
    //Business rule: polygon values overwrites points value.
    //If locationPolygon is true and polygon attribute is available then update locationWkt with polygon value
    NSString *regEx = [NSString stringWithFormat:@".*%@.*", @kPOLYGON_STR];
    NSRange range = [location.value rangeOfString:regEx options:NSRegularExpressionSearch];
    // Upper layer handles the formatting
    if([record.survey.locationPolygon intValue] == 1 && range.location != NSNotFound) {
        // Exception case:
        // If you have only 2 polygon points then change it from multipolygon to multiline string.
        NSString *polygonStr = location.value;
        polygonStr = [polygonStr stringByReplacingOccurrencesOfString:@kPOLYGON_START withString:@""];
        polygonStr = [polygonStr stringByReplacingOccurrencesOfString:@kPOLYGON_END withString:@""];
        NSArray* polyArray = [polygonStr componentsSeparatedByString:@kPOLYGON_FIELD_DELIMITER];
        if([polyArray count] == 3){
            NSString *multiLine = [[NSString alloc] initWithFormat:@"MULTILINESTRING ((%@,%@))",[polyArray objectAtIndex:0],[polyArray objectAtIndex:1]];
            [uploadDict setObject:multiLine forKey:@"locationWkt"];
            [uploadDict setObject:record.survey.polygonCensusMethod forKey:@"censusMethod_id"];
            
        }else{
            [uploadDict setObject:location.value forKey:@"locationWkt"];
            [uploadDict setObject:record.survey.polygonCensusMethod forKey:@"censusMethod_id"];
        }
    }
    else{
        NSArray* locDescArr = [location.value componentsSeparatedByString:@kPOLYGON_FIELD_DELIMITER];
        NSString* lat;
        NSString* lon;
        NSString* accuracy;
        
        if (locDescArr.count == 3) {
            lat = [locDescArr objectAtIndex:0];
            lon = [locDescArr objectAtIndex:1];
            accuracy = [locDescArr objectAtIndex:2];
            
            [uploadDict setObject:lat forKey:@"latitude"];
            [uploadDict setObject:lon forKey:@"longitude"];
            [uploadDict setObject:accuracy forKey:@"accuracy"];
        }
    }
    
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
        
    [uploadDict setObject:[self formatDateForUpload:date] forKey:@"when"];
    
    [uploadDict setObject: [NSNumber numberWithInteger:[number.value integerValue]] forKey:@"number"];
    
    CFUUIDRef newUniqueId = CFUUIDCreate(kCFAllocatorDefault);
    NSString * uuidString = (__bridge_transfer NSString*)CFUUIDCreateString(kCFAllocatorDefault, newUniqueId);
    CFRelease(newUniqueId);
    [uploadDict setObject:uuidString forKey:@"id"];
    
    [uploadDict setObject:[[NSNumber alloc]initWithInt:0] forKey:@"location"];
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
                // Remove any image that are associated to the record.
                if(photopoints) {
                    NSArray *pics = [photopoints.value componentsSeparatedByString:@"|"];
                    for(NSString * pic in pics){
                     NSArray *fields = [pic componentsSeparatedByString:@","];
                        if([fields count] == 4)
                            [FileService deleteSavedFile: [fields objectAtIndex:3]];
                    }
                }
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

-(NSString*)formatDateStringForUpload:(NSString*)dateString
{
    return [self formatDateForUpload:[Record stringToDate:dateString]];
}
                          
-(NSString*)formatDateForUpload:(NSDate*)date
{
    // get the long date
    NSNumber *millisSince1970 = [NSNumber numberWithDouble: 1000.0 * [date timeIntervalSince1970]];
    return [NSString stringWithFormat:@"%lld", [millisSince1970 longLongValue]];

}
-(NSString*)urlEncode:(NSString*)string
{
    return (NSString *)CFBridgingRelease(CFURLCreateStringByAddingPercentEscapes(
              NULL,
              (__bridge CFStringRef) string,
              NULL,
              CFSTR("!*'();:@&=+$,/?%#[]"),
              kCFStringEncodingUTF8));
}
@end
