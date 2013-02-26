//
//  LabelledSpeciesCell.m
//  MobileFieldData
//
//  Created by Chris Godwin on 29/01/13.
//
//

#import "LabelledSpeciesCell.h"
#import "Species.h"

@interface LabelledSpeciesCell() {
 
    UILabel *scientificNameLabel;
    UILabel *commonNameLabel;
}
@end

@implementation LabelledSpeciesCell

@synthesize imageView, species;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        
        self.accessoryType = UITableViewCellAccessoryDisclosureIndicator;

        imageView = [[UIImageView alloc] initWithFrame:CGRectMake(10, 23, 40, 40)];
        [self addSubview:imageView];
        
        commonNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(55, 28, self.bounds.size.width-60, 15)];
        commonNameLabel.font = [UIFont systemFontOfSize:14];
        [self addSubview:commonNameLabel];
        
        scientificNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(55, 43, self.bounds.size.width-60, 15)];
        scientificNameLabel.font = [UIFont italicSystemFontOfSize:12];
        [self addSubview:scientificNameLabel];
        
    }
    return self;
}



-(void)setSpecies:(Species *)selectedSpecies
{
    species = selectedSpecies;
    commonNameLabel.text = species.commonName;
    scientificNameLabel.text = species.scientificName;
    
    imageView.image = [UIImage imageWithContentsOfFile:species.imageFileName];
    if (species.commonName) {
        self.value = species.commonName;
    }
    else {
        self.value = @"";
    }
}

@end
