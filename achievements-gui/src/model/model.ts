import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";
import { CompareData } from "./compareData";
import { ConsoleData, ConsoleSource } from "./consoleData";
import { GameData } from "./gameData";


export const STEAM_CONSOLE_ID:number = 100000;
export const PS3_CONSOLE_ID:number = 200000;
export const PSVITA_CONSOLE_ID:number = 300000;

@Injectable({
  providedIn: 'root'
})
export class Model{
    private consoleData:Map<number, ConsoleData> = new Map();
    private compareData:Map<ConsoleSource, CompareData[]> = new Map();

    private updateBehaviorSubject:BehaviorSubject<any> = new BehaviorSubject(null);
    private compareUpdateBehaviorSubject:BehaviorSubject<any> = new BehaviorSubject(null);

    getConsoleData():Map<number, ConsoleData>{
        return this.consoleData;
    }

    getCompareData():Map<ConsoleSource, CompareData[]>{
      return this.compareData;
    }

    getUpdateBehaviorSubject():BehaviorSubject<GameData[]>{
      return this.updateBehaviorSubject;
    }

    getCompareUpdateBehaviorSubject():BehaviorSubject<CompareData[]>{
      return this.compareUpdateBehaviorSubject;
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
    refreshData(gameData:GameData[]):void{
      this.getUpdateBehaviorSubject().next(gameData);
    }

    /**
     * Refresh compare data source
     * @param compareData Data to refresh
     */
    refreshCompareData(compareData:CompareData[]):void{
      this.getCompareUpdateBehaviorSubject().next(compareData);
    }
}