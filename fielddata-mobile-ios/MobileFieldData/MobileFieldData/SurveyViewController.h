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

@interface SurveyViewController : UITableViewController {
    
    @private
    FieldDataService* fieldDataService;
    Survey* survey;
    NSArray* attributes;
    NSMutableDictionary* inputFields;
}

@property (nonatomic, retain) NSMutableDictionary* loadedValues;

- (id)initWithStyle:(UITableViewStyle)style survey:(Survey*)s;

@end
