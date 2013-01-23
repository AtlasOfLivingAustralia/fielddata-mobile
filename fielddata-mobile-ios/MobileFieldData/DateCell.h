//
//  DateCell.h
//  MobileFieldData
//
//  Created by Chris Godwin on 17/01/13.
//
//

#import <UIKit/UIKit.h>

@interface DateCell : UITableViewCell {

  
}
@property (strong) UILabel *label;
@property (readonly, strong) NSMutableString* value;

-(void)setDate:(NSString*)dateString;

@end
