import { Injectable } from '@angular/core';
import { ConsoleSource } from '../model/consoleData';
import { PSVITA_CONSOLE_ID } from '../model/model';
import { AbstractStandaloneDataService } from './abstract-standalone-game-data-service';

@Injectable({
  providedIn: 'root'
})
export class PSVitaGameDataService extends AbstractStandaloneDataService {
  PSVITA_PATH: string = "psvita/";

  override getId(): number {
    return PSVITA_CONSOLE_ID;
  }
  override getSource(): ConsoleSource {
    return ConsoleSource.PSVITA;
  }
  override getMainPath(): string {
    return this.PSVITA_PATH;
  }
}
