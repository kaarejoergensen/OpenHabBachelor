import { RuleHelperService } from '../../helpers/rule-helper.service';
import { RuleMapperHelperService } from '../../helpers/rule-mapper-helper.service';
import { ThingItemMapperHelperService } from '../../helpers/thing-item-mapper-helper.service';
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
    this.things = this.sharedProperties.getThings();
    this.thingsWithEditableItems = this.things.filter(t => t.editableItems && t.editableItems.length > 0);
    const editString = this.route.snapshot.queryParams['edit'] || undefined;
    if (editString && editString === 'true'
        && this.sharedProperties.getRule() !== undefined && this.sharedProperties.getRule() !== null) {
      this.edit = true;
      this.rule = this.sharedProperties.getRule();
      ThingItemMapperHelperService.addThingToRule(this.rule, this.things);
      this.step = 5;
    } else {
      this.edit = false;
      this.sharedProperties.reset();
      this.next();
    }
  }
  constructor(private itemService: ItemService, private sharedProperties: SharedPropertiesService,
  private thingService: ThingService, private ruleService: RuleService, private location: Location,
  private route: ActivatedRoute, private router: Router) { }

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
