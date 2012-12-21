//
//  SingleSelectListCell.h
//  MobileFieldData
//
//  Created by Chris Godwin on 3/12/12.
//
//

#import <UIKit/UIKit.h>

@interface SingleSelectListCell : UITableViewCell

@property (nonatomic, retain) UILabel* label;
@property (nonatomic, retain) UILabel* valueLabel;
@property (nonatomic, retain) NSArray* options;
@property (nonatomic, retain) NSMutableString* value;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier options:(NSArray*)o;
- (void)setSelectedValue:(NSString*)value;
-(NSString*)getSelectedValue;

@end
