import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { environment } from '../environments/environment';
import { ConsoleSource, ConsoleData } from '../model/consoleData';
import { CompareData } from '../model/compareData';
import { Model } from '../model/model';
import { UtilsService } from './utils-service';
import { GameData } from '../model/gameData';

@Injectable({
  providedIn: 'root'
})
export abstract class AbstractSpecificGameDataService {
  abstract getSource(): ConsoleSource;
  abstract getMainPath(): string;
  abstract requestAllGameData(model: Model, http: HttpClient): Promise<any>;


  CONSOLE_DATA_METHOD: string = "console_data/";
  OWNED_GAMES_METHOD: string = "owned_games/";
  GAME_DATA_METHOD: string = "game_data/";
  FULL_GAME_DATA_METHOD: string = "full_game_data/";
  COMPARE_DATA_METHOD: string = "compare_data/";
  EXISTING_DATA_METHOD: string = "existing_data/";
  WRITE_DATABASE_METHOD: string = "write_database/";

  /**
 * 
 * @param http : HttpClient
 * @returns Promise for getting steam console data
 */
  requestConsoleData(http: HttpClient): Promise<ConsoleData[]> {
    return firstValueFrom(http.get<ConsoleData[]>(environment.API_URL + this.getMainPath() + this.CONSOLE_DATA_METHOD));
  }

  async writeDatabase(http: HttpClient) {
    console.log("Writing " + this.getSource() + " database")
    await firstValueFrom(http.get<any>(environment.API_URL + this.getMainPath() + this.WRITE_DATABASE_METHOD));
  }

  async compareData(model: Model, http: HttpClient): Promise<any> {
    const compareData: CompareData[] = await firstValueFrom(http.get<any>(environment.API_URL + this.getMainPath() + this.COMPARE_DATA_METHOD))
    model.getCompareData().set(this.getSource(), compareData)
    model.refreshCompareData(compareData);
    console.log("Process " + UtilsService.consoleSourceText(this.getSource()) + " " + compareData.length + " compare data");
    return null;
  }

  async requestExistingGameData(model: Model, http: HttpClient): Promise<any> {
    const gameData: GameData[] = await firstValueFrom(http.get<GameData[]>(environment.API_URL + this.getMainPath() + this.EXISTING_DATA_METHOD));
    for (const game of gameData) {
      const consoleData: ConsoleData | undefined = model.getConsoleData().get(game.ConsoleID);
      if (!consoleData) {
        console.log(UtilsService.consoleSourceText(this.getSource()) + " console data not yet set")
        return null;
      }
      //Add data to console data
      consoleData.Games.set(game.ID, game);
    }
    //Force refresh data
    model.refreshData(gameData);
  }

  async requestFullGameData(consoleId: number, gameId: number, model: Model, http: HttpClient): Promise<GameData | null> {
    const fullGameData: GameData = await firstValueFrom(http.get<GameData>(environment.API_URL + this.getMainPath() + this.FULL_GAME_DATA_METHOD + gameId));
    const consoleData: ConsoleData | undefined = model.getConsoleData().get(consoleId);

    if (!consoleData) {
      console.log(this.getSource() + " console data not yet set")
      return null;
    }
    //Add game to console game map
    console.log(fullGameData);
    consoleData.Games.set(fullGameData.ID, fullGameData)
    this.writeDatabase(http);

    //Force refresh data
    model.refreshData([fullGameData]);

    return fullGameData;
  }
}
