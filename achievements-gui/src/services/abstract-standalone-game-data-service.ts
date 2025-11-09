import { HttpClient } from "@angular/common/http";
import { firstValueFrom } from "rxjs";
import { environment } from "../environments/environment";
import { ConsoleData } from "../model/consoleData";
import { GameData } from "../model/gameData";
import { Model } from "../model/model";
import { UtilsService } from "./utils-service";
import { AbstractSpecificGameDataService } from "./abstract-game-data-service";

export abstract class AbstractStandaloneDataService extends AbstractSpecificGameDataService {

    abstract getId(): number;

    /** 
   * Requests all game data
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
    override async requestAllGameData(model: Model, http: HttpClient): Promise<any> {
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
        const consoleGames: GameData[] = await firstValueFrom(http.get<GameData[]>(environment.API_URL + this.getMainPath() + this.GAME_DATA_METHOD));
        processing(consoleGames);

        //Send request for compare data
        this.compareData(model, http);
        this.writeDatabase(http);

        return null;
    }
}