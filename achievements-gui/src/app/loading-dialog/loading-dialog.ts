import { Component } from '@angular/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ConsoleData, ConsoleSource } from '../../model/consoleData';
import { Model, PS3_CONSOLE_ID, PSVITA_CONSOLE_ID, STEAM_CONSOLE_ID, XBOX360_CONSOLE_ID } from '../../model/model';
import { GameDataService } from '../../services/game-data-service';
import { RAGameDataService } from '../../services/ra-game-data-service';
import { SteamGameDataService } from '../../services/steam-game-data-service';
import { Xbox360GameDataService } from '../../services/xbox360-game-data-service';

@Component({
  selector: 'app-loading-dialog',
  imports: [MatProgressBarModule],
  templateUrl: './loading-dialog.html',
  styleUrl: './loading-dialog.scss'
})
export class LoadingDialog {
  progressRA: number = 0;
  progressSteam: number = 0;
  progressPS3: number = 0;
  progressPSVita: number = 0;
  progressXbox360: number = 0;

  model: Model;
  gameDataService: GameDataService;
  raGameDataService: RAGameDataService;
  steamGameDataService: SteamGameDataService;

  constructor(model: Model,
    gameDataService: GameDataService,
    raGameDataService: RAGameDataService,
    steamGameDataService: SteamGameDataService) {
    this.model = model;
    this.gameDataService = gameDataService;
    this.raGameDataService = raGameDataService;
    this.steamGameDataService = steamGameDataService;

    //Update progress map
    this.model.getUpdateBehaviorSubject().subscribe(() => {
      this.updatePS3Progress();
      this.updatePSVitaProgress();
      this.updateXbox360Progress();
      this.updateRAProgress();
      this.updateSteamProgress();
    });
  }

  updateRAProgress(): void {
    let raConsoleNumber: number = 0;
    for (const console of this.model.getConsoleData().values()) {
      if (console.Source == ConsoleSource.RETRO_ACHIEVEMENTS) {
        raConsoleNumber++;
      }
    }
    this.progressRA = 100 * (this.raGameDataService.consoleReqCounter / raConsoleNumber);
  }

  updateSteamProgress(): void {
    const data: ConsoleData | undefined = this.model.getConsoleData().get(STEAM_CONSOLE_ID);
    if (!data) {
      this.progressSteam = 0;
      return;
    }
    this.progressSteam = 100 * (this.steamGameDataService.gameReqCounter / data.Games.size);
  }

  updatePS3Progress(): void {
    const data: ConsoleData | undefined = this.model.getConsoleData().get(PS3_CONSOLE_ID);
    if (!data) {
      this.progressPS3 = 0;
      return;
    }
    this.progressPS3 = data.Games.size > 0 ? 100 : 0;
  }

  updatePSVitaProgress(): void {
    const data: ConsoleData | undefined = this.model.getConsoleData().get(PSVITA_CONSOLE_ID);
    if (!data) {
      this.progressPSVita = 0;
      return;
    }
    this.progressPSVita = data.Games.size > 0 ? 100 : 0;
  }

  updateXbox360Progress(): void {
    const data: ConsoleData | undefined = this.model.getConsoleData().get(XBOX360_CONSOLE_ID);
    if (!data) {
      this.progressXbox360 = 0;
      return;
    }
    this.progressXbox360 = data.Games.size > 0 ? 100 : 0;
  }

  hasRA(): boolean {
    return this.gameDataService.sourcesToRequest.includes(ConsoleSource.RETRO_ACHIEVEMENTS);
  }
  hasSteam(): boolean {
    return this.gameDataService.sourcesToRequest.includes(ConsoleSource.STEAM);
  }
  hasPS3(): boolean {
    return this.gameDataService.sourcesToRequest.includes(ConsoleSource.PS3);
  }
  hasPSVita(): boolean {
    return this.gameDataService.sourcesToRequest.includes(ConsoleSource.PSVITA);
  }
  hasXbox360(): boolean {
    return this.gameDataService.sourcesToRequest.includes(ConsoleSource.XBOX_360);
  }
}
