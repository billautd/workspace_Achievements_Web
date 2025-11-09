import { Injectable } from '@angular/core';
import { ConsoleSource } from '../model/consoleData';
import { PS3_CONSOLE_ID } from '../model/model';
import { AbstractStandaloneDataService } from './abstract-standalone-game-data-service';

@Injectable({
  providedIn: 'root'
})
export class PS3GameDataService extends AbstractStandaloneDataService {
  PS3_PATH: string = "ps3/";

  override getId(): number {
    return PS3_CONSOLE_ID;
  }
  override getSource(): ConsoleSource {
    return ConsoleSource.PS3;
  }
  override getMainPath(): string {
    return this.PS3_PATH;
  }
}
