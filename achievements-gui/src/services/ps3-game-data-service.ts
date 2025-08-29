import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { environment } from '../environments/environment';
import { CompareData } from '../model/compareData';
import { ConsoleData, ConsoleSource } from '../model/consoleData';
import { GameData } from '../model/gameData';
import { Model, PS3_CONSOLE_ID } from '../model/model';

@Injectable({
  providedIn: 'root'
})
export class PS3GameDataService {
  PS3_PATH: string = "ps3/";
  GAME_DATA_METHOD: string = "game_data/"
  CONSOLE_DATA_METHOD: string = "console_data/";
  COMPARE_DATA_METHOD: string = "compare_data/";
  EXISTING_DATA_METHOD: string = "existing_data/";

  /**
   * 
   * @param http : HttpClient
   * @returns Promise for getting PS3 console data
   */
  requestPS3ConsoleData(http: HttpClient): Promise<ConsoleData[]> {
    return firstValueFrom(http.get<ConsoleData[]>(environment.API_URL + this.PS3_PATH + this.CONSOLE_DATA_METHOD));
  }

  /** 
     * Requests PS3 game data
     * Method requestPS3ConsoleData must be called first
     * 
     * This method :
     * 1) Gets all console games with their correct completion status
     * 2) Refreshes table
     * 
     * @param model : Model instance
     * @param http : HttpClient
     * @returns Empty promise for getting all PS3 game data
     */
  async requestPS3GameData(model: Model, http: HttpClient): Promise<any> {
    const processing = (gameData: GameData[]) => {
      const consoleData: ConsoleData | undefined = model.getConsoleData().get(PS3_CONSOLE_ID);
      if (!consoleData) {
        console.log("PS3 console data not yet set")
        return;
      }
      //Add game to console game map
      gameData.forEach((game) => {
        consoleData.Games.set(game.ID, game);
      })

      console.log("PS3 game data OK");

      //Force refresh data
      model.refreshData(gameData);
    }
    const consoleGames: GameData[] = await firstValueFrom(http.get<GameData[]>(environment.API_URL + this.PS3_PATH + this.GAME_DATA_METHOD));
    processing(consoleGames);

    //Send request for compare data
    this.compareData(model, http);

    return null;
  }

  async requestPS3ExistingGameData(model: Model, http: HttpClient): Promise<any> {
    const gameData: GameData[] = await firstValueFrom(http.get<GameData[]>(environment.API_URL + this.PS3_PATH + this.EXISTING_DATA_METHOD));
    const consoleData: ConsoleData | undefined = model.getConsoleData().get(PS3_CONSOLE_ID);
    if (!consoleData) {
      console.log("PS3 console data not yet set")
      return null;
    }
    //Add game to console game map
    gameData.forEach((game) => {
      consoleData.Games.set(game.ID, game);
    })

    console.log("PS3 game data OK");

    //Force refresh data
    model.refreshData(gameData);
  }

  async compareData(model:Model, http:HttpClient):Promise<any>{
    const compareData: CompareData[] = await firstValueFrom(http.get<any>(environment.API_URL + this.PS3_PATH + this.COMPARE_DATA_METHOD))
    model.getCompareData().set(ConsoleSource.PS3, compareData)
    model.refreshCompareData(compareData);
    return null;
  }
}
