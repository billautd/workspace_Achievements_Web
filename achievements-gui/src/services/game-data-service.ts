import { inject, Injectable, model } from '@angular/core';
import { GameData } from '../model/gameData';
import { BehaviorSubject, firstValueFrom, forkJoin, Observable, of } from 'rxjs';
import { ConsoleData, ConsoleSource } from '../model/consoleData';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Model, PS3_CONSOLE_ID, PSVITA_CONSOLE_ID, STEAM_CONSOLE_ID } from '../model/model';
import { RAGameDataService } from './ra-game-data-service';
import { SteamGameDataService } from './steam-game-data-service';
import { PSVitaGameDataService } from './psvita-game-data-service';
import { PS3GameDataService } from './ps3-game-data-service';

@Injectable({
  providedIn: 'root',
})
export class GameDataService {
  http: HttpClient = inject(HttpClient);
  raDataService:RAGameDataService;
  steamDataService:SteamGameDataService;
  ps3DataService:PS3GameDataService;
  psVitaDataService:PSVitaGameDataService;

  constructor(raDataService:RAGameDataService,
    steamDataService:SteamGameDataService,
    ps3DataService:PS3GameDataService,
    psVitaDataService:PSVitaGameDataService
  ){
    this.raDataService = raDataService;
    this.steamDataService = steamDataService;
    this.ps3DataService = ps3DataService;
    this.psVitaDataService = psVitaDataService;
  }

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
    const raObs: Promise<ConsoleData[]> = this.raDataService.requestRAConsoleData(this.http);
    //Steam console data
    const steamObs: Promise<ConsoleData[]> = this.steamDataService.requestSteamConsoleData(this.http);
    //PS3 console data
    const ps3Obs: Promise<ConsoleData[]> = this.ps3DataService.requestPS3ConsoleData(this.http);
    //PS vita console data
    const psVitaObs: Promise<ConsoleData[]> = this.psVitaDataService.requestPSVitaConsoleData(this.http);

    return Promise.all([
      // raObs,
      steamObs,
      //ps3Obs,
      //psVitaObs
      ]).then((allRes) => {
      allRes.forEach(processing);
    })
  }

  async requestGameData(model: Model): Promise<any> {
    return Promise.all([
      //this.raDataService.requestRAGameData(model, this.http),
      this.steamDataService.requestSteamGameData(model, this.http),
      //this.ps3DataService.requestPS3GameData(model, this.http),
      //this.psVitaDataService.requestPSVitaGameData(model, this.http)
    ]).then(() => { });
  }
}
