import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-schemas',
  templateUrl: './schemas.component.html',
  styleUrls: ['./schemas.component.css']
})
export class SchemasComponent implements OnInit {

  schemas = [
    {name: "Movie", route: "/movies"},
    {name: "Review", route: "reviews"}
  ]

  selected = this.schemas[0].name
  
  constructor() { }

  ngOnInit(): void {
  }

}
