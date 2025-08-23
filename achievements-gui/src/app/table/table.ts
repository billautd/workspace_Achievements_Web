import { Component, inject, Input } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { Model } from '../../model/model';
import { CompletionStatusType, GameData } from '../../model/gameData';
import { GameDataService } from '../../services/game-data-service';
import { HttpClient } from '@angular/common/http';
import { WebSocketSubject } from 'rxjs/webSocket';
import { environment } from '../../environments/environment';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatSelectModule } from '@angular/material/select';
import { ConsoleData } from '../../model/consoleData';

@Component({
  selector: 'app-table',
  imports: [MatTableModule, MatInputModule, MatIconModule, MatFormFieldModule, MatSelectModule, FormsModule, CommonModule],
  templateUrl: './table.html',
  styleUrl: './table.css',
  providers:[Model, GameDataService]
})
export class Table {
  data:MatTableDataSource<GameData> = new MatTableDataSource<GameData>();
  columnsToDisplay:string[] = ["consoleName", "name", "completionStatus", "achievements", "percentage", "id"];
  consoles:string[] = [];
  
  http:HttpClient = inject(HttpClient);
  model:Model;
  gameDataService:GameDataService;
  filterText:string = "";
  filterConsole:string[] = [];

  constructor(model:Model,
    gameDataService:GameDataService){
    this.model = model;
    this.gameDataService = gameDataService;
  }

  ngOnInit(){
    //No data is passed through this behavior subject, it's only a trigger to refresh table data
    this.model.getUpdateBehaviorSubject().subscribe((dummy) => {
      this.data.data = this.model.flattenMap();
    })
  }
  
  /**
   * Request games data to back
   * Data will come from websocket games_socket
   */
  requestAllData():void{
    this.gameDataService.requestConsoleData(this.model).then((res1) => {
      console.log("Console data OK")
      this.gameDataService.requestGameData(this.model).then((res2) => {
        console.log("Game data OK")
      })
    });
  }


  achievementsText(data:GameData){
    return data.NumAwardedHardcore + " / " + data.MaxPossible;
  }

  percentageText(data:GameData){
    let num:number;
    if(data.NumAwardedHardcore== 0){
      if(data.CompletionStatus === CompletionStatusType.MASTERED){
        num = 1;
      }else if(data.CompletionStatus === CompletionStatusType.BEATEN){
        num = 0.5
      }else{
        num = 0;
      }
    }else{
      num = data.NumAwardedHardcore / data.MaxPossible;
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
    }else if(data.ConsoleName === "PlayStation 3"){
      url = "https://www.exophase.com/platform/psn/?q=" + this.parseGameName(data.Title) + "&sort=updated&platforms=7";
    }else if(data.ConsoleName === "PlayStation Vita"){
      url = "https://www.exophase.com/platform/psn/?q=" + this.parseGameName(data.Title) + "&sort=updated&platforms=6";
    }else {
      url = "https://retroachievements.org/game/" + data.ID;
    }
    window.open(url, "_blank");
  }

  parseGameName(name:string):string{
    return name.replace(/[#&•]/g, "").replace(/ /g, "+").toLowerCase();
  }

  applyFilter(){
    const filter:string = this.filterText.trim().toLowerCase();
    this.data.filter = filter;
  }

  isSocketDone(data:any):boolean{
    return false;
  }

  getGamesNumberText():string{
    return this.data.data.length + " games";
  }
}