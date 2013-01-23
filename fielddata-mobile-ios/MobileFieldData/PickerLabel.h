//
// Redefines the inputView and inputAccessoryView properties to allow the label to use an input view to display a picker.
// Intended for use with date and time fields.
//

#import <UIKit/UIKit.h>

@interface PickerLabel : UILabel
    @property (strong, nonatomic, readwrite) UIView* inputView;
    @property (strong, nonatomic, readwrite) UIView* inputAccessoryView;
@end
