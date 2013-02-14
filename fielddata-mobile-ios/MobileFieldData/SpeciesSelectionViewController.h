//
//  SpeciesSelectionViewController.h
//  MobileFieldData
//
//  Created by Chris Godwin on 29/01/13.
//
//

#import "SpeciesListViewController.h"
#import "Species.h"

@protocol SpeciesSelectionDelegate <NSObject>

-(void)speciesSelected:(Species*)species;

@end

@interface SpeciesSelectionViewController : SpeciesListViewController

- (id)initWithStyle:(UITableViewStyle)style selectedSpecies:(Species*)species speciesIds:(NSArray*)speciesIds;

@property (nonatomic, weak) id<SpeciesSelectionDelegate> delegate;

@property (nonatomic, strong) Species* selectedSpecies;

@end
