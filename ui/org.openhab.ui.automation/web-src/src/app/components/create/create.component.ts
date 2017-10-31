import { RuleMapperHelper } from '../../helpers/rule-mapper-helper';
import { Item } from '../../models/item';
import { Rule, Condition, Action } from '../../models/rule';
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
    transition(':enter', [   // :enter is alias to 'void => *'
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
  requiredFormControl = new FormControl('', [
    Validators.required]);
  edit: boolean;
  conditionsComponentData = null;
  actionsComponentData = null;
  newConditionButtonEnabled = false;
  newActionButtonEnabled = false;

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
        this.initializeModules();
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
      this.step = 4;
    } else {
      this.next();
    }
  }
  
  initializeModules(): void {
    if (this.rule.conditions.length > 0) {
      for (const condition of this.rule.conditions) {
        this.createNewConditionComponent(condition);
      }
      this.newConditionButtonEnabled = true;
    } else {
      this.createNewConditionComponent(null);
    }
    if (this.rule.actions.length > 0) {
      for (const action of this.rule.actions) {
        this.createNewActionComponent(action);
      }
      this.newActionButtonEnabled = true;
    } else {
      this.createNewActionComponent(null);
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

  saveNameAndDescription(name: string, desc: string): void {
    this.rule.name = name;
    this.rule.description = desc;
  }
  createRule(): void {
    const ruleDTO = RuleMapperHelper.mapRuleToDTO(this.rule);
    const body = ruleDTO.getJSON();
    console.log(new JsonPipe().transform(body));
    this.ruleService.createRule(body)
      .subscribe(res => this.goToOverview(res),
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

  isConditionsZero(): boolean {
    return this.rule.conditions.length === 0;
  }

  isActionsZero(): boolean {
    return this.rule.actions.length === 0;
  }

  onRuleUpdated(mod: any) {
    if (this.isCondition(mod)) {
      if (mod.id !== null && mod.id !== undefined) {
        const conditions = this.rule.conditions.filter(c => c.id === mod.id);
        if (conditions && conditions.length > 0) {
          const index = this.rule.conditions.indexOf(conditions[0]);
          this.rule.conditions.splice(index, 1);
        }
      } else {
        mod.id = this.getMaxId();
      }
      this.rule.conditions.push(mod);
      this.newConditionButtonEnabled = true;
    } else {
      if (mod.id !== null && mod.id !== undefined) {
        const actions = this.rule.actions.filter(a => a.id === mod.id);
        if (actions && actions.length > 0) {
          const index = this.rule.actions.indexOf(actions[0]);
          this.rule.actions.splice(index, 1);
        }
      } else {
        mod.id = this.getMaxId();
      }
      this.rule.actions.push(mod);
      this.newActionButtonEnabled = true;
    }
  }
  
  onModDeleted(mod: any) {
    if (this.isCondition(mod)) {
      if (mod.id !== null && mod.id !== undefined) {
        const conditions = this.rule.conditions.filter(c => c.id === mod.id);
        if (conditions && conditions.length > 0) {
          const index = this.rule.conditions.indexOf(conditions[0]);
          this.rule.conditions.splice(index, 1);
        }
      }
      if (this.rule.conditions.length === 0) {
        this.newConditionButtonEnabled = false;
      }
    } else {
      if (mod.id !== null && mod.id !== undefined) {
        const actions = this.rule.actions.filter(a => a.id === mod.id);
        if (actions && actions.length > 0) {
          const index = this.rule.actions.indexOf(actions[0]);
          this.rule.actions.splice(index, 1);
        }
      }
    }
    if (this.rule.actions.length === 0) {
      this.newActionButtonEnabled = false;
    }
  }

  isCondition(arr: any): arr is Condition {
    return (<Condition>arr).state !== undefined || (<Condition>arr).days !== undefined;
  }

  getMaxId(): string {
    let maxId = 0;
    for (const c of this.rule.conditions) {
      if (c.id && !isNaN(parseInt(c.id, 10)) && parseInt(c.id, 10) > maxId) {
        maxId = parseInt(c.id, 10);
      }
    }
    for (const a of this.rule.conditions) {
      if (a.id && !isNaN(parseInt(a.id, 10)) && parseInt(a.id, 10) > maxId) {
        maxId = parseInt(a.id, 10);
      }
    }
    return (++maxId).toString();
  }
  
  createNewConditionComponent(mod: Condition): void {
    this.conditionsComponentData = {
      component: ItemsComponent,
      inputs: {
        things: this.things,
        thingType: 'condition',
        mod: mod
      }
    };
    this.newConditionButtonEnabled = false;
  }
  
  createNewActionComponent(mod: Action): void {
    this.actionsComponentData = {
      component: ItemsComponent,
      inputs: {
        things: this.thingsWithEditableItems,
        thingType: 'action',
        mod: mod
      }
    };
    this.newActionButtonEnabled = false;
  }
}
