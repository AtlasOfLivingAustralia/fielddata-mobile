//
//  SavedRecordsViewController.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 4/10/12.
//
//

#import <UIKit/UIKit.h>
#import "FieldDataService.h"

@interface SavedRecordsViewController : UITableViewController {

    @private
    FieldDataService* fieldDataService;
    NSArray* recordList;
}

@end
