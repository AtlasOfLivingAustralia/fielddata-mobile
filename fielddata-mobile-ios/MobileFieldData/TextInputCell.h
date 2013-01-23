//
//  TextInputCell.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 20/09/12.
//
//

#import <UIKit/UIKit.h>
#import "SurveyInputCell.h"

@interface TextInputCell : UITableViewCell <UITextFieldDelegate>

@property (nonatomic, retain) UILabel* label;
@property (nonatomic, retain) UITextField *inputField;
@property (nonatomic, retain) UITextView *inputView;
@end
