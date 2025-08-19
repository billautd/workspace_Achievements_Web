import { Component, inject, Input } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { Model } from '../../model/model';
import { CompletionStatusType, GameData } from '../../model/gameData';
import { GameDataService } from '../../services/game-data-service';
import { WebsocketService } from '../../services/websocket-service';
import { HttpClient } from '@angular/common/http';
import { WebSocketSubject } from 'rxjs/webSocket';
import { environment } from '../../environments/environment';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-table',
  imports: [MatTableModule, MatInputModule, MatIconModule, MatFormFieldModule, MatSelectModule, FormsModule, CommonModule],
  templateUrl: './table.html',
  styleUrl: './table.css',
  providers:[Model, GameDataService, WebsocketService]
})
export class Table {
  data:MatTableDataSource<GameData> = new MatTableDataSource<GameData>();
  columnsToDisplay:string[] = ["consoleName", "name", "completionStatus", "achievements", "percentage", "id"];
  consoles:string[] = [];
  
  http:HttpClient = inject(HttpClient);
  webSocketService:WebsocketService;
  model:Model;
  gameDataService:GameDataService;
  gamesWebsocket:WebSocketSubject<any> | undefined;
  isRequestBlocked:boolean = false;
  filterText:string = "";
  filterConsole:string[] = [];


  constructor(model:Model,
    gameDataService:GameDataService,
    webSocketService:WebsocketService
  ){
    this.model = model;
    this.gameDataService = gameDataService;
    this.webSocketService = webSocketService;
  }
    
  ngOnInit(){
    this.gamesWebsocket = this.webSocketService.connect("/games_socket");
  }
  
  /**
   * Request games data to back
   * Data will come from websocket games_socket
   */
  requestGamesData():void{
    this.gamesWebsocket?.subscribe({
      next : (data) => {
        this.gameDataService.readData(data, this.model.getGameListSubject()).subscribe((newData) => {
          //Refresh table
          this.data.data = newData;
          this.getConsoles(newData)
          this.isRequestBlocked = false;
        })
      },
      error: (err) => {
        console.log(err)
        this.isRequestBlocked = false;
      },
      complete: () => {
        console.log("Completed")
        this.isRequestBlocked = false;
      }
    })
    this.http.get(environment.API_URL + "/ra/all_data").subscribe(res => {});
    this.isRequestBlocked = true;
  }

  achievementsText(data:GameData){
    return data.NumAwarded + " / " + data.NumAchievements;
  }

  percentageText(data:GameData){
    let num:number;
    if(data.NumAchievements == 0){
      if(data.CompletionStatus === CompletionStatusType.MASTERED){
        num = 1;
      }
      else if(data.CompletionStatus === CompletionStatusType.BEATEN){
        num = 0.5
      }else{
        num = 0;
      }
    }else{
      num = data.NumAwarded / data.NumAchievements;
    }
    return Number(num).toLocaleString(undefined,{style: 'percent', minimumFractionDigits:0});;
  }

  completionStatusText(data:GameData){
    switch(data.CompletionStatus){
      case CompletionStatusType.MASTERED:
        return "Mastered";
      case CompletionStatusType.BEATEN:
        return "Beaten";
      case CompletionStatusType.CANNOT_PLAY:
        return "Cannot play";
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

  openURL(data:GameData):void{
    let url:string;
    if(data.ConsoleName === "Steam"){
      url = "https://store.steampowered.com/app/" + data.ID;
    }else {
      url = "https://retroachievements.org/game/" + data.ID;
    }
    window.open(url, "_blank");
  }

  applyFilter(){
    const filter:string = this.filterText.trim().toLowerCase();
    this.data.filter = filter;
  }

  getConsoles(data:GameData[]){
    this.consoles = [];
    for(const gameData of data){
      if(!this.consoles.includes(gameData.ConsoleName)){
        this.consoles.push(gameData.ConsoleName)
      }
    }
    this.consoles.sort((a, b) => a.localeCompare(b));
  }
}