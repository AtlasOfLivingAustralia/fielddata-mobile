//
//  IntegerInputCell.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 21/09/12.
//
//

#import "IntegerInputCell.h"

@implementation IntegerInputCell

@synthesize inputField;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        
        inputField = [[UITextField alloc] initWithFrame:CGRectMake(10, 35, 100, 28)];
        inputField.borderStyle = UITextBorderStyleRoundedRect;
        inputField.keyboardType = UIKeyboardTypeDecimalPad;
        inputField.delegate = self;
        
        [self.contentView addSubview:inputField];

    }
    return self;
}

// only allow numeric characters
- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    for (int i = 0; i < [string length]; i++) {
        char c = [string characterAtIndex:i];
        // Allow a leading '-' for negative integers
        if (!((c == '-' && i == 0) || (c >= '0' && c <= '9'))) {
            return NO;
        }
    }
    return YES;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return NO;
}

-(void)textFieldDidEndEditing:(UITextField*)textField
{
    self.value = textField.text;
}


@end
