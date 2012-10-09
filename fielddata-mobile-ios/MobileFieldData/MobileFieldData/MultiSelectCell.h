//
//  MultiSelectCell.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 25/09/12.
//
//

#import <UIKit/UIKit.h>

@interface MultiSelectCell : UITableViewCell <UIPickerViewDelegate, UIPickerViewDataSource> {
    @private
    NSMutableArray *selectedItems;
}

@property (nonatomic, retain) UILabel* label;
@property (nonatomic, retain) UIPickerView* picker;
@property (nonatomic, retain) NSArray* options;
@property (nonatomic, retain) NSMutableString* value;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier options:(NSArray*)o;

@end
