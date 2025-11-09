import { Injectable } from '@angular/core';
import { ConsoleSource } from '../model/consoleData';
import { AbstractStandaloneDataService } from './abstract-standalone-game-data-service';
import { XBOX360_CONSOLE_ID } from '../model/model';

@Injectable({
  providedIn: 'root'
})
export class Xbox360GameDataService extends AbstractStandaloneDataService {
  XBOX360_PATH: string = "xbox360/";

  override getId(): number {
    return XBOX360_CONSOLE_ID;
  }
  override getSource(): ConsoleSource {
    return ConsoleSource.XBOX_360;
  }
  override getMainPath(): string {
    return this.XBOX360_PATH;
  }
}
