//
//  TextInputCell.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 20/09/12.
//
//

#import "TextInputCell.h"

@implementation TextInputCell

@synthesize inputField;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        
        
        inputField = [[UITextField alloc] initWithFrame:CGRectMake(10, 35, self.bounds.size.width-20, 28)];
        inputField.borderStyle = UITextBorderStyleRoundedRect;
        inputField.delegate = self;
        
        [self.contentView addSubview:inputField];
    }
    return self;
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
