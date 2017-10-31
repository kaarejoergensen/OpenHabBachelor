import { RuleMapperHelper } from '../../helpers/rule-mapper-helper';
import { Rule } from '../../models/rule';
import { RuleDTO } from '../../models/rule-dto';
import { RuleService } from '../../services/rule.service';
import { SharedPropertiesService } from '../../services/shared-properties.service';
import { Component, OnInit, Inject } from '@angular/core';
import { Location } from '@angular/common';
import { MatSnackBar, MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { Router } from '@angular/router';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.css'],
  providers: [RuleService]
})
export class OverviewComponent implements OnInit {
  rules: Rule[];
  isLoading = true;
  constructor(private ruleService: RuleService, private sharedProperties: SharedPropertiesService,
    private location: Location, private router: Router, public snackBar: MatSnackBar, private dialog: MatDialog) { }

  ngOnInit() {
    this.updateRules(true);
  }

  updateRules(handleCreateResult: boolean): void {
    this.isLoading = true;
    this.ruleService.getRules().
      subscribe(res => {
        this.rules = res.map(function(r) {return RuleMapperHelper.mapDTOtoRule(r); });
        this.isLoading = false;
        if (handleCreateResult) {
          this.handleCreateResult(this.sharedProperties.getResult());
        }
      });
  }

  handleCreateResult(result: any) {
    if (result !== undefined && result !== null) {
      if (typeof result === 'string') {
        this.openSnackbar(result);
      } else {
        this.openSnackbar('Failed: ' + (result.statusText ? result.statusText : '') + (result.status ? ' (' + result.status + ')' : ''));
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

  openCreate(rule: Rule) {
    if (rule !== undefined && rule !== null) {
      this.sharedProperties.setRule(rule);
      this.router.navigate(['/create'], {queryParams: {edit: true}});
    } else {
      this.router.navigate(['/create'], {queryParams: {edit: false}});
    }
  }

  deleteRule(rule: any): void {
    const dialogRef = this.dialog.open(DialogDeleteRuleComponent, {
      data: {rule: rule}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.ruleService.deleteRule(result.uid)
          .subscribe(res => {
            if (res) {
              this.openSnackbar('Rule removed');
              const index = this.rules.indexOf(result);
              if (index !== -1) {
                this.rules.splice(index, 1);
              } else {
                this.updateRules(false);
              }
            } else {
              this.openSnackbar('Rule removal failed');
            }
          },
          error => this.openSnackbar('Failed: ' + error.message));
      }});
  }

  enableDisableRule(rule: any): void {
    this.ruleService.enableDisableRule(rule)
      .subscribe(res => rule.enabled = res ? !rule.enabled : rule.enabled);
  }
}

@Component({
  selector: 'app-dialog-delete-rule',
  template: `
<h1 mat-dialog-title>Remove {{data.rule.name}}?</h1>
<div mat-dialog-actions style="float: right">
  <button mat-button (click)="onNoClick()" tabindex="2">Cancel</button>
<button mat-button [mat-dialog-close]="data.rule" tabindex="-1">Remove</button>
</div>`,
})
export class DialogDeleteRuleComponent {
  constructor(
    public dialogRef: MatDialogRef<DialogDeleteRuleComponent>,
      @Inject(MAT_DIALOG_DATA) public data: any) { }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
