import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { ConsoleData, ConsoleSource } from '../model/consoleData';
import { CompletionStatusType, GameData } from '../model/gameData';
import { Model } from '../model/model';
import { PS3GameDataService } from './ps3-game-data-service';
import { PSVitaGameDataService } from './psvita-game-data-service';
import { RAGameDataService } from './ra-game-data-service';
import { SteamGameDataService } from './steam-game-data-service';
import { Xbox360GameDataService } from './xbox360-game-data-service';

@Injectable({
  providedIn: 'root',
})
export class GameDataService {
  http: HttpClient = inject(HttpClient);
  raDataService: RAGameDataService;
  steamDataService: SteamGameDataService;
  ps3DataService: PS3GameDataService;
  psVitaDataService: PSVitaGameDataService;
  xbox360DataService: Xbox360GameDataService;

  sourcesToRequest: ConsoleSource[] = [
    ConsoleSource.PS3,
    ConsoleSource.PSVITA,
    ConsoleSource.XBOX_360,
    ConsoleSource.RETRO_ACHIEVEMENTS,
    ConsoleSource.STEAM
  ];

  constructor(raDataService: RAGameDataService,
    steamDataService: SteamGameDataService,
    ps3DataService: PS3GameDataService,
    psVitaDataService: PSVitaGameDataService,
    xbox360DataService: Xbox360GameDataService
  ) {
    this.raDataService = raDataService;
    this.steamDataService = steamDataService;
    this.ps3DataService = ps3DataService;
    this.psVitaDataService = psVitaDataService;
    this.xbox360DataService = xbox360DataService;
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
    const ps3Obs: Promise<ConsoleData[]> = this.ps3DataService.requestConsoleData(this.http);
    //PS vita console data
    const psVitaObs: Promise<ConsoleData[]> = this.psVitaDataService.requestConsoleData(this.http);
    //Xbox 360 console data
    const xbox360Obs: Promise<ConsoleData[]> = this.xbox360DataService.requestConsoleData(this.http);

    console.log("Requesting console data for sources : " + this.sourcesToRequest);

    return Promise.all([
      this.sourcesToRequest.includes(ConsoleSource.RETRO_ACHIEVEMENTS) ? raObs : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.STEAM) ? steamObs : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.PS3) ? ps3Obs : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.PSVITA) ? psVitaObs : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.XBOX_360) ? xbox360Obs : Promise.resolve<ConsoleData[]>([])
    ]).then((allRes) => {
      allRes.forEach(processing);
    })
  }

  async requestGameData(model: Model): Promise<any> {
    console.log("Requesting game data for sources : " + this.sourcesToRequest);

    return Promise.all([
      this.sourcesToRequest.includes(ConsoleSource.RETRO_ACHIEVEMENTS) ? this.raDataService.requestRAGameData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.STEAM) ? this.steamDataService.requestSteamGameData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.PS3) ? this.ps3DataService.requestGameData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.PSVITA) ? this.psVitaDataService.requestGameData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.XBOX_360) ? this.xbox360DataService.requestGameData(model, this.http) : Promise.resolve<ConsoleData[]>([])
    ]).then(() => { });
  }

  async requestExistingData(model: Model): Promise<any> {
    console.log("Requesting existing game data for sources : " + this.sourcesToRequest);

    return Promise.all([
      this.sourcesToRequest.includes(ConsoleSource.RETRO_ACHIEVEMENTS) ? this.raDataService.requestRAExistingGameData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.STEAM) ? this.steamDataService.requestSteamExistingGameData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.PS3) ? this.ps3DataService.requestExistingGameData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.PSVITA) ? this.psVitaDataService.requestExistingGameData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.XBOX_360) ? this.xbox360DataService.requestExistingGameData(model, this.http) : Promise.resolve<ConsoleData[]>([])
    ]).then(() => { });
  }

  async requestCompareData(model: Model): Promise<any> {
    console.log("Requesting compare game data for sources : " + this.sourcesToRequest);

    return Promise.all([
      this.sourcesToRequest.includes(ConsoleSource.RETRO_ACHIEVEMENTS) ? this.raDataService.compareData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.STEAM) ? this.steamDataService.compareData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.PS3) ? this.ps3DataService.compareData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.PSVITA) ? this.psVitaDataService.compareData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      this.sourcesToRequest.includes(ConsoleSource.XBOX_360) ? this.xbox360DataService.compareData(model, this.http) : Promise.resolve<ConsoleData[]>([])
    ]).then(() => { });
  }
}
