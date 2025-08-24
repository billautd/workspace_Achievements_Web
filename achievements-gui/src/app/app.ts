
import { Component, ViewChild } from '@angular/core';
import { Table } from './table/table';
import { MatTab, MatTabChangeEvent, MatTabsModule } from '@angular/material/tabs';
import { MainData } from './main-data/main-data';

@Component({
  selector: 'app-root',
  imports: [Table, MainData, MatTabsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
  providers:[]
})
export class App {
  @ViewChild("mainDataTab")mainDataTab!:MatTab;
  @ViewChild("mainData")mainData!:MainData;

  onUpdateSelectedTab(event:MatTabChangeEvent):void{
    this.mainData.raChartCanvas.isTabVisible = event.tab === this.mainDataTab;
    this.mainData.psVitaChartCanvas.isTabVisible = event.tab === this.mainDataTab;
    this.mainData.ps3ChartCanvas.isTabVisible = event.tab === this.mainDataTab;
    this.mainData.steamChartCanvas.isTabVisible = event.tab === this.mainDataTab;

    setTimeout(() => {
      this.mainData.raChartCanvas.updateChart();
      this.mainData.steamChartCanvas.updateChart();
      this.mainData.ps3ChartCanvas.updateChart();
      this.mainData.psVitaChartCanvas.updateChart();
    }, 1000)
  }
}

