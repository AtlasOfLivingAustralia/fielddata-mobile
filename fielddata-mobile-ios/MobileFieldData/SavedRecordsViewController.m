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
#import "AlertService.h"

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
        
        fieldDataService.uploadDelegate = self;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    UIBarButtonItem *uploadButton = [[UIBarButtonItem alloc]
                                   initWithBarButtonSystemItem:UIBarButtonSystemItemAction
                                                        target:self
                                                        action:@selector(uploadSurveys:)];
    
    self.navigationItem.rightBarButtonItem = uploadButton;
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.tableView = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [self.tableView performSelectorOnMainThread:@selector(reloadData) withObject:nil waitUntilDone:NO];
}

// Save the survey to disk
-(void)uploadSurveys:(id)sender {
    
    numRecordsToUpload = 0;
    uploadedRecordCount = 0;
    uploadsSuccessful = YES;
    
    // count the number of records to upload
    for (Record *record in recordList) {
        
        if ([fieldDataService isRecordComplete:record]) {
            numRecordsToUpload++;
        }
    }
    
    dispatch_queue_t dispatchQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    
    dispatch_async(dispatchQueue, ^(void){
        
        dispatch_sync(dispatch_get_main_queue(), ^{
            // Show the progress indicator
            [self showProgressIndicator];
        });
        
        dispatch_sync(dispatch_get_main_queue(), ^{
            // upload the completed records
            for (Record *record in recordList) {
        
                if ([fieldDataService isRecordComplete:record]) {
                    [fieldDataService uploadRecord:record];
                }
            }
        });
    
    });
}

- (void)uploadSurveysSuccessful:(BOOL)success {
    
    uploadedRecordCount++;
    uploadsSuccessful = uploadsSuccessful && success;
    
    if (uploadedRecordCount == numRecordsToUpload) {
        // reload the records
        recordList = [fieldDataService loadRecords];
        
        [self hideProgressIndicator];
        
        if (uploadsSuccessful) {
            NSString* message = [NSString stringWithFormat:@"%i completed surveys have been successfully uploaded.",
                                 numRecordsToUpload];
            [AlertService DisplayMessageWithTitle:@"Upload Successful" message:message];
        } else {
            NSString* message = [NSString stringWithFormat:@"Not all surveys have been successfully uploaded, please try again later."];
            [AlertService DisplayMessageWithTitle:@"Upload Failed" message:message];
        }
    
        [self.tableView performSelectorOnMainThread:@selector(reloadData) withObject:nil waitUntilDone:NO];
    }
    
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
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"d MMMM y HH:mm"];
    NSLocale *ausLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_AUS"];
    [dateFormatter setLocale:ausLocale];
    
    cell.textLabel.text = [NSString stringWithFormat:@"%@", [dateFormatter stringFromDate:record.date]];
    cell.detailTextLabel.text = record.survey.surveyDescription;
    
    if ([fieldDataService isRecordComplete:record]) {
        cell.imageView.image = [UIImage imageNamed:@"complete.png"];
    } else {
        cell.imageView.image = [UIImage imageNamed:@"incomplete.png"];
    }
    
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
    
    // Navigation logic may go here. Create and push another view controller.
    SurveyViewController* surveyViewController =
    [[SurveyViewController alloc] initWithStyle:UITableViewStylePlain
                                         survey:record.survey
                                         record:record];
    
    [self.navigationController pushViewController:surveyViewController animated:YES];
    
}
                   
-(void)showProgressIndicator
{
    // add the progress indicator to the view
    progressIndicator = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
        
    // Set properties
    progressIndicator.labelText = @"Uploading Surveys";
}
                  
-(void)hideProgressIndicator
{
    [MBProgressHUD hideAllHUDsForView:self.view animated:YES];
}
                   

@end
