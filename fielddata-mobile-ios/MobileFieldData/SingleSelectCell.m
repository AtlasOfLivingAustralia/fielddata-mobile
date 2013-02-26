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

@synthesize picker, options;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier options:(NSArray*)o
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        
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
    for (int i=1; i<options.count+1; i++) {
        SurveyAttributeOption* option = [options objectAtIndex:i-1];
        if ([option.value isEqualToString:inputValue]) {
            [picker selectRow:i inComponent:0 animated:NO];
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
    //SurveyAttributeOption* option = [options objectAtIndex:row];
    //return option.value;
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component {
    
    if (row == 0) {
        self.value = @"";
        //SurveyAttributeOption* selectedOption = [options objectAtIndex:1];
        //[value setString:selectedOption.value];
        //[picker selectRow:1 inComponent:0 animated:YES];
    } else {
        SurveyAttributeOption* selectedOption = [options objectAtIndex:row-1];
        self.value = selectedOption.value;
    }
    //SurveyAttributeOption* selectedOption = [options objectAtIndex:row];
    //[value setString:selectedOption.value];
}



@end
