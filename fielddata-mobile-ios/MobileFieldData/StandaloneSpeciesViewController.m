//
//  StandaloneSpeciesViewController.m
//  MobileFieldData
//
//  Created by Chris Godwin on 18/02/13.
//
//

#import "StandaloneSpeciesViewController.h"

@interface StandaloneSpeciesViewController () {
    Preferences *preferences;
}
@end

@implementation StandaloneSpeciesViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        preferences = [[Preferences alloc] init];
    }
    return self;
}
- (void)viewDidLoad
{
    [super viewDidLoad];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [self.tableView deselectRowAtIndexPath:indexPath animated:NO];
 
    Species *species = [speciesLoader objectAtIndexPath:indexPath];
    NSString *urlString = [NSString stringWithFormat:@"%@survey/fieldguide/%@", [preferences getFieldDataURL], species.taxonId];
    
    NSURL *url = [NSURL URLWithString:urlString];
    [[UIApplication sharedApplication] openURL:url];
    
}


@end
