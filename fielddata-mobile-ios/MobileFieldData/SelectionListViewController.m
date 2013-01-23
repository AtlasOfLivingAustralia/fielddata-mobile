//
//  SelectionListViewController.m
//  MobileFieldData
//
//  Manages the selection from a list of values presented to the user.
//
//

#import "SelectionListViewController.h"
#import "SurveyAttributeOption.h"
#import "SingleSelectCell.h"

@interface SelectionListViewController () {
    // Two arrays are used instead of a dictionary as we need to maintain insertion order.
    NSMutableArray* headers;
    NSMutableArray* groupedOptions;
    UIBarButtonItem *doneButton;
}

-(void)determineSelection:(SingleSelectListCell *)cell;
-(void)initialiseAttributeOptions:(NSString*)separator;
-(NSIndexPath*)findValue:(NSString*)value;

@end


@implementation SelectionListViewController

@synthesize multiSelect;


-(id)initWithValues:(UITableViewStyle)style selectionValues:(NSArray*)selectionValues cell:(SingleSelectListCell*)cell
        multiSelect:(BOOL)multiSelec grouped:(BOOL)grouped
{
    self = [self initWithStyle:style];
    if (self) {
        multiSelect = multiSelec;
        NSArray *sortDescriptors = [NSArray arrayWithObject:[NSSortDescriptor sortDescriptorWithKey:@"weight" ascending:YES]];
        values = [selectionValues sortedArrayUsingDescriptors:sortDescriptors];

        parent = cell;
        self.title = @"Select an option";
        
        headers = [NSMutableArray arrayWithCapacity:values.count];
        groupedOptions = [NSMutableArray arrayWithCapacity:values.count];
        [self initialiseAttributeOptions:grouped ? @"-" : nil];
        [self determineSelection:cell];
        
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    doneButton = [[UIBarButtonItem alloc]
                                   initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(saveSelection:)];
    
    self.navigationItem.rightBarButtonItem = doneButton;
    doneButton.enabled = (selectedRows.count > 0);
    
    UIBarButtonItem *cancelButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel target:self action:@selector(cancel:)];
    
    self.navigationItem.leftBarButtonItem = cancelButton;
}

-(void)determineSelection:(SingleSelectListCell*)cell {
    
    selectedRows = [NSMutableArray array];
    NSString* selection = [cell getSelectedValue];
    
    if (selection != nil && selection.length > 0) {
        NSArray* selectedValues = [selection componentsSeparatedByString:@", "];
        for (int i=0; i<selectedValues.count; i++) {
            NSIndexPath* row = [self findValue:selectedValues[i]];
            if (row != nil) {
                [selectedRows addObject:row];
            }
        }
    }
    

}

- (NSIndexPath*)findValue:(NSString *)value
{
    int index = 0;
    if (value != nil) {
        for (NSInteger section=0; section<headers.count; section++) {
            NSArray* options = [groupedOptions objectAtIndex:section];
            for (NSInteger row=0; row<options.count; row++) {
                SurveyAttributeOption* option = [values objectAtIndex:index];
                if ([option.value isEqualToString:value]) {
                    return [NSIndexPath indexPathForRow:row inSection:section];
                                    }
                index++;
            
            }
        }
    }
    return nil;
}

-(void)initialiseAttributeOptions:(NSString*)groupIdentifier {
    
    NSString* header = @"";
    NSString* previousHeader = nil;
    NSMutableArray* optionGroup = [NSMutableArray arrayWithCapacity:values.count];
    
    for (SurveyAttributeOption *option in values)  {
        NSString *optionValue = option.value;
        NSArray* split = [self splitIntoHeaderAndValue:groupIdentifier value:optionValue];
        header = split[0];
        NSLog(@"Value: %@ Header: %@", header, option.value);
        
        if (previousHeader != nil && ![header isEqualToString:previousHeader]) {
            [headers addObject:previousHeader];
            [groupedOptions addObject:optionGroup];
            
            NSLog(@"Adding: %@ with %d values", header, optionGroup.count);
            optionGroup = [NSMutableArray arrayWithCapacity:values.count];
        }
        [optionGroup addObject:split[1]];
        previousHeader = [header copy];
    }
    [headers addObject:header];
    [groupedOptions addObject:optionGroup];
    

}


-(void)saveSelection:(id)sender {
    if (selectedRows != nil) {
        
        NSMutableString* selectedValue = [NSMutableString stringWithString:[self valueAt:selectedRows[0] fullValue:YES]];
        for (int i=1; i<selectedRows.count; i++) {
            [selectedValue appendString: @", "];
            [selectedValue appendString: [self valueAt:selectedRows[i] fullValue:YES]];
        }
        
        [parent setSelectedValue:[selectedValue copy]];
    }
    [self dismissModalViewControllerAnimated:YES];
}

-(NSString*)valueAt:(NSIndexPath*) path fullValue:(BOOL)fullValue {
    NSArray* group  = [groupedOptions objectAtIndex:path.section];
    if (fullValue && headers.count > 1) {
        NSMutableString* result = [NSMutableString stringWithString:[headers objectAtIndex:path.section]];
        [result appendString:@"-"];
        [result appendString:[group objectAtIndex: path.row]];
        return result;
    }
    return [group objectAtIndex: path.row];
}

-(void)cancel:(id)sender {
    [self dismissModalViewControllerAnimated:YES];
}




- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(NSArray*)splitIntoHeaderAndValue:(NSString*)separator value:(NSString*) value {
    
    NSString* header = @"";
    NSString* rest = [value copy];
    NSInteger separatorPos = separator ? [value rangeOfString:separator].location : NSNotFound;
    if (separatorPos != NSNotFound) {
        header = [value substringToIndex:separatorPos];
        rest = [value substringFromIndex:separatorPos+1];
    }
    
    return [NSArray arrayWithObjects:header,rest, nil];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [headers count];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    return [headers objectAtIndex:section];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return ((NSArray*)[groupedOptions objectAtIndex:section]).count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Cell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        cell.accessoryType = UITableViewCellAccessoryNone;
        cell.textLabel.adjustsFontSizeToFitWidth = YES;
        cell.textLabel.font = [UIFont systemFontOfSize:15.0];
        cell.textLabel.minimumFontSize = 10.0;
        cell.textLabel.numberOfLines = 0;
    }
    
    cell.textLabel.text =  [self valueAt:indexPath fullValue:NO];
    
    if ([selectedRows containsObject:indexPath]) {
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
    
    if (!multiSelect) {
        if (selectedRows.count > 0) {
            paths = [paths arrayByAddingObject:selectedRows[0]];
        }
    
    
        selectedRows = [NSArray arrayWithObject:[indexPath copy]];
    }
    else {
        
        if ([selectedRows containsObject:indexPath]) {
            [selectedRows removeObject:indexPath];
        }
        else {
            [selectedRows addObject:indexPath];
        }
        
    }
    [self.tableView reloadRowsAtIndexPaths:paths withRowAnimation:UITableViewRowAnimationFade];
    doneButton.enabled = YES;
 }

@end


