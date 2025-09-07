
import { Component, ViewChild } from '@angular/core';
import { MatTab, MatTabChangeEvent, MatTabsModule } from '@angular/material/tabs';
import { Model } from '../model/model';
import { GameDataService } from '../services/game-data-service';
import { Compare } from "./compare/compare";
import { MainData } from './main-data/main-data';
import { Table } from './table/table';

@Component({
  selector: 'app-root',
  imports: [Table, MainData, MatTabsModule, Compare],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  providers: []
})
export class App {
  @ViewChild("mainDataTab") mainDataTab!: MatTab;
  @ViewChild("mainData") mainData!: MainData;

  model: Model;
  gameDataService: GameDataService;

  constructor(model: Model,
    gameDataService: GameDataService) {
    this.gameDataService = gameDataService;
    this.model = model;
  }

  onUpdateSelectedTab(event: MatTabChangeEvent): void {
    this.mainData.raChartCanvas.isTabVisible = event.tab === this.mainDataTab;
    this.mainData.standaloneChartCanvas.isTabVisible = event.tab === this.mainDataTab;
    this.mainData.steamChartCanvas.isTabVisible = event.tab === this.mainDataTab;
    this.mainData.raConsoleChartCanvas.isTabVisible = event.tab === this.mainDataTab;

    setTimeout(() => {
      this.mainData.raChartCanvas.updateChart();
      this.mainData.steamChartCanvas.updateChart();
      this.mainData.standaloneChartCanvas.updateChart();
      this.mainData.raConsoleChartCanvas.updateChart();
    }, 1000)
  }

}