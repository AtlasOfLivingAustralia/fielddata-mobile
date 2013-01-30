//
//  LabelledSpeciesCell.h
//  MobileFieldData
//
//  Created by Chris Godwin on 29/01/13.
//
//

#import <UIKit/UIKit.h>
@class Species;

@interface LabelledSpeciesCell : UITableViewCell

@property (strong, nonatomic) UILabel *label;
@property (strong, nonatomic) UIImageView *imageView;
@property (strong, nonatomic) Species* species;
@property (strong, nonatomic) NSMutableString* value;

@end
