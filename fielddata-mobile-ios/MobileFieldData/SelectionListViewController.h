//
//  SelectionListViewController.h
//  MobileFieldData
//
//  Created by Chris Godwin on 27/11/12.
//
//

#import <UIKit/UIKit.h>
#import "SingleSelectListCell.h"

@interface SelectionListViewController : UITableViewController {
    @private
    NSArray* values;
    // Temporary storage for the selected option.
    NSMutableArray* selectedRows;
    
    SingleSelectListCell* parent;
 
}

@property (readonly) BOOL multiSelect;
-(id)initWithValues:(UITableViewStyle)style selectionValues:(NSArray*)selectionValues cell:(SingleSelectListCell*)result multiSelect:(BOOL)multiSelec;

@end
