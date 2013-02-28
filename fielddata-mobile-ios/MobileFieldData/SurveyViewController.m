//
//  SurveyViewController.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 20/09/12.
//
//
#import <MapKit/MapKit.h>
#import <QuartzCore/QuartzCore.h>
#import "SurveyViewController.h"
#import "TextInputCell.h"
#import "IntegerInputCell.h"
#import "SingleSelectCell.h"
#import "SingleSelectListCell.h"
#import "MultiSelectCell.h"
#import "ImageCell.h"
#import "SpeciesCell.h"
#import "LocationCell.h"
#import "AlertService.h"
#import "RecordAttribute.h"
#import "MasterViewController.h"
#import "SelectionListViewController.h"
#import "MapViewController.h"
#import "DateCell.h"
#import "SpeciesSelectionViewController.h"
#import "LabelledSpeciesCell.h"
#import "RecordValidator.h"
#import "ValidationResult.h"

@interface SurveyViewController ()
{
    @private
    LocationCell *locationCell;
    LabelledSpeciesCell *speciesCell;
    SingleSelectListCell *locationPrecisionCell;
    NSInteger attributeCellsRowOffset;
    ValidationResult *validationResult;
    BOOL editingSavedRecord;
    
}

@end

@implementation SurveyViewController

- (id)initWithStyle:(UITableViewStyle)style survey:(Survey*)s record:(Record*)r
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
        
        fieldDataService = [[FieldDataService alloc]init];
        fieldDataService.uploadDelegate = self;
        
        survey = s;
        
        attributes = [self sortAndFilterAttributes:[survey.attributes allObjects]];
        inputFields = [NSMutableDictionary dictionaryWithCapacity:attributes.count];
        invalidAttributes = [[NSMutableArray alloc] init];
        validationResult = [[ValidationResult alloc] init];
        
        attributeCellsRowOffset = 1;
        editingSavedRecord = (r != nil);
        if (!editingSavedRecord) {
            loadedValues = [[NSMutableDictionary alloc]init];
        } else {
            record = r;
            loadedValues = [NSMutableDictionary dictionaryWithCapacity:record.recordAttributes.count];
            
            for (RecordAttribute* recordAttribute in record.recordAttributes) {
                NSString* value = recordAttribute.value;
                if (value == NULL) {
                    value = @"";
                }
                [loadedValues setObject:value forKey:recordAttribute.surveyAttribute.weight];
            }
        }
    }
    return self;
}

-(NSArray*)sortAndFilterAttributes:(NSArray*)attributesToFilter
{
    NSArray* supportedTypes = [NSArray arrayWithObjects:kSpeciesRP, kNumber, kPoint, kWhen, kTimeRP, kNotes,
                               kIntegerType, kIntegerWithRange, kDecimal, kRegEx, kDate, kTime, kString, kStringAutoComplete, kText, kStringWithValidValues, kSingleCheckbox, kMultiCheckbox, kMultiSelect, kImage, kSpecies, nil];
    NSPredicate* predicate = [NSPredicate predicateWithFormat:@"typeCode IN %@", supportedTypes];
    
    NSArray* filtered = [attributesToFilter filteredArrayUsingPredicate:predicate];
    
    NSArray *sortDescriptors = [NSArray arrayWithObject:
                                [NSSortDescriptor sortDescriptorWithKey:@"weight" ascending:YES]];
    
    filtered = [filtered sortedArrayUsingDescriptors:sortDescriptors];
    return filtered;
    
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    UIBarButtonItem *saveButton = [[UIBarButtonItem alloc]
            initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(saveSurvey:)];
    
    self.navigationItem.rightBarButtonItem = saveButton;
    
    UITapGestureRecognizer *gestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideKeyboard:)];
    gestureRecognizer.cancelsTouchesInView = NO;
    [self.tableView addGestureRecognizer:gestureRecognizer];
    
}

-(void)hideKeyboard:(UITapGestureRecognizer *)gestureRecognizer
{
    [self.tableView endEditing:YES];
      
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.tableView = nil;
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    NSLog(@"Testing when this is called");
}

-(void)validate
{
    if (record == nil) {
        return;
    }
    RecordValidator *recordValidator = [[RecordValidator alloc] init];
    validationResult = [recordValidator validate:record];
    
    [self updateViewsWithValidationResults:validationResult scrollToErrors:YES];
}

-(void)validate:(NSString*)value forAttribute:(SurveyAttribute*)attribute
{
    [validationResult removeErrorForId:attribute.weight];
    RecordValidator *recordValidator = [[RecordValidator alloc] init];
    AttributeError *error = [recordValidator validate:value forAttribute:attribute];
    if (error) {
        [validationResult addError:error];
        [invalidAttributes addObject:attribute.weight];
    }
    else {
        [invalidAttributes removeObject:attribute.weight];
    }
    [self updateViewsWithValidationResults:validationResult scrollToErrors:NO];
}

// Save the survey to disk
-(void)saveSurvey:(id)sender {
    
    [self.view endEditing:YES];
    
    if (record != nil) {
        [fieldDataService updateRecord:record attributes:attributes inputFields:inputFields];
    }
    else {
        record = [fieldDataService createRecord:attributes survey:survey inputFields:inputFields];
    }
    [self validate];
    // check if all the mandatory fields have been entered
    if (validationResult.valid) {
        if (editingSavedRecord) {
            
            [AlertService DisplayMessageWithTitle:@"Observation Saved"
                                          message:@"Observation has been successfully updated and is ready to be uploaded."];
            [[self navigationController] popViewControllerAnimated:YES];
        }
        else {
            UIAlertView *alertView =[[UIAlertView alloc]
                                     initWithTitle:@"Upload Survey"
                                     message:@"Would you like to upload this survey immediately?"
                                     delegate:self
                                     cancelButtonTitle:@"No"
                                     otherButtonTitles:@"Yes", nil];
            
            [alertView show];
        }
    
    } else {
        [AlertService DisplayMessageWithTitle:@"Observation Draft Saved"
                                          message:@"All mandatory fields (marked *) must be entered before your observations can be uploaded to the server."];
    }
        
}

-(void)updateViewsWithValidationResults:(ValidationResult*)result scrollToErrors:(BOOL)scrollToErrors
{
    
    [invalidAttributes removeAllObjects];
    for (AttributeError *error in result.errors) {
        NSNumber *attributeId = error.attributeId;
        [invalidAttributes addObject:attributeId];
    }
    attributeCellsRowOffset = result.valid ? 1 : [result messagesAndFields].count;
    
    [self.tableView reloadData];
    if (!result.valid && scrollToErrors) {
        [self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:1 inSection:0] atScrollPosition:UITableViewScrollPositionTop animated:YES];
    }
}

-(void)displaySelectionList:(NSIndexPath*)indexPath
{
    SurveyAttribute* attribute = [self attributeForPath:indexPath];
    BOOL multiSelect = [attribute.typeCode isEqualToString:kMultiCheckbox] ? YES : NO;
    BOOL grouped = [attribute.question isEqualToString:@"Treatment method *"];
    NSArray *values = [[NSArray alloc] initWithArray:attribute.options.allObjects];
    
    SingleSelectListCell* cell = (SingleSelectListCell*)[self.tableView cellForRowAtIndexPath:indexPath];
    
    SelectionListViewController *detailViewController =
    [[SelectionListViewController alloc] initWithValues:UITableViewStylePlain selectionValues:values cell:cell multiSelect:multiSelect grouped:grouped];
    UINavigationController *navigationBar = [[UINavigationController alloc] initWithRootViewController:detailViewController];
    // ...
    // Pass the selected object to the new view controller.
    [self.navigationController presentModalViewController:navigationBar animated:YES];

}

-(void)displaySpeciesList
{
    SpeciesSelectionViewController *speciesViewController = [[SpeciesSelectionViewController alloc] initWithStyle:UITableViewStylePlain selectedSpecies:speciesCell.species speciesIds:survey.speciesIds];
    speciesViewController.delegate = self;
    
    UINavigationController *navigationBar = [[UINavigationController alloc] initWithRootViewController:speciesViewController];
    // ...
    // Pass the selected object to the new view controller.
    [self.navigationController presentModalViewController:navigationBar animated:YES];

}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    //return (interfaceOrientation == UIInterfaceOrientationPortrait);
    return YES;
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return attributes.count+attributeCellsRowOffset;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 0) {
        return [self surveyNameCell:tableView];
    }
    else if ([self isValidationSummaryRow:indexPath.row])
    {
        NSDictionary* errors = [validationResult messagesAndFields];
        NSArray* sortedMessages = [[errors allKeys] sortedArrayUsingSelector:@selector(compare:)];
        
        NSString *key = sortedMessages[indexPath.row-1];
        NSArray *fields = [errors objectForKey:key];
        return [self validationSummaryCell:tableView message:key fields:fields];
    }
    SurveyAttribute* attribute = [self attributeForPath:indexPath];
    NSString *CellIdentifier = [attribute.weight stringValue];
    SurveyInputCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    if (cell == nil) {
        NSString* mandatory = @"";
        if ([attribute.required intValue] == 1  && ![attribute.question hasSuffix:@"*"]) {
            mandatory = @" *";
        }
            
        if ([attribute.typeCode isEqualToString:kIntegerType]) {
            IntegerInputCell* intCell = [[IntegerInputCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                                                  reuseIdentifier:CellIdentifier];
            intCell.inputField.text = [loadedValues objectForKey:attribute.weight];
            cell = intCell;
            
        } else if ([attribute.typeCode isEqualToString:kMultiSelect]) {
            SingleSelectCell* singleCell = [[SingleSelectCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                                                     reuseIdentifier:CellIdentifier
                                                                     options:attribute.options.allObjects];
            [singleCell setSelectedValue:[NSString stringWithFormat:@"%@", [loadedValues objectForKey:attribute.weight]]];
            cell = singleCell;
            
        } else if ([attribute.typeCode isEqualToString:kStringWithValidValues]) {
            SingleSelectListCell* listCell = [[SingleSelectListCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                                                         reuseIdentifier:CellIdentifier
                                                                         options:attribute.options.allObjects];
            [listCell setSelectedValue:[loadedValues objectForKey:attribute.weight]];
            if ([attribute.name isEqualToString:@"Location_Precision"]) {
                locationPrecisionCell = listCell;
            }
            cell = listCell;
        } else if ([attribute.typeCode isEqualToString:kMultiCheckbox]) {
            SingleSelectListCell* listCell = [[SingleSelectListCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                                                         reuseIdentifier:CellIdentifier
                                                                                 options:attribute.options.allObjects];
            [listCell setSelectedValue:[loadedValues objectForKey:attribute.weight]];
            cell = listCell;
           
        } else if ([attribute.typeCode isEqualToString:kImage]) {
            ImageCell* imageCell = [[ImageCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
            imageCell.parentController = self;
            [imageCell updateImage:[loadedValues objectForKey:attribute.weight]];
            cell = imageCell;
           
        } else if ([attribute.typeCode isEqualToString:kSpeciesRP]) {
            speciesCell = [[LabelledSpeciesCell alloc]initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
            NSString *commonName = [loadedValues objectForKey:attribute.weight];
            if (commonName) {
                speciesCell.species=[fieldDataService findSpeciesByCommonName:commonName];
            }
            cell = speciesCell;
                
        } else if ([attribute.typeCode isEqualToString:kPoint]) {
            locationCell = [[LocationCell alloc]initWithStyle:UITableViewCellStyleDefault
                                                                     reuseIdentifier:CellIdentifier];
            locationCell.delegate = self;
            [locationCell setLocation:[NSString stringWithFormat:@"%@", [loadedValues objectForKey:attribute.weight]]];
            cell = locationCell;
            
        } else if ([attribute.typeCode isEqualToString:kWhen] ||
                   [attribute.typeCode isEqualToString:kDate]) {
            
            DateCell *dateCell = [[DateCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
            NSString *date = [loadedValues objectForKey:attribute.weight];
            if (date) {
                [dateCell setDate:date];
            }
            cell = dateCell;
        }
        else {
            TextInputCell* textCell = [[TextInputCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
            
            textCell.inputField.text = [loadedValues objectForKey:attribute.weight];
            cell = textCell;
            
            UIKeyboardType keyboardType = UIKeyboardTypeDefault;
            
            if ([attribute.typeCode isEqualToString:kNumber] ||
                [attribute.typeCode isEqualToString:kIntegerType]) {
                keyboardType = UIKeyboardTypeNumberPad;
            }
            else if ([attribute.typeCode isEqualToString:kDecimal]) {
                keyboardType = UIKeyboardTypeDecimalPad;
            }
            textCell.inputField.keyboardType = keyboardType;
        }
        cell.label.text = [NSString stringWithFormat:@"%@%@", attribute.question, mandatory];
        [inputFields setObject:cell.value forKey:attribute.weight];
        [cell addObserver:self forKeyPath:@"value" options:NSKeyValueObservingOptionNew context:(__bridge void *)attribute];
    }
    
    //NSLog(@"Index Path: %d  Label: %@", indexPath.row, cell.label.text);
    
    return cell;
}


- (void)tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.row == 0) {
        UIColor *color = [[UIColor alloc] initWithRed:206.0/255.0f green:243.0/255.0f blue:1.0f alpha:1.0f];
        cell.backgroundColor =  color;
    }
    else if ([self isValidationSummaryRow:indexPath.row]) {
        cell.backgroundColor = [[UIColor redColor] colorWithAlphaComponent:0.7f];
        
    }
    else if (indexPath.row >= attributeCellsRowOffset){
        SurveyAttribute* attribute = [self attributeForPath:indexPath];
        SurveyInputCell* inputCell = (SurveyInputCell*)cell;
        
        if ([invalidAttributes containsObject:attribute.weight]) {
           
            inputCell.label.opaque = YES;
            inputCell.label.backgroundColor = [[UIColor redColor] colorWithAlphaComponent:0.7f];
            inputCell.label.textColor = [UIColor whiteColor];
            
        }
        else {
            inputCell.label.opaque = NO;
            inputCell.label.backgroundColor = [[UIColor lightGrayColor] colorWithAlphaComponent:0.3f];
            inputCell.label.textColor = [UIColor blackColor];
        }
        //else if (inputCell)
        
    }
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 0) {
        return 75;
    }
    else if ([self isValidationSummaryRow:indexPath.row]) {
        NSString *text = @"Text";
        CGSize textSize = [text sizeWithFont:[UIFont systemFontOfSize:17.0f]];
        NSInteger invalidFieldCount = validationResult.errors.count;
        
        return textSize.height * (invalidFieldCount+1) + 10;
    }
    SurveyAttribute* attribute = [self attributeForPath:indexPath];
    if ([attribute.typeCode isEqualToString:kMultiSelect]) {
        return 200;
    } else if ([attribute.typeCode isEqualToString:kImage]) {
        return 90;
        
    } else if ([attribute.typeCode isEqualToString:kSpeciesRP]) {
        return 75;
    } else if ([attribute.typeCode isEqualToString:kPoint]) {
        return 120;
    } else if ([attribute.typeCode isEqualToString:kStringWithValidValues] ||
               [attribute.typeCode isEqualToString:kMultiCheckbox] ||
               [attribute.typeCode isEqualToString:kWhen] ||
               [attribute.typeCode isEqualToString:kDate]) {
        return 60;
    } else {
        return 75;
    }
}

- (UITableViewCell *)surveyNameCell:(UITableView *)tableView
{
    static NSString *cellIdentifier = @"SurveyDescription";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellIdentifier];
        cell.accessoryType = UITableViewCellAccessoryNone;
        cell.textLabel.adjustsFontSizeToFitWidth = YES;
        cell.textLabel.text = survey.name;
        
        cell.detailTextLabel.text = survey.surveyDescription;
        cell.detailTextLabel.numberOfLines = 2;
        
        cell.backgroundColor = [UIColor colorWithRed:206 green:243 blue:255 alpha:0];
        
        
    }
    return cell;
}

-(UITableViewCell*)validationSummaryCell:(UITableView*)tableView message:(NSString*)message fields:(NSArray*)fields
{
    static NSString *cellIdentifier = @"ValidationSummary";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellIdentifier];
        cell.accessoryType = UITableViewCellAccessoryNone;
        cell.textLabel.adjustsFontSizeToFitWidth = YES;
        cell.detailTextLabel.numberOfLines = 0;
        cell.detailTextLabel.textColor = [UIColor whiteColor];
        cell.detailTextLabel.backgroundColor = [UIColor clearColor];
        cell.textLabel.textColor = [UIColor whiteColor];
        cell.textLabel.backgroundColor = [UIColor clearColor];
        
        
    }
    
    cell.textLabel.text = [message stringByAppendingString:@" for the following fields:"];
    
    
    NSMutableString* messageText = [[NSMutableString alloc] init];
    for (NSNumber* fieldId in fields) {
        SurveyAttribute *attribute = [survey getAttributeByWeight:fieldId];
        
        [messageText appendString:@" \u2022 "];
        [messageText appendString:attribute.question];
        [messageText appendString:@"\n"];
    }
    cell.detailTextLabel.text = messageText;
    
    return cell;
}

-(void)observeValueForKeyPath:(NSString*)keyPath ofObject:(id)object change:(NSDictionary*)change context:(void*)context
{
    SurveyAttribute* attribute = (__bridge SurveyAttribute*)context;
    NSString* value = [change objectForKey:@"new"];
    NSLog(@"Received change notification for attribute: %@, new value:%@", attribute.weight, value);
    if ([invalidAttributes containsObject:attribute.weight]) {
        [self validate:value forAttribute:attribute];
    }
    [inputFields setObject:value forKey:attribute.weight];
}


-(BOOL)isValidationSummaryRow:(NSInteger)row
{
    return (!validationResult.valid && row <= attributeCellsRowOffset);
}

-(SurveyAttribute*)attributeForPath:(NSIndexPath*)indexPath
{
    return [attributes objectAtIndex:indexPath.row-attributeCellsRowOffset];
}

-(NSIndexPath*)pathForAttribute:(SurveyAttribute*)attribute
{
    return [NSIndexPath indexPathForRow:([attributes indexOfObject:attribute]+attributeCellsRowOffset) inSection:0];
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row >= attributeCellsRowOffset) {
        SurveyAttribute* attribute = [self attributeForPath:indexPath];
        if ([attribute.typeCode isEqualToString:kStringWithValidValues] ||
            [attribute.typeCode isEqualToString:kMultiCheckbox]) {
            [self displaySelectionList:indexPath];
        }
        else if ([attribute.typeCode isEqualToString:kSpeciesRP]) {
            [self displaySpeciesList];
        }
    }
}

- (NSIndexPath *)tableView:(UITableView *)tv willSelectRowAtIndexPath:(NSIndexPath *)path
{
    if (path.row >= attributeCellsRowOffset) {
        SurveyAttribute* attribute = [self attributeForPath:path];
        if ([attribute.typeCode isEqualToString:kStringWithValidValues] ||
            [attribute.typeCode isEqualToString:kMultiCheckbox] ||
            [attribute.typeCode isEqualToString:kWhen] ||
            [attribute.typeCode isEqualToString:kDate] ||
            [attribute.typeCode isEqualToString:kSpeciesRP]) {
            return path;
        }
    }
    // Deselect any current selection (this is to effectively cancel a current edit of a date field)
    [tv deselectRowAtIndexPath:[tv indexPathForSelectedRow] animated:YES];
    return nil;
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 0) {
        [AlertService DisplayMessageWithTitle:@"Observation Saved"
                                      message:@"Please go to the \"Saved Records\" menu to upload your observations to the server."];
        
        [[self navigationController] popViewControllerAnimated:YES];
    } else {
        dispatch_queue_t dispatchQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
        
        dispatch_async(dispatchQueue, ^(void) {
            
            dispatch_sync(dispatch_get_main_queue(), ^{
                // Show the progress indicator
                [self showProgressIndicator];
            });
            
            dispatch_sync(dispatch_get_main_queue(), ^{
                [fieldDataService uploadRecord:record];
            });
        });
    }
}

- (void)uploadSurveysSuccessful:(BOOL)success {
    
    [self hideProgressIndicator];
    
    if (success) {
        NSString* message = [NSString stringWithFormat:@"Survey has been successfully uploaded."];
        [AlertService DisplayMessageWithTitle:@"Upload Successful" message:message];
    } else {
        NSString* message = [NSString stringWithFormat:@"Survey upload failed, please try again later."];
        [AlertService DisplayMessageWithTitle:@"Upload Failed" message:message];
    }
    
    [[self navigationController] popViewControllerAnimated:YES];
}


-(void)showProgressIndicator
{
    // add the progress indicator to the view
    progressIndicator = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    // Set properties
    progressIndicator.labelText = @"Uploading Survey";
}

-(void)hideProgressIndicator
{
    [MBProgressHUD hideAllHUDsForView:self.view animated:YES];
}

-(void)showMap
{
    // If a location has already been selected by the user, initialise the Map with that location, otherwise
    // use the survey defaults to display a region.
    MapViewController *mapController = nil;
    CLLocation *location = [self getLocation];
    if (location) {
        mapController = [[MapViewController alloc] initWithLocation:location];
    }
    else {
        mapController = [[MapViewController alloc] initWithSurveyDefaults:survey];
    }
    mapController.delegate = self;
    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:mapController];
    [self presentModalViewController:navigationController animated:YES];
    
}

-(CLLocation *)getLocation
{
    SurveyAttribute *locationAttribute = [survey getAttributeByType:kPoint];
    NSString *locationString = [inputFields objectForKey:locationAttribute.weight];
    NSLog(@"Location weight=%@, Inputfields=%@", locationAttribute.weight, inputFields);
    if (locationString) {
        return [Record stringToLocation:locationString];
    }
    
    return nil;
}

// Callback from the Map View when the user selects a location on the map.
- (void)locationSelected:(CLLocation *)selectedLocation
{
    [locationCell setFoundLocation:selectedLocation];
    if (locationPrecisionCell) {
        [locationPrecisionCell setSelectedValue:@"On-screen map"];
    }
    NSLog(@"Found location!=%@", selectedLocation);
}

// Callback from the Location Cell when the GPS finds a location.
-(void)locationFound:(CLLocation*)foundLocation
{
    if (locationPrecisionCell) {
        [locationPrecisionCell setSelectedValue:@"GPS"];
    }
}

#pragma mark SpeciesSelectionDelegate implementation
-(void)speciesSelected:(Species *)species
{
    [speciesCell setSpecies:species];
}


@end
