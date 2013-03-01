//
//  SpeciesGroup.h
//  MobileFieldData
//
//  Created by Chris Godwin on 20/02/13.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class Species, SpeciesGroup;

@interface SpeciesGroup : NSManagedObject

@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSString* groupDescription;
@property (nonatomic, retain) SpeciesGroup *parent;
@property (nonatomic, retain) NSSet *species;
@property (nonatomic, retain) NSSet *subgroups;
@property (nonatomic, retain) NSNumber *groupId;
@end

@interface SpeciesGroup (CoreDataGeneratedAccessors)

- (void)addSpeciesObject:(Species *)value;
- (void)removeSpeciesObject:(Species *)value;
- (void)addSpecies:(NSSet *)values;
- (void)removeSpecies:(NSSet *)values;

- (void)addSubgroupsObject:(SpeciesGroup *)value;
- (void)removeSubgroupsObject:(SpeciesGroup *)value;
- (void)addSubgroups:(NSSet *)values;
- (void)removeSubgroups:(NSSet *)values;

@end
