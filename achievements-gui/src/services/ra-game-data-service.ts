import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { environment } from '../environments/environment';
import { ConsoleData, ConsoleSource } from '../model/consoleData';
import { GameData } from '../model/gameData';
import { Model } from '../model/model';
import { HttpClient } from '@angular/common/http';
import { delay } from './utils-service';

@Injectable({
  providedIn: 'root'
})
export class RAGameDataService {
  RA_PATH: string = "ra/";
  COMPLETION_PROGRESS_METHOD: string = "completion_progress/";
  GAME_DATA_METHOD: string = "game_data/";
  CONSOLE_DATA_METHOD: string = "console_data/";

  /**
   * 
   * @param http : HttpClient
   * @returns Promise for getting all RA console data
   */
  requestRAConsoleData(http: HttpClient): Promise<ConsoleData[]> {
    return firstValueFrom(http.get<ConsoleData[]>(environment.API_URL + this.RA_PATH + this.CONSOLE_DATA_METHOD));
  }

  /**
   * Requests RA game data for each console found
   * Method requestRAConsoleData must be called first
   * 
   * This method :
   * 1) Gets user completion progress to get games with at least 1 achievement unlocked
   * 2) Gets all console games, with games never played
   * 3) Refreshes table data
   * 
   * @param model : Model instance
   * @param http : HttpClient
   * @returns Empty promise for getting all RA game data
   */
  async requestRAGameData(model: Model, http: HttpClient): Promise<any> {
    const processing = (gameData: GameData[]) => {
      for (const game of gameData) {
        const consoleData: ConsoleData | undefined = model.getConsoleData().get(game.ConsoleID);
        if (!consoleData) {
          console.log("No console " + game.ConsoleName + " (" + game.ConsoleID + ") found");
          return;
        }
        //Add data to console data
        consoleData.Games.set(game.ID, game);
      }
      //Force refresh data
      model.refreshTableData(gameData);
    }

    //Get completion progress beforehand
    const completionProgressObs: GameData[] = await firstValueFrom(http.get<GameData[]>(environment.API_URL + this.RA_PATH + this.COMPLETION_PROGRESS_METHOD));
    processing(completionProgressObs);
    console.log("Processed " + completionProgressObs.length + " games from completion progress");

    //Get console games for each RA console
    for (const consoleEntry of model.getConsoleData().entries()) {
      const consoleId: number = consoleEntry[0];
      const consoleData: ConsoleData = consoleEntry[1];
      //Do not parse non RA consoles
      if (consoleData.Source !== ConsoleSource.RETRO_ACHIEVEMENTS) {
        continue;
      }
      //Request games for given console data
      const consoleGames: GameData[] = await firstValueFrom(http.get<GameData[]>(environment.API_URL + this.RA_PATH + this.GAME_DATA_METHOD + consoleId));
      processing(consoleGames);
      console.log("Processed " + consoleGames.length + " games for " + consoleData.Name + " (" + consoleId + ")");
      //Wait to not send too many requests
      await delay(5000);
    }

    return null;
  }
}
