//
//  SavedRecordsViewController.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 4/10/12.
//
//

#import "SavedRecordsViewController.h"
#import "Record.h"
#import "RecordAttribute.h"
#import "FieldDataService.h"
#import "SurveyViewController.h"

@interface SavedRecordsViewController ()

@end

@implementation SavedRecordsViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
        fieldDataService = [[FieldDataService alloc]init];
        recordList = [fieldDataService loadRecords];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

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

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
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
    return recordList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Cell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    }
    // Configure the cell...
    Record* record = [recordList objectAtIndex:indexPath.row];
    cell.textLabel.text = [NSString stringWithFormat:@"%@", record.date];
    cell.detailTextLabel.text = record.survey.surveyDescription;
    
    return cell;
}

/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{

    Record* record = [recordList objectAtIndex:indexPath.row];
    
    NSMutableDictionary* inputFields = [NSMutableDictionary dictionaryWithCapacity:record.recordAttributes.count];
    
    for (RecordAttribute* recordAttribute in record.recordAttributes) {
        
        NSString* value = [NSString stringWithFormat:@"%@", recordAttribute.value];
        [inputFields setObject:value forKey:recordAttribute.surveyAttribute.weight];
        
        /*
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
        }*/
        
    }
    
    // Navigation logic may go here. Create and push another view controller.
    SurveyViewController* surveyViewController =
    [[SurveyViewController alloc] initWithStyle:UITableViewStylePlain survey:record.survey];
    
    [self.navigationController pushViewController:surveyViewController animated:YES];
    
}

@end
