import { RuleService } from '../../services/rule.service';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { MatSnackBar } from '@angular/material';
import { Router } from '@angular/router';

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
    private location: Location, private router: Router, public snackBar: MatSnackBar) { }

  ngOnInit() {
    this.isLoading = true;
    this.ruleService.getRules().
      subscribe(res => {
        this.rules = res;
        this.isLoading = false;
        this.handleCreateResult(this.sharedProperties.getResult());
      });
  }

  handleCreateResult(result: any) {
    if (result !== undefined && result !== null) {
      if (typeof result === 'boolean') {
        if (result) {
          this.openSnackbar('Rule added');
        } else {
          this.openSnackbar('Rule creation failed');
        }
      } else {
        this.openSnackbar('Failed: ' + result.message);
      }
      this.sharedProperties.setResult(null);
    }
  }

  openSnackbar(text: string) {
    this.snackBar.open(text, null, {
      duration: 2000,
      horizontalPosition: 'right',
    });
  }

  openCreate(edit: boolean) {
    this.router.navigate(['/create'], {queryParams: {edit: edit}});
  }
}
