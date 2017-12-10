import { RuleHelperService } from '../../helpers/rule-helper.service';
import { RuleMapperHelperService } from '../../helpers/rule-mapper-helper.service';
import { ItemModel } from '../../models/item.model';
import { RuleModel, RuleModelModule, EVENT_TYPE, CONDITION_TYPE, ACTION_TYPE } from '../../models/rule.model';
import { ThingModel } from '../../models/thing.model';
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
  things: ThingModel[];
  thingsWithEditableItems: ThingModel[];
  step = 0;
  rule: RuleModel;
  requiredFormControl = new FormControl('', [Validators.required]);
  edit: boolean;
  creatingRule = false;
  
  ngOnInit(): void {
    this.rule = new RuleModel();
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
    let items: ItemModel[];
    let things: ThingModel[];
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
      if (things && things.length > 0 && items && items.length > 0) {
        this.initializeThingsAndItems(things, items);
      } else if (things && items) {
        this.step = -1;
      }
    },
    error => this.step = -2);
  }
  
  initializeThingsAndItems(things: ThingModel[], items: ItemModel[]): void {
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
    
  addItemsToThings(items: ItemModel[], things: ThingModel[]): ThingModel[] {
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
    const modules = this.rule.events.concat(this.rule.actions, this.rule.conditions);
    for (const mod of modules) {
      if (mod.unsupportedModule) {
        console.log('addThingToRule mod unsupported, not adding thing: ' + mod.id + ' ' + mod.label);
        continue;
      }
      if (!mod.itemName) {
        console.log('addThingToRule mod itemName null, adding CustomTime: ' + JSON.stringify(mod));
      } 
      const things = this.things.filter(t => t.items.filter(i => i.name === mod.itemName).length > 0);
      if (things.length > 0) {
        mod.thing = things[0];
      } else {
        console.log('no thing found for module ' + JSON.stringify(mod));
      }
    }
  }

  createTimeThing(): ThingModel {
    const thing = new ThingModel();
    thing.label = 'Time';
    const item = new ItemModel();
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
    this.creatingRule = true;
    const ruleDTO = RuleMapperHelperService.mapRuleToDTO(this.rule);
    const body = ruleDTO.getJSON();
    this.ruleService.updateRule(body)
      .subscribe(res => res ? this.goToOverview('Recipe updated') : this.goToOverview('Recipe update failed'),
                 error => this.goToOverview(error));
  }
  
  createRule(): void {
    this.creatingRule = true;
    const ruleDTO = RuleMapperHelperService.mapRuleToDTO(this.rule);
    const body = ruleDTO.getJSON();
    this.ruleService.createRule(body)
      .subscribe(res => res ? this.goToOverview('Recipe added') : this.goToOverview('Recipe creation failed'),
                 error => this.goToOverview(error));
  }

  goToOverview(result: any) {
    this.creatingRule = false;
    this.sharedProperties.setResult(result);
    this.router.navigate(['/overview']);
  }

  isThingArray(arr: ThingModel[] | ItemModel[]): arr is ThingModel[] {
    return arr.length > 0 && (<ThingModel>arr[0]).statusInfo !== undefined;
  }

  isItemArray(arr: ThingModel[] | ItemModel[]): arr is ItemModel[] {
    return arr.length > 0 && (<ItemModel>arr[0]).link !== undefined;
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

  onRuleUpdated(mod: RuleModelModule) {
    RuleHelperService.updateModule(mod, this.rule);
  }
  
  onModDeleted(mod: any) {
    RuleHelperService.removeModule(mod, this.rule);
  }
}
