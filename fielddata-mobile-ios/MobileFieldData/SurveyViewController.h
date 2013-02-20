//
//  SurveyViewController.h
//  MobileFieldData
//
//  Created by Birks, Matthew (CSIRO IM&T, Yarralumla) on 20/09/12.
//
//

#import <UIKit/UIKit.h>
#import "FieldDataService.h"
#import "Survey.h"
#import "SurveyAttribute.h"
#import "SurveyAttributeOption.h"
#import "Record.h"
#import "MBProgressHUD.h"
#import "MapViewController.h"
#import "SpeciesSelectionViewController.h"
#import "LocationCell.h"

@interface SurveyViewController : UITableViewController <UIAlertViewDelegate, FieldDataServiceUploadDelegate, MapViewControllerDelegate, SpeciesSelectionDelegate, LocationCellDelegate> {
    
    @private
    FieldDataService* fieldDataService;
    Survey* survey;
    Record* record;
    NSArray* attributes;
    NSMutableDictionary* inputFields;
    NSMutableDictionary* loadedValues;
    NSMutableArray* invalidAttributes;
    MBProgressHUD *progressIndicator;
}

- (void)showMap;
- (void)locationSelected:(CLLocation *)selectedLocation;
- (id)initWithStyle:(UITableViewStyle)style survey:(Survey*)s record:(Record*)r;

@end
