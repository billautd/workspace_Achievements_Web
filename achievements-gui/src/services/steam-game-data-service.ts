import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { environment } from '../environments/environment';
import { ConsoleData, ConsoleSource } from '../model/consoleData';
import { GameData } from '../model/gameData';
import { Model, STEAM_CONSOLE_ID } from '../model/model';
import { AbstractSpecificGameDataService } from './abstract-game-data-service';

@Injectable({
  providedIn: 'root'
})
export class SteamGameDataService extends AbstractSpecificGameDataService {
  STEAM_PATH: string = "steam/";

  gameReqCounter: number = 0;

  override getSource(): ConsoleSource {
    return ConsoleSource.STEAM;
  }
  override getMainPath(): string {
    return this.STEAM_PATH;
  }

  override async requestAllGameData(model: Model, http: HttpClient): Promise<any> {
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

    //Reset counter
    this.gameReqCounter = 0;
    for (const gameIndex of consoleData.Games.keys()) {
      const gameData: GameData = await firstValueFrom(http.get<GameData>(environment.API_URL + this.STEAM_PATH + this.GAME_DATA_METHOD + gameIndex));
      console.log("Received data for game " + gameData.Title + " (" + gameData.ID + ")")
      consoleData.Games.set(gameIndex, gameData);
      this.gameReqCounter++;
      model.refreshData([gameData]);
    }

    //Send request for compare data
    this.compareData(model, http);
    this.writeDatabase(http);
    return null;
  }
}
