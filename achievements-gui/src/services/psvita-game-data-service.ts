import { Injectable } from '@angular/core';
import { ConsoleSource } from '../model/consoleData';
import { PSVITA_CONSOLE_ID } from '../model/model';
import { StandaloneDataService } from './abstract-standalone-game-data-service';
import { GameDataService } from './game-data-service';
import { UtilsService } from './utils-service';

@Injectable({
  providedIn: 'root'
})
export class PSVitaGameDataService extends StandaloneDataService {
  PSVITA_PATH: string = "psvita/";
  GAME_DATA_METHOD: string = "game_data/";
  CONSOLE_DATA_METHOD: string = "console_data/";
  COMPARE_DATA_METHOD: string = "compare_data/";
  EXISTING_DATA_METHOD: string = "existing_data/";

  override getId(): number {
    return PSVITA_CONSOLE_ID;
  }
  override getSource(): ConsoleSource {
    return ConsoleSource.PSVITA;
  }
  override getMainPath(): string {
    return this.PSVITA_PATH;
  }
  override getConsoleDataPath(): string {
    return this.CONSOLE_DATA_METHOD;
  }
  override getGameDataPath(): string {
    return this.GAME_DATA_METHOD;
  }
  override getExistingGameDataPath(): string {
    return this.EXISTING_DATA_METHOD;
  }
  override getCompareDataPath(): string {
    return this.COMPARE_DATA_METHOD;
  }
}
