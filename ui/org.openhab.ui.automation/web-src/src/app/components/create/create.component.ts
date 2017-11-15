import { RuleHelper } from '../../helpers/rule-helper';
import { RuleMapperHelper } from '../../helpers/rule-mapper-helper';
import { Item } from '../../models/item';
import { Rule, RuleModule, EVENT_TYPE, CONDITION_TYPE, ACTION_TYPE } from '../../models/rule';
import { Thing } from '../../models/thing';
import { ItemService } from '../../services/item.service';
import { RuleService } from '../../services/rule.service';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { ThingService } from '../../services/thing.service';
import { ItemsComponent } from '../items/items.component';
import { JsonPipe, Location } from '@angular/common';
import { Component, OnInit, ViewChild, ElementRef, trigger, transition, style, animate } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';


import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/concat';


@Component({
  selector: 'app-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.css'],
  providers: [ItemService, ThingService, RuleService],
  animations: [
  trigger('fadeIn', [
    transition(':enter', [
      style({opacity: 0}),
      animate(500, style({opacity: 1})) 
    ])
  ])
]
})
export class CreateComponent implements OnInit {
  @ViewChild('bodyJson') jsonChild: ElementRef;
  things: Thing[];
  thingsWithEditableItems: Thing[];
  step = 0;
  rule: Rule;
  requiredFormControl = new FormControl('', [Validators.required]);
  edit: boolean;
  test = 0;

  ngOnInit(): void {
    this.rule = new Rule();
    const editString = this.route.snapshot.queryParams['edit'] || undefined;
    if (editString && editString === 'true'
        && this.sharedProperties.getRule() !== undefined && this.sharedProperties.getRule() !== null) {
      this.edit = true;
      this.rule = this.sharedProperties.getRule();
    } else {
      this.edit = false;
      this.sharedProperties.reset();
    }
    this.getItemsAndThings();
  }
  constructor(private itemService: ItemService, private sharedProperties: SharedPropertiesService,
  private thingService: ThingService, private ruleService: RuleService, private location: Location,
  private route: ActivatedRoute, private router: Router) { }

  getItemsAndThings(): void {
    let items = [];
    let things = [];
    this.itemService.getItems()
    .concat(this.thingService.getThings())
    .subscribe(res => {
      if (this.isThingArray(res)) {
        console.log('Fetched ' + res.length + ' things');
        things = res;
      } else if (this.isItemArray(res)) {
        console.log('Fetched ' + res.length + ' items');
        items = res;
      }
      if (things.length > 0 && items.length > 0) {
        // TODO: Fix this method. We assume length of things and length of items
        this.initializeThingsAndItems(things, items);
      }
      
    },
    error => this.handleError(error));
  }
  
  initializeThingsAndItems(things: Thing[], items: Item[]): void {
    this.things = this.addItemsToThings(items, things);
    this.thingsWithEditableItems = this.things.filter(t => t.editableItems && t.editableItems.length > 0);
    this.things.push(this.createTimeThing());
    if (this.edit) {
      this.addThingToRule();
      this.step = 5;
    } else {
      this.next();
    }
  }
  
  handleError(error: any): void {
    console.log('Error! ', error);
  }

  addItemsToThings(items: Item[], things: Thing[]): Thing[] {
    const thingsWithItems = [];
    for (const thing of things) {
      if (thing.channels && thing.channels.length > 0) {
        for (const channel of thing.channels) {
          if (channel.linkedItems && channel.linkedItems.length > 0) {
            for (const linkedItem of channel.linkedItems) {
              for (const item of items) {
                if (linkedItem === item.name) {
                  item.thingName = linkedItem;
                  if (thing.items !== undefined) {
                    thing.items.push(item);
                  } else {
                    thing['items'] = [];
                    thing.items.push(item);
                  }
                  if (!item.stateDescription || !item.stateDescription.readOnly) {
                    if (thing.editableItems !== undefined) {
                      thing.editableItems.push(item);
                    } else {
                      thing['editableItems'] = [];
                      thing.editableItems.push(item);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return things.filter(t => t.items !== undefined && t.items.length > 0);
  }

  addThingToRule(): void {
    console.log(this.rule);
    for (const event of this.rule.events) {
      const things = this.thingsWithEditableItems.filter(t => t.editableItems.filter(i => i.name === event.itemName).length > 0);
      if (things.length > 0) {
        event.thing = things[0];
      } else {
        console.log('No thing found for action ' + event.id);
      }
    }
    for (const action of this.rule.actions) {
      const things = this.thingsWithEditableItems.filter(t => t.editableItems.filter(i => i.name === action.itemName).length > 0);
      if (things.length > 0) {
        action.thing = things[0];
      } else {
        console.log('No thing found for action ' + action.id);
      }
    }
    for (const condition of this.rule.conditions) {
      
      const things = this.things.filter(t => t.items.filter(i => i.name === condition.itemName).length > 0);
      if (things.length > 0) {
        condition.thing = things[0];
        
      } else {
        console.log('No thing found for action ' + condition.id);
      }
    }
  }

  createTimeThing(): Thing {
    const thing = new Thing();
    thing.label = 'Time';
    const item = new Item();
    item.type = 'CustomTime';
    item.label = 'time';
    thing.items = [item];
    return thing;
  }

  next(): void {
    this.step++;
  }

  goBack(): void {
    this.step--;
  }
  
  updateRule(): void {
    const ruleDTO = RuleMapperHelper.mapRuleToDTO(this.rule);
    const body = ruleDTO.getJSON();
    this.ruleService.updateRule(body)
      .subscribe(res => res ? this.goToOverview('Rule updated') : this.goToOverview('Rule update failed'),
                 error => this.goToOverview(error));
  }
  
  createRule(): void {
    const ruleDTO = RuleMapperHelper.mapRuleToDTO(this.rule);
    const body = ruleDTO.getJSON();
    this.ruleService.createRule(body)
      .subscribe(res => res ? this.goToOverview('Rule added') : this.goToOverview('Rule creation failed'),
                 error => this.goToOverview(error));
  }

  goToOverview(result: any) {
    this.sharedProperties.setResult(result);
    this.router.navigate(['/overview']);
  }

  isThingArray(arr: Thing[] | Item[]): arr is Thing[] {
    return arr.length > 0 && (<Thing>arr[0]).statusInfo !== undefined;
  }

  isItemArray(arr: Thing[] | Item[]): arr is Item[] {
    return arr.length > 0 && (<Item>arr[0]).link !== undefined;
  }

  cancel(): void {
    this.goToOverview(null);
  }
  
  

  isEventsZero(): boolean {
    return this.rule.events.length === 0;
  }

  isActionsZero(): boolean {
    return this.rule.actions.length === 0;
  }

  onRuleUpdated(mod: RuleModule) {
    RuleHelper.updateModule(mod, this.rule);
    this.test++;
  }
  
  onModDeleted(mod: any) {
    RuleHelper.removeModule(mod, this.rule);
  }
}
