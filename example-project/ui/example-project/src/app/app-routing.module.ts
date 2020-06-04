import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ScalaphyDashboardComponent } from './scalaphy-dashboard/scalaphy-dashboard.component';


const routes: Routes = [
  {path: '', component: ScalaphyDashboardComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
