//
//  SpeciesSelectionViewController.m
//  MobileFieldData
//
//  Created by Chris Godwin on 29/01/13.
//
//

#import "SpeciesSelectionViewController.h"

@interface SpeciesSelectionViewController () {
    UIBarButtonItem *doneButton;
    NSInteger selectedRow;
    Species *initialSelection;
}
@end

@implementation SpeciesSelectionViewController

@synthesize selectedSpecies, delegate;

- (id)initWithStyle:(UITableViewStyle)style selectedSpecies:(Species*)intialSpeciesSelection;
{
    self = [super initWithStyle:style];
    if (self) {
        
        selectedRow = NSNotFound;
        initialSelection = intialSpeciesSelection;
        
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    doneButton = [[UIBarButtonItem alloc]
                  initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(saveSelection:)];
    doneButton.enabled = NO;
    
    self.navigationItem.rightBarButtonItem = doneButton;
    
    UIBarButtonItem *cancelButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel target:self action:@selector(cancel:)];
    
    self.navigationItem.leftBarButtonItem = cancelButton;
    
    if (initialSelection) {
        self.selectedSpecies = initialSelection;
        [self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:selectedRow inSection:0] atScrollPosition:UITableViewScrollPositionMiddle animated:YES];
    }
}


-(IBAction)cancel:(id)sender {
    [self dismissModalViewControllerAnimated:YES];
}

-(IBAction)saveSelection:(id)sender {
    [delegate speciesSelected:selectedSpecies];
    [self dismissModalViewControllerAnimated:YES];
}

-(void)setSelectedSpecies:(Species*)species
{
    selectedSpecies = species;
    NSUInteger oldSelection = selectedRow;
    
    if (species == nil) {
        selectedRow = NSNotFound;
        doneButton.enabled = NO;
    }
    else {
        selectedRow = [speciesList indexOfObject:species];
        doneButton.enabled = YES;
    }

    NSArray *paths = [NSArray arrayWithObjects:[NSIndexPath indexPathForRow:oldSelection inSection:0], [NSIndexPath indexPathForRow:selectedRow inSection:0], nil];
    [self.tableView reloadRowsAtIndexPaths:paths withRowAnimation:NO];
}




#pragma mark - Table data source
// This method is overridden to configure the checkmark for the selected species.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [super tableView:tableView cellForRowAtIndexPath:indexPath];
    
    if (selectedRow == indexPath.row) {
        cell.accessoryType = UITableViewCellAccessoryCheckmark;
    }
    else {
        cell.accessoryType = UITableViewCellAccessoryNone;
    }
    return cell;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSArray* paths = [NSArray arrayWithObject:indexPath];
    
    // If we have a current selection (different to the new selection), add that row to the
    // ones that need to be reloaded / redrawn.
    if (selectedRow != NSNotFound && selectedRow != indexPath.row) {
        paths = [paths arrayByAddingObject:[NSIndexPath indexPathForRow:selectedRow inSection:0]];
    }
    
    if (selectedRow == indexPath.row) {
        self.selectedSpecies = nil;
    }
    else {
        self.selectedSpecies = [speciesList objectAtIndex:indexPath.row];
    }
    
    [self.tableView reloadRowsAtIndexPaths:paths withRowAnimation:UITableViewRowAnimationFade];
}

@end
