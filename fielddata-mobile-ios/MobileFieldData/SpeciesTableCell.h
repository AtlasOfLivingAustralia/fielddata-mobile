//
//  SpeciesTableCell.h
//  MobileFieldData
//
// Adds an image to the left side of the cell.
//

#import <UIKit/UIKit.h>

@class Species;

@interface SpeciesTableCell : UITableViewCell

@property (strong, nonatomic) UIImageView *imageView;
@property (strong, nonatomic) Species* species;

@end
