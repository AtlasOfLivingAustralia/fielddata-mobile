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


@end
