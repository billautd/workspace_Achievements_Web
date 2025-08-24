import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { environment } from '../environments/environment';
import { ConsoleData } from '../model/consoleData';
import { GameData } from '../model/gameData';
import { Model, STEAM_CONSOLE_ID } from '../model/model';

@Injectable({
  providedIn: 'root'
})
export class SteamGameDataService {
    STEAM_PATH:string = "steam/";
    CONSOLE_DATA_METHOD:string = "console_data/";
    OWNED_GAMES_METHOD:string = "owned_games/";
    GAME_DATA_METHOD:string = "game_data/";

    /**
     * 
     * @param http : HttpClient
     * @returns Promise for getting steam console data
     */
    requestSteamConsoleData(http:HttpClient):Promise<ConsoleData[]>{
      return firstValueFrom(http.get<ConsoleData[]>(environment.API_URL + this.STEAM_PATH + this.CONSOLE_DATA_METHOD));
    }

    async requestSteamGameData(model: Model, http:HttpClient): Promise<any> {
      const ownedGames: GameData[] = await firstValueFrom(http.get<GameData[]>(environment.API_URL + this.STEAM_PATH + this.OWNED_GAMES_METHOD));
      const consoleData: ConsoleData | undefined = model.getConsoleData().get(STEAM_CONSOLE_ID);
      if (!consoleData) {
        console.log("No Steam console data found");
        return;
      }
      for (const game of ownedGames) {
        consoleData.Games.set(game.ID, game);
      }
      //Force refresh data
      model.refreshData(ownedGames);
  

      for(const gameIndex of consoleData.Games.keys()){
        const gameData:GameData = await firstValueFrom(http.get<GameData>(environment.API_URL + this.STEAM_PATH + this.GAME_DATA_METHOD + gameIndex));
        console.log("Received data for game " + gameData.Title + " (" + gameData.ID + ")")
        consoleData.Games.set(gameIndex, gameData);
        model.refreshData([gameData]);
      }

      return null;
    }
}
