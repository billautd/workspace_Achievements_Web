import { Injectable } from "@angular/core";
import { GameData } from "./gameData";
import { ConsoleData } from "./consoleData";
import { BehaviorSubject } from "rxjs";


export const STEAM_CONSOLE_ID:number = 100000;
export const PS3_CONSOLE_ID:number = 200000;
export const PSVITA_CONSOLE_ID:number = 300000;

@Injectable({
  providedIn: 'root'
})
export class Model{
    private consoleData:Map<number, ConsoleData> = new Map();
    private updateBehaviorSubject:BehaviorSubject<any> = new BehaviorSubject(null);

    getConsoleData():Map<number, ConsoleData>{
        return this.consoleData;
    }

    getUpdateBehaviorSubject():BehaviorSubject<GameData[]>{
      return this.updateBehaviorSubject;
    }

    flattenMap():GameData[]{
      const list:GameData[] = [];
      this.consoleData.forEach(c => {
        c.Games.forEach(g => list.push(g))
      })
      return list;
    }

    /**
     * Refresh table data source, while avoiding refreshing the whole table every time
     * @param gameData Data to refresh in table.
     */
    refreshTableData(gameData:GameData[]):void{
      this.getUpdateBehaviorSubject().next(gameData);
    }
}