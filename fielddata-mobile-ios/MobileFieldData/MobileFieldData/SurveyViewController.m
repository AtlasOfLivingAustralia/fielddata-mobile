//
//  SurveyViewController.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 20/09/12.
//
//

#import "SurveyViewController.h"
#import "TextInputCell.h"
#import "IntegerInputCell.h"
#import "SingleSelectCell.h"
#import "MultiSelectCell.h"
#import "ImageCell.h"
#import "SpeciesCell.h"
#import "LocationCell.h"

@interface SurveyViewController ()

@end

@implementation SurveyViewController

@synthesize loadedValues;

- (id)initWithStyle:(UITableViewStyle)style survey:(Survey*)s
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
        fieldDataService = [[FieldDataService alloc]init];
        survey = s;
        
        NSArray *sortDescriptors = [NSArray arrayWithObject:[NSSortDescriptor sortDescriptorWithKey:@"weight" ascending:YES]];
        attributes = [[survey.attributes allObjects] sortedArrayUsingDescriptors:sortDescriptors];
        
        inputFields = [NSMutableDictionary dictionaryWithCapacity:attributes.count];
        
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    UIBarButtonItem *saveButton = [[UIBarButtonItem alloc]
            initWithBarButtonSystemItem:UIBarButtonSystemItemSave target:self action:@selector(saveSurvey:)];
    
    self.navigationItem.rightBarButtonItem = saveButton;
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

// Save the survey to disk
-(void)saveSurvey:(id)sender {
    
    [fieldDataService createRecord:attributes inputFields:inputFields];
    
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
    return attributes.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    
    SurveyAttribute* attribute = [attributes objectAtIndex:indexPath.row];
    NSString *CellIdentifier = [attribute.weight stringValue];
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    if (cell == nil) {
        if ([attribute.typeCode isEqualToString:kIntegerType]) {
            IntegerInputCell* intCell = [[IntegerInputCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
            intCell.label.text = attribute.question;
            //intCell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            cell = intCell;
            [inputFields setObject:intCell.inputField forKey:attribute.weight];
            
        } else if ([attribute.typeCode isEqualToString:kMultiSelect]) {
            SingleSelectCell* singleCell = [[SingleSelectCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                                                     reuseIdentifier:CellIdentifier
                                                                     options:attribute.options.allObjects];
            singleCell.label.text = attribute.question;
            cell = singleCell;
            [inputFields setObject:singleCell.value forKey:attribute.weight];
            
        } else if ([attribute.typeCode isEqualToString:kMultiCheckbox]) {
            MultiSelectCell* multiCell = [[MultiSelectCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                                                    reuseIdentifier:CellIdentifier
                                                                    options:attribute.options.allObjects];
            multiCell.label.text = attribute.question;
            cell = multiCell;
            [inputFields setObject:multiCell.value forKey:attribute.weight];
        } else if ([attribute.typeCode isEqualToString:kImage]) {
            ImageCell* imageCell = [[ImageCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
            imageCell.label.text = attribute.question;
            imageCell.parentController = self;
            cell = imageCell;
            [inputFields setObject:imageCell.filePath forKey:attribute.weight];
        } else if ([attribute.typeCode isEqualToString:kSpeciesRP]) {
            NSArray* species = [fieldDataService loadSpecies];
            SpeciesCell* speciesCell = [[SpeciesCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier species:species];
            speciesCell.label.text = attribute.question;
            cell = speciesCell;
            [inputFields setObject:speciesCell.value forKey:attribute.weight];
        } else if ([attribute.typeCode isEqualToString:kPoint]) {
            LocationCell* locationCell = [[LocationCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
            cell = locationCell;
            [inputFields setObject:locationCell.value forKey:attribute.weight];
        } else {
            TextInputCell* textCell = [[TextInputCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
            textCell.label.text = attribute.question;
            //textCell.label.text = [NSString stringWithFormat:@"%@ %@", attribute.question, attribute.weight];
            //textCell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
            cell = textCell;
            [inputFields setObject:textCell.inputField forKey:attribute.weight];
        }
    }
    
    //NSLog(@"Index Path: %d  Label: %@", indexPath.row, cell.label.text);
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    SurveyAttribute* attribute = [attributes objectAtIndex:indexPath.row];
    if ([attribute.typeCode isEqualToString:kMultiSelect] ||
        [attribute.typeCode isEqualToString:kMultiCheckbox]) {
        return 200;
    } else if ([attribute.typeCode isEqualToString:kImage]) {
        return 140;
    } else if ([attribute.typeCode isEqualToString:kSpeciesRP]) {
        return 170;
    } else if ([attribute.typeCode isEqualToString:kPoint]) {
        return 120;
    } else {
        return 90;
    }
}



#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Navigation logic may go here. Create and push another view controller.
    /*
     <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:@"<#Nib name#>" bundle:nil];
     // ...
     // Pass the selected object to the new view controller.
     [self.navigationController pushViewController:detailViewController animated:YES];
     */
}

- (NSIndexPath *)tableView:(UITableView *)tv willSelectRowAtIndexPath:(NSIndexPath *)path
{
    return nil;
}


@end
