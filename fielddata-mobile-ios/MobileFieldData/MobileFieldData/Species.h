//
//  Species.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 14/09/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface Species : NSManagedObject

@property (nonatomic, retain) NSString * scientificName;
@property (nonatomic, retain) NSString * commonName;
@property (nonatomic, retain) NSString * imageFileName;

@end
