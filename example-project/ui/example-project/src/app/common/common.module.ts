import { NgModule } from '@angular/core';
import { CommonModule as CModule } from '@angular/common';
import { MenuComponent } from './menu/menu.component';
import { MatMenuModule } from '@angular/material/menu';
import { OverlayModule } from '@angular/cdk/overlay';
import { RouterModule } from '@angular/router';
import {MatCardModule} from '@angular/material/card';
import {MatInputModule} from '@angular/material/input';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatDividerModule} from '@angular/material/divider';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [MenuComponent],
  imports: [
    CModule,
    MatMenuModule,
    OverlayModule,
    RouterModule,
    MatCardModule,
    MatInputModule, 
    MatProgressBarModule,
    MatDividerModule,
    FormsModule
  ],
  exports: [MenuComponent]
})
export class CommonModule { }
