import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { environment } from '../environments/environment';
import { ConsoleData } from '../model/consoleData';
import { GameData } from '../model/gameData';
import { Model, PSVITA_CONSOLE_ID } from '../model/model';

@Injectable({
  providedIn: 'root'
})
export class PSVitaGameDataService {
  PSVITA_PATH: string = "psvita/";
  CONSOLE_DATA_METHOD: string = "console_data/";

  /**
   * 
   * @param http : HttpClient
   * @returns Promise for getting PSVita console data
   */
  requestPSVitaConsoleData(http: HttpClient): Promise<ConsoleData[]> {
    return firstValueFrom(http.get<ConsoleData[]>(environment.API_URL + this.PSVITA_PATH + this.CONSOLE_DATA_METHOD));
  }

  
  /** 
     * Requests PSVita game data
     * Method requestPSVitaConsoleData must be called first
     * 
     * This method :
     * 1) Gets all console games with their correct completion status
     * 2) Refreshes table
     * 
     * @param model : Model instance
     * @param http : HttpClient
     * @returns Empty promise for getting all PSVita game data
     */
  async requestPSVitaGameData(model: Model, http:HttpClient): Promise<any> {
    const processing = (gameData: GameData[]) => {
      const consoleData: ConsoleData | undefined = model.getConsoleData().get(PSVITA_CONSOLE_ID);
      if (!consoleData) {
        console.log("PSVita console data not yet set")
        return;
      }
      //Add game to console game map
      gameData.forEach((game) => {
        consoleData.Games.set(game.ID, game);
      })

      //Force refresh data
      model.refreshTableData(gameData);
    }
    return firstValueFrom(http.get<GameData[]>(environment.API_URL + "/psvita/game_data")).then(processing);
  }
}
