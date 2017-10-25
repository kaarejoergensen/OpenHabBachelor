import { Module } from '../../models/module';
import { Item } from '../../models/item';
import { Thing } from '../../models/thing';
import { ItemService } from '../../services/item.service';
import { RuleService } from '../../services/rule.service';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { ThingService } from '../../services/thing.service';
import { JsonPipe, Location } from '@angular/common';
import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/concat';


@Component({
  selector: 'app-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.css'],
  providers: [ItemService, ThingService, RuleService]
})
export class CreateComponent implements OnInit {
  @ViewChild('bodyJson') jsonChild: ElementRef;
  things: Thing[];
  thingsWithEditableItems: Thing[];
  step = 0;
  ruleName: string;
  ruleDescription: string;
  hidden = true;

  ngOnInit(): void {
    const edit = this.route.snapshot.queryParams['edit'] || undefined;
    if (edit && edit !== 'true') {
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
        this.things = this.addItemsToThings(items, things);
        this.thingsWithEditableItems = this.things.filter(t => t.editableItems && t.editableItems.length > 0);
        this.things.push(this.createTimeThing());
        this.next();
      }
    },
    error => this.handleError(error));
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
    this.ruleName = name;
    this.ruleDescription = desc;
  }
  createRule(): void {
    const body = this.getRuleJson(null);
    console.log(new JsonPipe().transform(body));
    this.ruleService.createRule(body)
      .subscribe(res => this.goToOverview(res),
                 error => this.goToOverview(error));
  }

  goToOverview(result: any) {
    this.sharedProperties.setResult(result);
    this.router.navigate(['/overview']);
  }

  getRuleJson(uid: string): any {
    const body = {
      tags: [],
      conditions: this.sharedProperties.getModuleJSON('condition'),
      description: this.ruleDescription,
      name: this.ruleName,
      triggers: this.sharedProperties.getModuleJSON('trigger'),
      configDescriptions: [],
      actions: this.sharedProperties.getModuleJSON('action'),
    };
    if (uid !== undefined && uid !== null) {
      body['uid'] = uid;
    }
    return body;
  }

  isThingArray(arr: Thing[] | Item[]): arr is Thing[] {
    return arr.length > 0 && (<Thing>arr[0]).statusInfo !== undefined;
  }

  isItemArray(arr: Thing[] | Item[]): arr is Item[] {
    return arr.length > 0 && (<Item>arr[0]).link !== undefined;
  }

  cancel(): void {
    this.location.back();
  }
}
