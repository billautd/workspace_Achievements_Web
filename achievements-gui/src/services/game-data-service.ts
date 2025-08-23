import { inject, Injectable, model } from '@angular/core';
import { GameData } from '../model/gameData';
import { BehaviorSubject, firstValueFrom, forkJoin, Observable, of } from 'rxjs';
import { ConsoleData, ConsoleSource } from '../model/consoleData';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Model, PS3_CONSOLE_ID, PSVITA_CONSOLE_ID } from '../model/model';
import { delay } from './utils-service';

@Injectable({
  providedIn: 'root',
})
export class GameDataService {
  http: HttpClient = inject(HttpClient);

  /**
 * Add data to model
 * @param data Data received from games_socket
 */
  readData(data: GameData[], modelSubject: BehaviorSubject<GameData[]>): Observable<GameData[]> {
    modelSubject.next(data);
    console.log("Read " + data.length + " data")
    return of(modelSubject.getValue());
  }

  async requestConsoleData(model: Model): Promise<any> {
    const processing = (consoleData: ConsoleData[]) => {
      for (const cons of consoleData) {
        //Init map
        cons.Games = new Map();
        //Add console data to model map
        model.getConsoleData().set(cons.ID, cons);
      }
    }

    //All requests are executed simultaneously but method returns when all 4 are done

    //RA console data
    const raObs:Promise<ConsoleData[]>  = firstValueFrom(this.http.get<ConsoleData[]>(environment.API_URL + "/ra/console_data"));
    //Steam console data
    const steamObs: Promise<ConsoleData[]> = firstValueFrom(this.http.get<ConsoleData[]>(environment.API_URL + "/steam/console_data"));
    //PS3 console data
    const ps3Obs: Promise<ConsoleData[]> = firstValueFrom(this.http.get<ConsoleData[]>(environment.API_URL + "/ps3/console_data"));
    //PS vita console data
    const psVitaObs: Promise<ConsoleData[]> = firstValueFrom(this.http.get<ConsoleData[]>(environment.API_URL + "/psvita/console_data"));

    return Promise.all([raObs, steamObs, ps3Obs, psVitaObs]).then((allRes) => {
      allRes.forEach(processing);
    })
  }

  async requestGameData(model: Model): Promise<any> {
    return Promise.all([
      this.requestRAGameData(model),
      this.requestSteamGameData(model),
      this.requestPS3GameData(model),
      this.requestPSVitaGameData(model)
    ]).then(() => { });
  }

   async requestRAGameData(model: Model): Promise<any> {
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
        model.getUpdateBehaviorSubject().next(null);
      }
      
    //Get completion progress beforehand
    const completionProgressObs:GameData[] = await firstValueFrom(this.http.get<GameData[]>(environment.API_URL + "/ra/completion_progress"));
    processing(completionProgressObs);
    console.log("Processed " + completionProgressObs.length + " games from completion progress");

    //Get console games for each RA console
    for (const consoleEntry of model.getConsoleData().entries()) {
      const consoleId:number = consoleEntry[0];
      const consoleData:ConsoleData = consoleEntry[1];
      //Do not parse non RA consoles
      if (consoleData.Source !== ConsoleSource.RETRO_ACHIEVEMENTS) {
        continue;
      }
      const consoleGames:GameData[] = await firstValueFrom(this.http.get<GameData[]>(environment.API_URL + "/ra/game_data/" + consoleId));
      processing(consoleGames);
      console.log("Processed "+ consoleGames.length + " games for " + consoleData.Name + " (" + consoleId + ")");
      await delay(3000);
    }

    return null;
  }

  async requestSteamGameData(model: Model): Promise<any> {
    return null;
  }

  async requestPS3GameData(model: Model): Promise<any> {
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

      //Force refresh data
      model.getUpdateBehaviorSubject().next(null);
    }

    return firstValueFrom(this.http.get<GameData[]>(environment.API_URL + "/ps3/game_data")).then(processing);
  }

  async requestPSVitaGameData(model: Model): Promise<any> {
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
      model.getUpdateBehaviorSubject().next(null);
    }
    return firstValueFrom(this.http.get<GameData[]>(environment.API_URL + "/psvita/game_data")).then(processing);
  }
}
