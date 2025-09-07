import { HttpClient } from "@angular/common/http";
import { firstValueFrom } from "rxjs";
import { environment } from "../environments/environment";
import { ConsoleData, ConsoleSource } from "../model/consoleData";
import { GameData } from "../model/gameData";
import { Model } from "../model/model";
import { CompareData } from "../model/compareData";
import { UtilsService } from "./utils-service";

export abstract class StandaloneDataService {

    abstract getId(): number;
    abstract getSource(): ConsoleSource;
    abstract getMainPath(): string;
    abstract getConsoleDataPath(): string;
    abstract getGameDataPath(): string;
    abstract getExistingGameDataPath(): string;
    abstract getCompareDataPath(): string;
    /**
 * 
 * @param http : HttpClient
 * @returns Promise for getting PS3 console data
 */
    requestConsoleData(http: HttpClient): Promise<ConsoleData[]> {
        return firstValueFrom(http.get<ConsoleData[]>(environment.API_URL + this.getMainPath() + this.getConsoleDataPath()));

    }

    /** 
   * Requests game data
   * Method requestConsoleData must be called first
   * 
   * This method :
   * 1) Gets all console games with their correct completion status
   * 2) Refreshes table
   * 
   * @param model : Model instance
   * @param http : HttpClient
   * @returns Empty promise for getting all game data
   */
    async requestGameData(model: Model, http: HttpClient): Promise<any> {
        const processing = (gameData: GameData[]) => {
            const consoleData: ConsoleData | undefined = model.getConsoleData().get(this.getId());
            if (!consoleData) {
                console.log(UtilsService.consoleSourceText(this.getSource()) + "console data not yet set")
                return;
            }
            //Add game to console game map
            gameData.forEach((game) => {
                consoleData.Games.set(game.ID, game);
            })

            console.log(UtilsService.consoleSourceText(this.getSource()) + " game data OK");

            //Force refresh data
            model.refreshData(gameData);
        }
        const consoleGames: GameData[] = await firstValueFrom(http.get<GameData[]>(environment.API_URL + this.getMainPath() + this.getGameDataPath()));
        processing(consoleGames);

        //Send request for compare data
        this.compareData(model, http);

        return null;
    }

    async requestExistingGameData(model: Model, http: HttpClient): Promise<any> {
        const gameData: GameData[] = await firstValueFrom(http.get<GameData[]>(environment.API_URL + this.getMainPath() + this.getExistingGameDataPath()));
        const consoleData: ConsoleData | undefined = model.getConsoleData().get(this.getId());
        if (!consoleData) {
            console.log(UtilsService.consoleSourceText(this.getSource()) + " console data not yet set")
            return null;
        }
        //Add game to console game map
        gameData.forEach((game) => {
            consoleData.Games.set(game.ID, game);
        })

        console.log(UtilsService.consoleSourceText(this.getSource()) + " game data OK");

        //Force refresh data
        model.refreshData(gameData);
    }

    async compareData(model: Model, http: HttpClient): Promise<any> {
        const compareData: CompareData[] = await firstValueFrom(http.get<any>(environment.API_URL + this.getMainPath() + this.getCompareDataPath()))
        model.getCompareData().set(this.getSource(), compareData)
        model.refreshCompareData(compareData);
        console.log("Process " + UtilsService.consoleSourceText(this.getSource()) + " " + compareData.length + " compare data");
        return null;
    }
}