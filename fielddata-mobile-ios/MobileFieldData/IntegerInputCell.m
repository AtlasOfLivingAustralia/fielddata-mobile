//
//  IntegerInputCell.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 21/09/12.
//
//

#import "IntegerInputCell.h"

@implementation IntegerInputCell

@synthesize label, inputField;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        label = [[UILabel alloc] initWithFrame:CGRectMake(10, 0, self.bounds.size.width-20, 50)];
        label.font = [UIFont boldSystemFontOfSize:12.0];
        label.numberOfLines = 0;
        [self.contentView addSubview:label];
        
        inputField = [[UITextField alloc] initWithFrame:CGRectMake(10, 50, 100, 28)];
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



@end
