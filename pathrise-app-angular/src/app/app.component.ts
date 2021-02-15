import { Component, OnInit } from '@angular/core';
import * as $ from 'jquery/dist/jquery.min.js';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'pathrise-app-angular';

  ngOnInit() {
    $.ajax({
      type: "GET",
      url: "../assets/job_opportunities.csv",
      dataType: "text",
      success: this.processCSV
    });
  }

  processCSV(data: string) {
    let records = data.split("\n");
    console.log(records);
  }
}
