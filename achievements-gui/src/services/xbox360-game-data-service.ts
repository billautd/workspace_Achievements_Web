import { Injectable } from '@angular/core';
import { ConsoleSource } from '../model/consoleData';
import { StandaloneDataService } from './abstract-standalone-game-data-service';
import { GameDataService } from './game-data-service';
import { XBOX360_CONSOLE_ID } from '../model/model';
import { UtilsService } from './utils-service';

@Injectable({
  providedIn: 'root'
})
export class Xbox360GameDataService extends StandaloneDataService {
  XBOX360_PATH: string = "xbox360/";
  GAME_DATA_METHOD: string = "game_data/"
  CONSOLE_DATA_METHOD: string = "console_data/";
  COMPARE_DATA_METHOD: string = "compare_data/";
  EXISTING_DATA_METHOD: string = "existing_data/";

  override getId(): number {
    return XBOX360_CONSOLE_ID;
  }
  override getSource(): ConsoleSource {
    return ConsoleSource.XBOX_360;
  }
  override getMainPath(): string {
    return this.XBOX360_PATH;
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
