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

@property (nonatomic, retain) NSString * commonName;
@property (nonatomic, retain) NSString * imageFileName;
@property (nonatomic, retain) NSString * scientificName;
@property (nonatomic, retain) NSNumber * taxonId;

@end
