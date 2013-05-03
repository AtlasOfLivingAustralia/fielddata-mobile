//
//  MasterViewController.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 10/09/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Preferences.h"
#import "SurveyDownloadController.h"

@class DetailViewController;

#import <CoreData/CoreData.h>

@interface MasterViewController : UITableViewController <SurveyDownloadDelegate> {
    @private
    Preferences *preferences;
    NSArray* surveys;
}

@end
