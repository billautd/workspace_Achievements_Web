import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { Model } from '../../model/model';
import { GameData } from '../../model/gameData';
import { GameDataService } from '../../services/game-data-service';
import { WebsocketService } from '../../services/websocket-service';
import { HttpClient } from '@angular/common/http';
import { WebSocketSubject } from 'rxjs/webSocket';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-table',
  imports: [MatTableModule],
  templateUrl: './table.html',
  styleUrl: './table.css',
  providers:[Model, GameDataService, WebsocketService]
})
export class Table {
  data:MatTableDataSource<GameData> = new MatTableDataSource<GameData>();
  columnsToDisplay = ["id", "name"];
  
  http:HttpClient = inject(HttpClient);
  webSocketService:WebsocketService;
  model:Model;
  gameDataService:GameDataService;
  gamesWebsocket:WebSocketSubject<any> | undefined;

  constructor(model:Model,
    gameDataService:GameDataService,
    webSocketService:WebsocketService,
  ){
    this.model = model;
    this.gameDataService = gameDataService;
    this.webSocketService = webSocketService;
  }
    
  ngOnInit(){
    this.gamesWebsocket = this.webSocketService.connect("/ra/games_socket");
    this.gamesWebsocket?.subscribe({
      next : (data) => {
        this.gameDataService.readData(data, this.model.getGameListSubject()).subscribe((newData) => {
          //Refresh table
          this.data.data = newData;
        })
      },
      error: (err) => console.log(err),
      complete: () => console.log("Completed")
    })
  }
  
  /**
   * Request games data to back
   * Data will come from websocket games_socket
   */
  requestGamesData():void{
    this.http.get(environment.API_URL + "/ra/all_console_games").subscribe(res => {});
  }
}
