//
//  MarginLabel.m
//  MobileFieldData
//
//  Created by Chris Godwin on 21/02/13.
//
//

#import <QuartzCore/QuartzCore.h>
#import "MarginLabel.h"

@implementation MarginLabel

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self.layer setBorderColor:[UIColor redColor].CGColor];
        [self.layer setCornerRadius:5.0f];
    }
    return self;
}

- (void)drawTextInRect:(CGRect)rect {
    UIEdgeInsets insets = {0, 5, 0, 5};
    return [super drawTextInRect:UIEdgeInsetsInsetRect(rect, insets)];
}

@end
