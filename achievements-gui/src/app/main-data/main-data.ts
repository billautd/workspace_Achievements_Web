import { Component, ViewChild } from '@angular/core';
import { ConsoleData, ConsoleSource } from '../../model/consoleData';
import { Model, PS3_CONSOLE_ID, PSVITA_CONSOLE_ID, STEAM_CONSOLE_ID } from '../../model/model';
import { GameDataService } from '../../services/game-data-service';
import { ChartCanvas } from '../chart-canvas/chart-canvas';

@Component({
  selector: 'app-main-data',
  imports: [ChartCanvas],
  templateUrl: './main-data.html',
  styleUrl: './main-data.scss'
})
export class MainData {
  @ViewChild("steamChartCanvas") steamChartCanvas!: ChartCanvas;
  @ViewChild("raChartCanvas") raChartCanvas!: ChartCanvas;
  @ViewChild("ps3ChartCanvas") ps3ChartCanvas!: ChartCanvas;
  @ViewChild("psVitaChartCanvas") psVitaChartCanvas!: ChartCanvas;
  raConsoleIds: number[] = [];
  steamConsoleId: number = STEAM_CONSOLE_ID;
  ps3ConsoleId: number = PS3_CONSOLE_ID;
  psVitaConsoleId: number = PSVITA_CONSOLE_ID;

  model: Model;
  gameDataService: GameDataService;

  steamAchievementsText: string = "";
  steamAchievementsPercentageText: string = "";

  raAchievementsText: string = "";
  raAchievementsPercentageText: string = "";

  constructor(model: Model,
    gameDataService: GameDataService
  ) {
    this.model = model;
    this.gameDataService = gameDataService;
  }

  ngOnInit() {
    //No data is passed through this behavior subject, it's only a trigger to refresh data
    this.model.getUpdateBehaviorSubject().subscribe(() => {
      this.updateSteamAchievementsText();
      this.updateRAAchievementsText();
      this.updateRAConsoleIds();
    })
  }

  updateRAConsoleIds(): void {
    const ids: number[] = [];
    for (const console of this.model.getConsoleData()) {
      if (console[1].Source == ConsoleSource.RETRO_ACHIEVEMENTS) {
        ids.push(console[0])
      }
    }
    this.raConsoleIds = ids;
  }

  updateSteamAchievementsText(): void {
    //Get total and earned
    let earned: number = 0;
    let total: number = 0;
    const steamData: ConsoleData | undefined = this.model.getConsoleData().get(STEAM_CONSOLE_ID);
    if (!steamData) {
      this.steamAchievementsText = "- / -";
      this.steamAchievementsPercentageText = "- %";
      return;
    }
    for (const game of steamData.Games) {
      earned += game[1].NumAwardedHardcore;
      total += game[1].MaxPossible;
    }
    //Update achivements number text
    this.steamAchievementsText = earned + " / " + total;

    //Update achivements percentage text
    if (total == 0) {
      this.steamAchievementsPercentageText = "- %";
    } else {
      const num: number = earned / total;
      this.steamAchievementsPercentageText = Number(num).toLocaleString(undefined, { style: 'percent', minimumFractionDigits: 0 });;
    }
  }

  updateRAAchievementsText(): void {
    //Get total and earned
    let earned: number = 0;
    let total: number = 0;
    for (const console of this.model.getConsoleData()) {
      if (console[1].Source != ConsoleSource.RETRO_ACHIEVEMENTS) {
        continue;
      }
      for (const game of console[1].Games) {
        earned += game[1].NumAwardedHardcore;
        total += game[1].MaxPossible;
      }
    }
    //Update achivements number text
    this.raAchievementsText = earned + " / " + total;

    //Update achivements percentage text
    if (total == 0) {
      this.raAchievementsPercentageText = "- %";
    } else {
      const num: number = earned / total;
      this.raAchievementsPercentageText = Number(num).toLocaleString(undefined, { style: 'percent', minimumFractionDigits: 0 });;
    }
  }
}
