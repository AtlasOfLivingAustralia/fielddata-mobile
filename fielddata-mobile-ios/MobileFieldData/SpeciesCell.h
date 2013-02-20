//
//  SpeciesCell.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 28/09/12.
//
//

#import <UIKit/UIKit.h>
#import "SurveyInputCell.h"


@interface SpeciesCell : SurveyInputCell <UIPickerViewDelegate, UIPickerViewDataSource> {
    
}

@property (nonatomic, retain) UIPickerView* picker;
@property (nonatomic, retain) UIImageView* speciesImage;
@property (nonatomic, retain) NSArray* species;
@property (nonatomic, retain) NSMutableString* value;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier species:(NSArray*)s;

@end
