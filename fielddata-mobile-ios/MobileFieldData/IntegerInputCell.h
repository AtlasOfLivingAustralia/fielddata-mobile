//
//  IntegerInputCell.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 21/09/12.
//
//

#import <UIKit/UIKit.h>
#import "SurveyInputCell.h"

@interface IntegerInputCell : SurveyInputCell <UITextFieldDelegate>

@property (nonatomic, retain) UITextField *inputField;

@end
