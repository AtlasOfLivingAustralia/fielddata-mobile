//
//  SpeciesListViewController.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 14/09/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FieldDataService.h"

@interface SpeciesListViewController : UITableViewController {
    
    @protected
    FieldDataService* fieldDataService;
    NSArray* speciesList;
    
}

@end
