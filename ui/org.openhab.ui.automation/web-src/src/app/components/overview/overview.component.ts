import { RuleService } from '../../services/rule.service';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.css'],
  providers: [RuleService]
})
export class OverviewComponent implements OnInit {
  rules: any[];
  isLoading = true;
  constructor(private ruleService: RuleService, private sharedProperties: SharedPropertiesService,
    private location: Location) { }

  ngOnInit() {
    this.isLoading = true;
    this.ruleService.getRules().
      subscribe(res => {
        this.rules = res;
        this.isLoading = false;
      });
  }

  goBack(): void {
    this.location.back();
  }
}
