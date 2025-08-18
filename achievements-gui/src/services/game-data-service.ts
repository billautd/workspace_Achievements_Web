import { Injectable, model } from '@angular/core';
import { GameData } from '../model/gameData';
import { BehaviorSubject, Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GameDataService {
    /**
   * Add data to model
   * @param data Data received from games_socket
   */
  readData(data:GameData[], modelSubject:BehaviorSubject<GameData[]>):Observable<GameData[]>{
    const gameDataArray:GameData[] = [];
    for(const game of data){
      gameDataArray.push({ id:game["id"], name:game["name"]});
    }
    modelSubject.next(gameDataArray);
    console.log("Read " + data.length + " data")
    return of(modelSubject.getValue());
  }
}
