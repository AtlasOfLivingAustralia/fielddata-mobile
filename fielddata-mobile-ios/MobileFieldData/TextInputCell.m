//
//  TextInputCell.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 20/09/12.
//
//

#import "TextInputCell.h"

@implementation TextInputCell

@synthesize label, inputField, inputView;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        label = [[UILabel alloc] initWithFrame:CGRectMake(10, 0, self.bounds.size.width-20, 50)];
        label.font = [UIFont boldSystemFontOfSize:12.0];
        label.numberOfLines = 0;
        [self.contentView addSubview:label];
        
        inputField = [[UITextField alloc] initWithFrame:CGRectMake(10, 50, self.bounds.size.width-20, 28)];
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


@end
