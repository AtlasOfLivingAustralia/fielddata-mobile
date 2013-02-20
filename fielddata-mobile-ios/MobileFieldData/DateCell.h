//
//  DateCell.h
//  MobileFieldData
//
//  Created by Chris Godwin on 17/01/13.
//
//

#import <UIKit/UIKit.h>
#import "SurveyInputCell.h"

@interface DateCell : SurveyInputCell {

    
}
@property (readonly, strong) NSMutableString* value;

-(void)setDate:(NSString*)dateString;

@end
