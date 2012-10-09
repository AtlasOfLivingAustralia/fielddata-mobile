//
//  IntegerInputCell.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 21/09/12.
//
//

#import <UIKit/UIKit.h>
#import "SurveyInputCell.h"

@interface IntegerInputCell : UITableViewCell <UITextFieldDelegate>

@property (nonatomic, retain) UILabel* label;
@property (nonatomic, retain) UITextField *inputField;

@end
