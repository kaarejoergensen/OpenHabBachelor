import {RuleMapperHelperService} from '../../helpers/rule-mapper-helper.service';
import {RuleModel} from '../../models/rule.model';
import {RuleDTO} from '../../models/rule-do.model';
import {ModuleTypeService} from '../../services/module-type.service';
import {RuleService} from '../../services/rule.service';
import {SharedPropertiesService} from '../../services/shared-properties.service';
import {Component, OnInit, Inject} from '@angular/core';
import {Location} from '@angular/common';
import {MatSnackBar, MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';
import {Router} from '@angular/router';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.css'],
  providers: [RuleService, ModuleTypeService]
})
export class OverviewComponent implements OnInit {
  rules: RuleModel[];
  isLoading = true;
  extensionMissing = false;
  loadError = false;
  constructor(private ruleService: RuleService, private sharedProperties: SharedPropertiesService,
    private moduleTypeService: ModuleTypeService, private location: Location, private router: Router,
    public snackBar: MatSnackBar, private dialog: MatDialog) {}
  ngOnInit() {
    this.updateRules(true);
  }

  updateRules(handleCreateResult: boolean): void {
    this.isLoading = true;
    this.ruleService.getRules().
      subscribe(res => {
        this.rules = res.map(function(r) {return RuleMapperHelperService.mapDTOtoRule(r); });
        this.checkIfModuleExtensionExists(handleCreateResult);
      },
      error => {
        if (error && error.status === 404) {
          this.handleMissingExtensions();
        } else {
          this.isLoading = false;
          this.loadError = true;
        }
      });
  }

  checkIfModuleExtensionExists(handleCreateResult: boolean) {
    this.moduleTypeService.getModules().
      subscribe(res => {
        if (res.filter(mt => mt.uid === 'ItemCommandAboveBelowTrigger' || mt.uid === 'BetweenTimesCondition').length === 2) {
          this.isLoading = false;
          if (handleCreateResult) {
            this.handleCreateResult(this.sharedProperties.getResult());
          }
        } else {
          this.handleMissingExtensions();
        }
      },
      error => {
        if (error && error.status === 404) {
          this.handleMissingExtensions();
        } else {
          this.isLoading = false;
          this.loadError = true;
        }
      });
  }

  handleMissingExtensions() {
    this.isLoading = false;
    this.extensionMissing = true;
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

  openCreate(rule: RuleModel) {
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
              this.openSnackbar('Recipe removed');
              const index = this.rules.indexOf(result);
              if (index !== -1) {
                this.rules.splice(index, 1);
              } else {
                this.updateRules(false);
              }
            } else {
              this.openSnackbar('Recipe removal failed');
            }
          },
          error => this.openSnackbar('Recipe removal failed: ' + error.message));
      }
    });
  }

  enableDisableRule(rule: RuleModel): void {
    this.ruleService.enableDisableRule(rule)
      .subscribe(res => {
        if (res) {
          rule.enabled = !rule.enabled;
        } else {
          this.openSnackbar((rule.enabled ? 'Disable' : 'Enable') + ' recipe failed.');
        }
      },
      error => this.openSnackbar((rule.enabled ? 'Disable' : 'Enable') + ' recipe failed.'));
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
    @Inject(MAT_DIALOG_DATA) public data: any) {}

  onNoClick(): void {
    this.dialogRef.close();
  }

}
