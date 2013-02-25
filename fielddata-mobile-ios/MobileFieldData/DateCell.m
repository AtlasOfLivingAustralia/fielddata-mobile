#import <UIKit/UIKit.h>
#import "DateCell.h"
#import "Record.h"

@interface DateCell() {

    UIDatePicker *picker;
    UIToolbar *inputAccessoryView;
    
}
@property (strong) UILabel *dateLabel;
@end

@implementation DateCell

@synthesize dateLabel;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        _value = [[NSMutableString alloc]init];
        
        dateLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, 20, self.bounds.size.width-35, 30)];
        dateLabel.font = [UIFont systemFontOfSize:12.0];
        dateLabel.text = @"";
        
        [self.contentView addSubview:dateLabel];
        
        picker = [[UIDatePicker alloc] initWithFrame:CGRectZero];
        picker.datePickerMode = UIDatePickerModeDate;
        picker.autoresizingMask = UIViewAutoresizingFlexibleHeight;
        [picker addTarget:self action:@selector(dateChanged:) forControlEvents:UIControlEventValueChanged];
        
        // Initialise to today's date
        [picker setDate:[NSDate date]];
        [self dateChanged:nil];
    }
    return self;
}

-(BOOL)canBecomeFirstResponder
{
    return YES;
}

-(BOOL)becomeFirstResponder
{
    NSDate *date = [Record stringToDate:self.value];
    if (date) {
        picker.date = date;
    }
    return [super becomeFirstResponder];
}

- (BOOL)resignFirstResponder {
	
	UITableView *tableView = (UITableView *)self.superview;
	[tableView deselectRowAtIndexPath:[tableView indexPathForCell:self] animated:YES];
    
	return [super resignFirstResponder];
}

-(UIView *)inputView
{
    return picker;
}

-(UIView *)inputAccessoryView
{
    if (!inputAccessoryView) {
        inputAccessoryView = [[UIToolbar alloc] init];
        inputAccessoryView.barStyle = UIBarStyleBlackTranslucent;
        inputAccessoryView.autoresizingMask = UIViewAutoresizingFlexibleHeight;
        [inputAccessoryView sizeToFit];
        CGRect frame = inputAccessoryView.frame;
        inputAccessoryView.frame = frame;
    
        UIBarButtonItem *doneBtn =[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(done:)];
        UIBarButtonItem *flexibleSpaceLeft = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
        NSArray *array = [NSArray arrayWithObjects:flexibleSpaceLeft, doneBtn, nil];
        [inputAccessoryView setItems:array];
    }
    return inputAccessoryView;
}

-(void)setDate:(NSString *)dateString
{
    [self.value setString:dateString];
    self.dateLabel.text = dateString;
    [self.dateLabel setNeedsDisplay];
    
}

-(void)done:(id)sender
{
    [self resignFirstResponder];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];
    if (selected) {
		[self becomeFirstResponder];
	}
    
    [self.dateLabel setNeedsDisplay];
   
}


-(IBAction)dateChanged:(id)sender
{
    NSLog(@"The picker date has changed");
    NSString *dateString = [Record dateToString:picker.date];
    [self setDate:dateString];
}

@end
