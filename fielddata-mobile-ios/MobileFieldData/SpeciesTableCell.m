//
//  SpeciesTableCell.m
//  MobileFieldData
//
// Adds an image to the left hand side of the cell.
//

#import "SpeciesTableCell.h"
#import "Species.h"

@implementation SpeciesTableCell

@synthesize imageView, species;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        
        imageView = [[UIImageView alloc] initWithFrame:CGRectMake(1, 1, 40, 40)];
        [self addSubview:imageView];
        self.indentationLevel = 1;
        self.indentationWidth = 35;
        
    }
    return self;
}


-(void)setSpecies:(Species *)selectedSpecies
{
    species = selectedSpecies;
    self.textLabel.text = species.commonName;
    self.detailTextLabel.text = species.scientificName;
    
    self.imageView.image = [UIImage imageWithContentsOfFile:species.imageFileName];
    [self.imageView setNeedsDisplay];
}

@end
