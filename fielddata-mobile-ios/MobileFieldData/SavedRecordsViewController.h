//
//  SavedRecordsViewController.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 4/10/12.
//
//

#import <UIKit/UIKit.h>
#import "FieldDataService.h"
#import "MBProgressHUD.h"

@interface SavedRecordsViewController : UITableViewController <FieldDataServiceUploadDelegate>{

    @private
    FieldDataService* fieldDataService;
    NSArray* recordList;
    int numRecordsToUpload;
    int uploadedRecordCount;
    MBProgressHUD *progressIndicator;
    BOOL uploadsSuccessful;
}

@end
