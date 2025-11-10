import { Component, ViewChild } from '@angular/core';
import { ConsoleData, ConsoleSource } from '../../model/consoleData';
import { Model, PS3_CONSOLE_ID, PSVITA_CONSOLE_ID, STEAM_CONSOLE_ID, XBOX360_CONSOLE_ID } from '../../model/model';
import { GameDataService } from '../../services/game-data-service';
import { ChartCanvas } from '../chart-canvas/chart-canvas';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { UtilsService } from '../../services/utils-service';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@Component({
  selector: 'app-main-data',
  imports: [ChartCanvas, MatFormFieldModule, FormsModule, ReactiveFormsModule, MatSelectModule, MatProgressBarModule],
  templateUrl: './main-data.html',
  styleUrl: './main-data.scss'
})
export class MainData {
  @ViewChild("steamChartCanvas") steamChartCanvas!: ChartCanvas;
  @ViewChild("raChartCanvas") raChartCanvas!: ChartCanvas;
  @ViewChild("standaloneChartCanvas") standaloneChartCanvas!: ChartCanvas;
  @ViewChild("raConsoleChartCanvas") raConsoleChartCanvas!: ChartCanvas;

  raConsoleIds: number[] = [];
  standaloneConsoleIds: number[] = [];
  //To access in HTML
  steamConsoleId: number = STEAM_CONSOLE_ID;


  model: Model;
  gameDataService: GameDataService;


  earnedAchievementsTexts: Map<number, string> = new Map();
  totalAchievementsTexts: Map<number, string> = new Map();
  totalAchievementsPercentages: Map<number, number> = new Map();
  earnedPointsTexts: Map<number, string> = new Map();
  totalPointsTexts: Map<number, string> = new Map();
  totalPointsPercentages: Map<number, number> = new Map();

  raEarnedAchievementsText: string = "";
  raTotalAchievementsText: string = "";
  raAchievementsPercentage: number = 0;
  raEarnedPointsText: string = "";
  raTotalPointsText: string = "";
  raPointsPercentage: number = 0;

  selectedRAConsole: string = "";
  raConsoles = new FormControl();
  raConsolesList: string[] = [];
  selectedRAConsoleId: number = -1;

  selectedStandaloneConsole: string = "";
  standaloneConsoles = new FormControl();
  standaloneConsolesList: string[] = [];
  selectedStandaloneConsoleId: number = 0;

  constructor(model: Model,
    gameDataService: GameDataService
  ) {
    this.model = model;
    this.gameDataService = gameDataService;
  }

  ngOnInit() {
    //No data is passed through this behavior subject, it's only a trigger to refresh data
    this.model.getUpdateBehaviorSubject().subscribe(() => {
      this.updateRAConsoleIds();
      this.updateRAConsolesList();
      this.updateStandaloneConsoleIds();
      this.updateStandaloneConsolesList();

      this.updateAchievementsText(STEAM_CONSOLE_ID);
      this.updateAchievementsText(this.selectedRAConsoleId);
      this.updateAchievementsText(this.selectedStandaloneConsoleId)

      this.updateRATotalAchievementsText(this.raConsoleIds);
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

  updateStandaloneConsoleIds(): void {
    this.standaloneConsoleIds = [PS3_CONSOLE_ID, PSVITA_CONSOLE_ID, XBOX360_CONSOLE_ID]
  }

  updateAchievementsText(consoleId: number): void {
    let totalAchievements = 0;
    let earnedAchievements = 0;
    let totalPoints = 0;
    let earnedPoints = 0;

    const consoleData: ConsoleData | undefined = this.model.getConsoleData().get(consoleId);

    if (!consoleData) {
      return;
    }
    for (const game of consoleData.Games) {
      earnedAchievements += game[1].NumAwardedHardcore;
      totalAchievements += game[1].MaxPossible;
      earnedPoints += game[1].EarnedPoints;
      totalPoints += game[1].Points;
    }
    this.totalAchievementsPercentages.set(consoleId, 100 * earnedAchievements / totalAchievements);
    this.totalPointsPercentages.set(consoleId, 100 * earnedPoints / totalPoints);

    //Texts
    this.earnedAchievementsTexts.set(consoleId, UtilsService.spaceNumber(earnedAchievements));
    this.totalAchievementsTexts.set(consoleId, UtilsService.spaceNumber(totalAchievements));
    this.earnedPointsTexts.set(consoleId, UtilsService.spaceNumber(earnedPoints));
    this.totalPointsTexts.set(consoleId, UtilsService.spaceNumber(totalPoints));
  }

  updateRATotalAchievementsText(ids: number[]): void {
    let totalAchievements = 0;
    let earnedAchievements = 0;
    let totalPoints = 0;
    let earnedPoints = 0;
    for (const console of this.model.getConsoleData()) {
      if (console[1].Source != ConsoleSource.RETRO_ACHIEVEMENTS) {
        continue;
      }
      if (!ids.includes(console[1].ID)) {
        continue;
      }
      for (const game of console[1].Games) {
        earnedAchievements += game[1].NumAwardedHardcore;
        totalAchievements += game[1].MaxPossible;
        earnedPoints += game[1].EarnedPoints;
        totalPoints += game[1].Points;
      }
    }
    this.raAchievementsPercentage = 100 * earnedAchievements / totalAchievements;
    this.raPointsPercentage = 100 * earnedPoints / totalPoints;

    //Texts
    this.raEarnedAchievementsText = UtilsService.spaceNumber(earnedAchievements);
    this.raTotalAchievementsText = UtilsService.spaceNumber(totalAchievements);
    this.raEarnedPointsText = UtilsService.spaceNumber(earnedPoints);
    this.raTotalPointsText = UtilsService.spaceNumber(totalPoints);
  }

  updateRAConsolesList(): void {
    this.raConsolesList = [];
    this.model.getConsoleData().forEach(c => {
      if (c.Source == ConsoleSource.RETRO_ACHIEVEMENTS) {
        this.raConsolesList.push(c.Name);
      }
    })
    this.raConsolesList.sort((o1, o2) => o1.localeCompare(o2))
  }

  updateStandaloneConsolesList(): void {
    this.standaloneConsolesList = [
      UtilsService.consoleSourceText(ConsoleSource.PS3),
      UtilsService.consoleSourceText(ConsoleSource.PSVITA),
      UtilsService.consoleSourceText(ConsoleSource.XBOX_360)
    ];
  }

  changeRASelectedConsole(event: MatSelectChange<any>) {
    this.selectedRAConsole = event.value;
    //Get console associated to name
    for (const c of this.model.getConsoleData().values()) {
      if (c.Source == ConsoleSource.RETRO_ACHIEVEMENTS && c.Name == this.selectedRAConsole) {
        this.selectedRAConsoleId = c.ID;
      }
    }
    this.raConsoleChartCanvas.updateChartData([this.selectedRAConsoleId]);
    this.updateAchievementsText(this.selectedRAConsoleId);
  }

  changeStandaloneSelectedConsole(event: MatSelectChange<any>) {
    this.selectedStandaloneConsole = event.value;
    //Get console associated to name
    for (const c of this.model.getConsoleData().values()) {
      if (c.Name == this.selectedStandaloneConsole) {
        this.selectedStandaloneConsoleId = c.ID;
      }
    }
    this.standaloneChartCanvas.updateChartData([this.selectedStandaloneConsoleId]);
    this.updateAchievementsText(this.selectedStandaloneConsoleId)
  }
}
