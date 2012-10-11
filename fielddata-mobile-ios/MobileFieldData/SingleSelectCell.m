//
//  MultiSelectCell.m
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 25/09/12.
//
//

#import "SingleSelectCell.h"
#import "SurveyAttributeOption.h"

@implementation SingleSelectCell

@synthesize label, picker, options, value;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier options:(NSArray*)o
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        label = [[UILabel alloc] initWithFrame:CGRectMake(10, 0, self.bounds.size.width-20, 50)];
        label.font = [UIFont boldSystemFontOfSize:12.0];
        label.numberOfLines = 0;
        [self.contentView addSubview:label];
        
        picker = [[UIPickerView alloc]init];
        picker.frame = CGRectMake(0, 40, self.bounds.size.width, 162.0);
        picker.delegate = self;
        picker.dataSource = self;
        picker.backgroundColor = [UIColor whiteColor];
        picker.showsSelectionIndicator = YES;
        
        picker.transform = CGAffineTransformMakeScale(0.8, 0.8);

        [self.contentView addSubview:picker];
        
        NSArray *sortDescriptors = [NSArray arrayWithObject:[NSSortDescriptor sortDescriptorWithKey:@"weight" ascending:YES]];
        options = [o sortedArrayUsingDescriptors:sortDescriptors];
        
        value = [[NSMutableString alloc]init];
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (void)setSelectedValue:(NSString*)inputValue
{
    for (int i=0; i<options.count; i++) {
        SurveyAttributeOption* option = [options objectAtIndex:i];
        if ([option.value isEqualToString:inputValue]) {
            [picker selectRow:i+1 inComponent:0 animated:NO];
            break;
        }
    }
}

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    return self.options.count+1;
}

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    if (row == 0) {
        return @"";
    } else {
        SurveyAttributeOption* option = [options objectAtIndex:row-1];
        return option.value;
    }
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component {
    if (row == 0) {
        //[value setString:@""];
        SurveyAttributeOption* selectedOption = [options objectAtIndex:1];
        [value setString:selectedOption.value];
        [picker selectRow:1 inComponent:0 animated:YES];
    } else {
        SurveyAttributeOption* selectedOption = [options objectAtIndex:row-1];
        [value setString:selectedOption.value];
    }
}



@end
