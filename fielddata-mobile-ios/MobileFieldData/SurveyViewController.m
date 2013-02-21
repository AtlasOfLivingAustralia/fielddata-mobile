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
        
        if (r == NULL) {
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
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
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

// Save the survey to disk
-(void)saveSurvey:(id)sender {
    
    [self.view endEditing:YES];
    
    if (record == NULL) {
        record = [fieldDataService createRecord:attributes survey:survey inputFields:inputFields];
        
        RecordValidator *recordValidator = [[RecordValidator alloc] init];
        ValidationResult *validationResult = [recordValidator validate:record];
        
        // check if all the mandatory fields have been entered
        if (validationResult.valid) {
            
            UIAlertView *alertView =[[UIAlertView alloc]
                                     initWithTitle:@"Upload Survey"
                                           message:@"Would you like to upload this survey immediately?"
                                          delegate:self
                                 cancelButtonTitle:@"No"
                                 otherButtonTitles:@"Yes", nil];
            
            [alertView show];
            
        } else {
            [AlertService DisplayMessageWithTitle:@"Observation Draft Saved"
                                          message:@"All mandatory fields (marked *) must be entered before your observations can be uploaded to the server."];
            //[[self navigationController] popViewControllerAnimated:YES];
            [self updateViewsWithValidationResults:validationResult];
            
        }
        
    } else {
        [fieldDataService updateRecord:record attributes:attributes inputFields:inputFields];
        
        if ([fieldDataService isRecordComplete:record]) {
            [AlertService DisplayMessageWithTitle:@"Observation Saved"
                                      message:@"Observation has been successfully updated and is ready to be uploaded."];
        } else {
            [AlertService DisplayMessageWithTitle:@"Observation Draft Saved"
                                          message:@"All mandatory fields (marked *) must be entered before your observations can be uploaded to the server."];

        }
        [[self navigationController] popViewControllerAnimated:YES];
    }
    
}

-(void)updateViewsWithValidationResults:(ValidationResult*)validationResult
{
    [invalidAttributes removeAllObjects];
    for (AttributeError *error in validationResult.errors) {
        NSNumber *attributeId = error.attributeId;
        [invalidAttributes addObject:attributeId];
    }
    
    [self.tableView reloadData];
//            NSLog(@"Attributeid=%@, invalid attr=%@", attribute.weight, attributeId);
//            if ([attribute.weight isEqualToNumber:attributeId]) {
//                UITableViewCell *cell = [self tableView:self.tableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:(row+1) inSection:0]];
//                [cell.layer setBorderColor:[UIColor redColor].CGColor];
//                [cell.layer setBorderWidth:2.0f];
//                
//                //[self.tableView reloadData];
//                
//            }
//        }
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
    return attributes.count+1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 0) {
        return [self surveyNameCell:tableView];
    }
//    else if (indexPath.row == 1)
//    {
//        return [self validationSummaryCell:tableView];
//    }
    SurveyAttribute* attribute = [self attributeForPath:indexPath];
    NSString *CellIdentifier = [attribute.weight stringValue];
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    if (cell == nil) {
        NSString* mandatory = @"";
        if ([attribute.required intValue] == 1  && ![attribute.question hasSuffix:@"*"]) {
            mandatory = @" *";
        }
            
        if ([attribute.typeCode isEqualToString:kIntegerType]) {
            IntegerInputCell* intCell = [[IntegerInputCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                                                  reuseIdentifier:CellIdentifier];
            intCell.label.text = [NSString stringWithFormat:@"%@%@", attribute.question, mandatory];
            intCell.inputField.text = [loadedValues objectForKey:attribute.weight];
            cell = intCell;
            [inputFields setObject:intCell.inputField forKey:attribute.weight];
            
        } else if ([attribute.typeCode isEqualToString:kMultiSelect]) {
            SingleSelectCell* singleCell = [[SingleSelectCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                                                     reuseIdentifier:CellIdentifier
                                                                     options:attribute.options.allObjects];
            singleCell.label.text = [NSString stringWithFormat:@"%@%@", attribute.question, mandatory];
            [singleCell setSelectedValue:[NSString stringWithFormat:@"%@", [loadedValues objectForKey:attribute.weight]]];
            cell = singleCell;
            [inputFields setObject:singleCell.value forKey:attribute.weight];
            
        } else if ([attribute.typeCode isEqualToString:kStringWithValidValues]) {
            SingleSelectListCell* listCell = [[SingleSelectListCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                                                         reuseIdentifier:CellIdentifier
                                                                         options:attribute.options.allObjects];
            listCell.label.text = [NSString stringWithFormat:@"%@%@", attribute.question, mandatory];
            [listCell setSelectedValue:[loadedValues objectForKey:attribute.weight]];
            cell = listCell;
            [inputFields setObject:listCell.value forKey:attribute.weight];
        } else if ([attribute.typeCode isEqualToString:kMultiCheckbox]) {
            SingleSelectListCell* listCell = [[SingleSelectListCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                                                         reuseIdentifier:CellIdentifier
                                                                                 options:attribute.options.allObjects];
            listCell.label.text = [NSString stringWithFormat:@"%@%@", attribute.question, mandatory];
            [listCell setSelectedValue:[loadedValues objectForKey:attribute.weight]];
            cell = listCell;
            [inputFields setObject:listCell.value forKey:attribute.weight];

        } else if ([attribute.typeCode isEqualToString:kImage]) {
            ImageCell* imageCell = [[ImageCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
            imageCell.label.text = [NSString stringWithFormat:@"%@%@", attribute.question, mandatory];
            imageCell.parentController = self;
            [imageCell updateImage:[loadedValues objectForKey:attribute.weight]];
            cell = imageCell;
            [inputFields setObject:imageCell.filePath forKey:attribute.weight];
        } else if ([attribute.typeCode isEqualToString:kSpeciesRP]) {
            speciesCell = [[LabelledSpeciesCell alloc]initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
            speciesCell.label.text = [NSString stringWithFormat:@"%@%@", attribute.question, mandatory];
            NSString *commonName = [loadedValues objectForKey:attribute.weight];
            if (commonName) {
                speciesCell.species=[fieldDataService findSpeciesByCommonName:commonName];
            }
            cell = speciesCell;
            [inputFields setObject:speciesCell.value forKey:attribute.weight];
            
        } else if ([attribute.typeCode isEqualToString:kPoint]) {
            locationCell = [[LocationCell alloc]initWithStyle:UITableViewCellStyleDefault
                                                                     reuseIdentifier:CellIdentifier];
            locationCell.delegate = self;
            [locationCell setLocation:[NSString stringWithFormat:@"%@", [loadedValues objectForKey:attribute.weight]]];
            cell = locationCell;
            [inputFields setObject:locationCell.value forKey:attribute.weight];
            
        } else if ([attribute.typeCode isEqualToString:kWhen] ||
                   [attribute.typeCode isEqualToString:kDate]) {
            
            DateCell *dateCell = [[DateCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
            dateCell.label.text = [NSString stringWithFormat:@"%@%@", attribute.question, mandatory];
            NSString *date = [loadedValues objectForKey:attribute.weight];
            if (date) {
                [dateCell setDate:date];
            }
            cell = dateCell;
            
            [inputFields setObject:dateCell.value forKey:attribute.weight];
        }
        else {
            TextInputCell* textCell = [[TextInputCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
            
            textCell.label.text = [NSString stringWithFormat:@"%@%@", attribute.question, mandatory];
            textCell.inputField.text = [loadedValues objectForKey:attribute.weight];
            cell = textCell;
            [inputFields setObject:textCell.inputField forKey:attribute.weight];
            
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
    }
    
    //NSLog(@"Index Path: %d  Label: %@", indexPath.row, cell.label.text);
    
    return cell;
}

- (void)tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.row == 0) {
        UIColor *color = [[UIColor alloc] initWithRed:206.0/255.0f green:243.0/255.0f blue:1.0f alpha:1.0f];
        cell.backgroundColor =  color;
    }
//    else if (indexPath.row ==1) {
//        cell.backgroundColor = [[UIColor redColor] colorWithAlphaComponent:0.7f];
//        
//    }
    else if (indexPath.row >= 1){
        SurveyAttribute* attribute = [self attributeForPath:indexPath];
        if ([invalidAttributes containsObject:attribute.weight]) {
           
            SurveyInputCell* inputCell = (SurveyInputCell*)cell;
            CGRect labelBounds = inputCell.label.bounds;
            inputCell.label.opaque = YES;
            inputCell.label.backgroundColor = [UIColor redColor];
            inputCell.label.textColor = [UIColor whiteColor];
            //[cell.layer setBorderColor:[UIColor redColor].CGColor];
            //[cell.layer setBorderWidth:2.0f];
            
//            UIImageView* errorIcon = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"incomplete.png"]];
//            errorIcon.frame = CGRectMake(
//                  labelBounds.origin.x + labelBounds.size.width,
//                  labelBounds.origin.y,
//                   15, 15);
//            [cell addSubview:errorIcon];
            
            
        }
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

-(UITableViewCell*)validationSummaryCell:(UITableView*)tableView
{
    static NSString *cellIdentifier = @"ValidationSummary";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:cellIdentifier];
        cell.accessoryType = UITableViewCellAccessoryNone;
        cell.textLabel.adjustsFontSizeToFitWidth = YES;
        cell.textLabel.text = @"Please provide a value for the following fields:";
        cell.textLabel.textColor = [UIColor whiteColor];
        cell.textLabel.backgroundColor = [UIColor clearColor];
        
        NSLog(@"Size: %f, %f", [cell.textLabel.font pointSize], [cell.detailTextLabel.font pointSize]);
        
        cell.detailTextLabel.text = @" \u2022 Description\n \u2022 something\n \u2022 something else";
        cell.detailTextLabel.numberOfLines = 0;
        cell.detailTextLabel.textColor = [UIColor whiteColor];
        cell.detailTextLabel.backgroundColor = [UIColor clearColor];
                
        
    }
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 0) {
        return 75;
    }
//    else if (indexPath.row == 1) {
//        NSString *text = @"Text";
//        CGSize textSize = [text sizeWithFont:[UIFont systemFontOfSize:17.0f]];
//
//        return textSize.height * 4 + 10;
//    }
    SurveyAttribute* attribute = [self attributeForPath:indexPath];
    if ([attribute.typeCode isEqualToString:kMultiSelect]) {
        return 200;
    } else if ([attribute.typeCode isEqualToString:kImage]) {
        ImageCell *cell = (ImageCell*)[self tableView:tableView cellForRowAtIndexPath:indexPath];
        if (cell.imageView.image) {
            return 140;
        }
        else {
            return 90;
        }
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
        return 90;
    }
}

-(SurveyAttribute*)attributeForPath:(NSIndexPath*)indexPath
{
    NSInteger offset = 1;
    return [attributes objectAtIndex:indexPath.row-offset];
    
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row > 1) {
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
    if (path.row > 1) {
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

- (void)locationSelected:(CLLocation *)selectedLocation
{
    [locationCell setFoundLocation:selectedLocation];
    NSLog(@"Found location!=%@", selectedLocation);
}

#pragma mark SpeciesSelectionDelegate implementation
-(void)speciesSelected:(Species *)species
{
    [speciesCell setSpecies:species];
}


@end
