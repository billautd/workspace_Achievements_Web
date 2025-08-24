import { inject, Injectable, model } from '@angular/core';
import { CompletionStatusType, GameData } from '../model/gameData';
import { BehaviorSubject, firstValueFrom, forkJoin, Observable, of } from 'rxjs';
import { ConsoleData, ConsoleSource } from '../model/consoleData';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Model, PS3_CONSOLE_ID, PSVITA_CONSOLE_ID, STEAM_CONSOLE_ID } from '../model/model';
import { RAGameDataService } from './ra-game-data-service';
import { SteamGameDataService } from './steam-game-data-service';
import { PSVitaGameDataService } from './psvita-game-data-service';
import { PS3GameDataService } from './ps3-game-data-service';
import { C } from '@angular/cdk/keycodes';

@Injectable({
  providedIn: 'root',
})
export class GameDataService {
  http: HttpClient = inject(HttpClient);
  raDataService: RAGameDataService;
  steamDataService: SteamGameDataService;
  ps3DataService: PS3GameDataService;
  psVitaDataService: PSVitaGameDataService;

  constructor(raDataService: RAGameDataService,
    steamDataService: SteamGameDataService,
    ps3DataService: PS3GameDataService,
    psVitaDataService: PSVitaGameDataService
  ) {
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

  async requestConsoleData(model: Model, sourcesToRequest:ConsoleSource[]): Promise<any> {
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

    console.log("Requesting console data for sources : " + sourcesToRequest);

    return Promise.all([
      sourcesToRequest.includes(ConsoleSource.RETRO_ACHIEVEMENTS) ? raObs : Promise.resolve<ConsoleData[]>([]),
      sourcesToRequest.includes(ConsoleSource.STEAM) ? steamObs :  Promise.resolve<ConsoleData[]>([]),
      sourcesToRequest.includes(ConsoleSource.PS3) ? ps3Obs :  Promise.resolve<ConsoleData[]>([]),
      sourcesToRequest.includes(ConsoleSource.PSVITA) ? psVitaObs :  Promise.resolve<ConsoleData[]>([]),
    ]).then((allRes) => {
      allRes.forEach(processing);
    })
  }

  async requestGameData(model: Model, sourcesToRequest:ConsoleSource[]): Promise<any> {
    console.log("Requesting game data for sources : " + sourcesToRequest);

    return Promise.all([
      sourcesToRequest.includes(ConsoleSource.RETRO_ACHIEVEMENTS) ? this.raDataService.requestRAGameData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      sourcesToRequest.includes(ConsoleSource.STEAM) ? this.steamDataService.requestSteamGameData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      sourcesToRequest.includes(ConsoleSource.PS3) ? this.ps3DataService.requestPS3GameData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
      sourcesToRequest.includes(ConsoleSource.PSVITA) ? this.psVitaDataService.requestPSVitaGameData(model, this.http) : Promise.resolve<ConsoleData[]>([]),
    ]).then((dummy) => { });
  }

  completionStatusText(completionStatus: CompletionStatusType) {
    switch (completionStatus) {
      case CompletionStatusType.MASTERED:
        return "Mastered";
      case CompletionStatusType.BEATEN:
        return "Beaten";
      case CompletionStatusType.NOT_PLAYED:
        return "Not played";
      case CompletionStatusType.NO_ACHIEVEMENTS:
        return "No achievements";
      case CompletionStatusType.TRIED:
        return "Tried";
      default:
        return "No status";
    }
  }

  consoleSourceText(consoleSource: ConsoleSource) {
    switch (consoleSource) {
      case ConsoleSource.PS3:
        return "PS3";
      case ConsoleSource.PSVITA:
        return "PSVITA";
      case ConsoleSource.STEAM:
        return "Steam";
      case ConsoleSource.RETRO_ACHIEVEMENTS:
        return "Retro Achievements";
      default:
        return "No source";
    }
  }
}
