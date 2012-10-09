//
//  SurveyInputCell.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 21/09/12.
//
//

#import "SurveyInputCell.h"

@implementation SurveyInputCell

@synthesize label, value;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    //[super setSelected:selected animated:animated];
}

/*
- (void)setValue:(NSString*)val {
    value = val;
}*/

@end
