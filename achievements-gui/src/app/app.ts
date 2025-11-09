
import { Component, ViewChild } from '@angular/core';
import { MatTab, MatTabChangeEvent, MatTabGroup, MatTabsModule } from '@angular/material/tabs';
import { Model } from '../model/model';
import { GameDataService } from '../services/game-data-service';
import { Compare } from "./compare/compare";
import { MainData } from './main-data/main-data';
import { Table } from './table/table';
import { GameDataPanel } from "./game-data-panel/game-data-panel";
import { GameData } from '../model/gameData';

@Component({
  selector: 'app-root',
  imports: [Table, MainData, MatTabsModule, Compare, GameDataPanel],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  providers: []
})
export class App {
  @ViewChild(MatTabGroup) tabGroup!: MatTabGroup;

  @ViewChild("mainDataTab") mainDataTab!: MatTab;
  @ViewChild("mainData") mainData!: MainData;

  @ViewChild("gameDataTab") gameDataTab!: MatTab;
  @ViewChild("gameData") gameData!: GameDataPanel;

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

  onSelectTableEntry(data: GameData) {
    this.tabGroup.selectedIndex = 3;
    this.gameDataTab.disabled = false;
    this.gameData.selectGame(data);
  }
}