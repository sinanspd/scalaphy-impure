import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { GraphQLModule } from './graphql.module';
import { HttpClientModule } from '@angular/common/http';
import { CommonModule as CModule} from './common/common.module';
import { ScalaphyDashboardComponent } from './scalaphy-dashboard/scalaphy-dashboard.component';
import {MatIconModule} from '@angular/material/icon';
import { SchemasComponent } from './schemas/schemas.component';

@NgModule({
  declarations: [
    AppComponent,
    ScalaphyDashboardComponent,
    SchemasComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    GraphQLModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatIconModule,
    CModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
