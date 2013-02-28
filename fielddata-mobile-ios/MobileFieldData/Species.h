//
//  Species.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 29/10/12.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface Species : NSManagedObject

@property (nonatomic, strong) NSString * commonName;
@property (nonatomic, strong) NSString * imageFileName;
@property (nonatomic, strong) NSString * scientificName;
@property (nonatomic, strong) NSNumber * taxonId;
// This is a denormalized attribute for convenience/performance of using a NSFetchedResultsController to display grouped species lists.
@property (nonatomic, strong) NSString * groupName;

@end
