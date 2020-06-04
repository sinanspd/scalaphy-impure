import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ScalaphyDashboardComponent } from './scalaphy-dashboard/scalaphy-dashboard.component';
import { SchemasComponent } from './schemas/schemas.component';


const routes: Routes = [
  {path: '', component: ScalaphyDashboardComponent },
  {path: 'schemas', component: SchemasComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
