import { Component, ViewChild } from '@angular/core';
import { ConsoleData, ConsoleSource } from '../../model/consoleData';
import { Model, PS3_CONSOLE_ID, PSVITA_CONSOLE_ID, STEAM_CONSOLE_ID, XBOX360_CONSOLE_ID } from '../../model/model';
import { GameDataService } from '../../services/game-data-service';
import { ChartCanvas } from '../chart-canvas/chart-canvas';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { UtilsService } from '../../services/utils-service';

@Component({
  selector: 'app-main-data',
  imports: [ChartCanvas, MatFormFieldModule, FormsModule, ReactiveFormsModule, MatSelectModule],
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
  steamConsoleId: number = STEAM_CONSOLE_ID;


  model: Model;
  gameDataService: GameDataService;

  steamAchievementsText: string = "";
  steamAchievementsPercentageText: string = "";

  raAchievementsText: string = "";
  raAchievementsPercentageText: string = "";

  raConsoleAchievementsText: string = "";
  raConsoleAchievementsPercentageText: string = "";

  selectedRAConsole: string = "";
  raConsoles = new FormControl();
  raConsolesList: string[] = [];
  selectedRAConsoleId: number = 0;

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

      this.updateSteamAchievementsText();
      this.updateRAAchievementsText(this.raConsoleIds);
      this.updateRAAchievementsText([this.selectedRAConsoleId]);
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

  updateRAAchievementsText(ids: number[]): void {
    //Get total and earned
    let earned: number = 0;
    let total: number = 0;
    for (const console of this.model.getConsoleData()) {
      if (console[1].Source != ConsoleSource.RETRO_ACHIEVEMENTS) {
        continue;
      }
      if (!ids.includes(console[1].ID)) {
        continue;
      }
      for (const game of console[1].Games) {
        earned += game[1].NumAwardedHardcore;
        total += game[1].MaxPossible;
      }
    }
    //Update achivements number text
    const totalText: string = earned + " / " + total;
    if (ids.length > 1) {
      this.raAchievementsText = totalText;
    } else if (ids.length == 1) {
      this.raConsoleAchievementsText = totalText
    }

    //Update achivements percentage text
    let percentageText: string;
    if (total == 0) {
      percentageText = "- %";
    } else {
      const num: number = earned / total;
      percentageText = Number(num).toLocaleString(undefined, { style: 'percent', minimumFractionDigits: 0 });;
    }
    if (ids.length > 1) {
      this.raAchievementsPercentageText = percentageText;
    } else if (ids.length == 1) {
      this.raConsoleAchievementsPercentageText = percentageText;
    }
  }

  updateRAConsolesList(): void {
    this.raConsolesList = [];
    this.model.getConsoleData().forEach(c => {
      if (c.Source == ConsoleSource.RETRO_ACHIEVEMENTS) {
        this.raConsolesList.push(c.Name);
      }
    })
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
    this.raChartCanvas.updateChartData([this.selectedRAConsoleId]);
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
  }
}
