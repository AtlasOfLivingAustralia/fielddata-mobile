//
//  SingleSelectListCell.h
//  MobileFieldData
//
//  Created by Chris Godwin on 3/12/12.
//
//

#import <UIKit/UIKit.h>
#import "SurveyInputCell.h"

@interface SingleSelectListCell : SurveyInputCell

@property (nonatomic, retain) UILabel* valueLabel;
@property (nonatomic, retain) NSArray* options;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier options:(NSArray*)o;
- (void)setSelectedValue:(NSString*)value;
-(NSString*)getSelectedValue;

@end
