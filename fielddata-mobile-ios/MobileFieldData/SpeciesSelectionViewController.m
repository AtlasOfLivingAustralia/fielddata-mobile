//
//  SpeciesSelectionViewController.m
//  MobileFieldData
//
//  Created by Chris Godwin on 29/01/13.
//
//

#import "SpeciesSelectionViewController.h"
#import "SpeciesTableCell.h"

@interface SpeciesSelectionViewController () {
    UIBarButtonItem *doneButton;
    Species *initialSelection;
}
@end

@implementation SpeciesSelectionViewController

@synthesize selectedSpecies, delegate;

- (id)initWithStyle:(UITableViewStyle)style selectedSpecies:(Species*)intialSpeciesSelection speciesIds:(NSArray*)speciesIds
{
    if (speciesIds && speciesIds.count > 0) {
        self = [super initWithStyle:style speciesIds:speciesIds];
    }
    else {
        self = [super initWithStyle:style];
    }
    if (self) {
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
        
        [self.tableView scrollToRowAtIndexPath:[speciesLoader indexPathForObject:initialSelection] atScrollPosition:UITableViewScrollPositionMiddle animated:YES];
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
    doneButton.enabled = (species != nil);
}

-(void)doSearch:(NSString*)searchText
{
    [super doSearch:searchText];
}



#pragma mark - Table data source
// This method is overridden to configure the checkmark for the selected species.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    SpeciesTableCell *cell = (SpeciesTableCell*)[super tableView:tableView cellForRowAtIndexPath:indexPath];
    
    if ([cell.species isEqual:self.selectedSpecies]) {
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
    [super tableView:tableView didSelectRowAtIndexPath:indexPath];
    
    NSIndexPath* currentSelectedPath = nil;
    if (self.selectedSpecies) {
        currentSelectedPath = [speciesLoader indexPathForObject:self.selectedSpecies];
    }
    
    Species* species = [speciesLoader objectAtIndexPath:indexPath];
    if ([species isEqual:self.selectedSpecies]) {
        // If the user selects the same cell, treat this as a de-select.
        self.selectedSpecies = nil;
    }
    else {
        self.selectedSpecies = species;
    }
    
    NSArray* paths = [NSArray arrayWithObject:indexPath];
    
    // If we have a current selection (different to the new selection), add that row to the
    // ones that need to be reloaded / redrawn.
    if (currentSelectedPath && ![currentSelectedPath isEqual:indexPath]) {
        paths = [paths arrayByAddingObject:currentSelectedPath];
    }
    
    [self.tableView reloadRowsAtIndexPaths:paths withRowAnimation:UITableViewRowAnimationFade];
}

@end
