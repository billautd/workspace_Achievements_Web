import { Component, inject } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { Model } from '../../model/model';
import { CompletionStatusType, GameData } from '../../model/gameData';
import { GameDataService } from '../../services/game-data-service';
import { HttpClient } from '@angular/common/http';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';

interface FilterData {
  text: string;
  consoles: string[];
  completionStatuses: string[];
}

@Component({
  selector: 'app-table',
  imports: [MatTableModule, MatIconModule, MatFormFieldModule, MatSelectModule, FormsModule, CommonModule, ReactiveFormsModule, MatInputModule],
  templateUrl: './table.html',
  styleUrl: './table.css',
  providers: [Model, GameDataService]
})
export class Table {
  //Table data

  data: MatTableDataSource<GameData> = new MatTableDataSource<GameData>();
  columnsToDisplay: string[] = ["consoleName", "name", "completionStatus", "achievements", "percentage", "id"];
  filterText: string = "";

  selectedConsoles: string[] = [];
  consoles = new FormControl();
  consolesList: string[] = [];

  selectedCompletionStatuses: string[] = [];
  completionStatuses = new FormControl();
  completionStatusesList: string[] = [];
  
  model: Model;
  gameDataService: GameDataService;

  http: HttpClient = inject(HttpClient);
  isRequestRunning: boolean = false;

  constructor(model: Model,
    gameDataService: GameDataService) {
    this.model = model;
    this.gameDataService = gameDataService;
  }

  ngOnInit() {
    //No data is passed through this behavior subject, it's only a trigger to refresh table data
    this.model.getUpdateBehaviorSubject().subscribe((gameData) => {
      // if(!gameData){
      //   console.log("No data given for table refresh. Returning...")
      //   return;
      // }
      // console.log("Refreshing table with " + gameData.length + " data");
      this.data.data = this.model.flattenMap();
    })

    //Init completion status list
    this.completionStatusesList = Object.values(CompletionStatusType).map(this.gameDataService.completionStatusText);

    //Setup custom filter predicate on filter string and console
    this.data.filterPredicate = (data: GameData, filter: string): boolean => {
      const searchTerms: FilterData = JSON.parse(filter);

      const text: string = searchTerms.text;
      const consoles: string[] = searchTerms.consoles;
      const completionStatuses:string[] = searchTerms.completionStatuses;

      //Contains string text
      const strFilter: boolean = (data.ID.toString().trim().toLowerCase().includes(text) ||
        data.Title.trim().toLowerCase().includes(text));
      //Contains filtered consoles
      const consoleFilter: boolean = consoles.includes(data.ConsoleName) || consoles.length == 0;
      //Contains completion status
      const completionStatusesFilter: boolean = completionStatuses.includes(this.gameDataService.completionStatusText(data.CompletionStatus)) || completionStatuses.length == 0;

      return strFilter && consoleFilter && completionStatusesFilter;
    };
  }

  /**
   * Request games data to back
   * Data will come from websocket games_socket
   */
  requestAllData(): void {
    this.isRequestRunning = true;
    this.gameDataService.requestConsoleData(this.model).then((dummy1) => {
      console.log("Console data OK")
      this.updateConsolesList();
      this.gameDataService.requestGameData(this.model).then((dummy2) => {
        console.log("Game data OK")
        this.isRequestRunning = false;
      })
    });
  }

  totalText(): string {
    return this.data.filteredData.length + " games";
  }

  achievementsText(data: GameData) {
    return data.NumAwardedHardcore + " / " + data.MaxPossible;
  }

  percentageText(data: GameData) {
    let num: number;
    if (data.NumAwardedHardcore == 0) {
      if (data.CompletionStatus === CompletionStatusType.MASTERED) {
        num = 1;
      } else if (data.CompletionStatus === CompletionStatusType.BEATEN) {
        num = 0.5
      } else {
        num = 0;
      }
    } else {
      num = data.NumAwardedHardcore / data.MaxPossible;
    }
    return Number(num).toLocaleString(undefined, { style: 'percent', minimumFractionDigits: 0 });;
  }

  openURL(data: GameData): void {
    let url: string;
    if (data.ConsoleName === "Steam") {
      url = "https://store.steampowered.com/app/" + data.ID;
    } else if (data.ConsoleName === "PlayStation 3") {
      url = "https://www.exophase.com/platform/psn/?q=" + this.parseGameName(data.Title) + "&sort=updated&platforms=7";
    } else if (data.ConsoleName === "PlayStation Vita") {
      url = "https://www.exophase.com/platform/psn/?q=" + this.parseGameName(data.Title) + "&sort=updated&platforms=6";
    } else {
      url = "https://retroachievements.org/game/" + data.ID;
    }
    window.open(url, "_blank");
  }

  parseGameName(name: string): string {
    return name.replace(/[#&•]/g, "").replace(/ /g, "+").toLowerCase();
  }

  isSocketDone(data: any): boolean {
    return false;
  }

  getGamesNumberText(): string {
    return this.data.data.length + " games";
  }

  /******************************/
  /* FILTERING */
  /******************************/

  changeSelectedConsoles(event: MatSelectChange<any>) {
    this.selectedConsoles = event.value;
    this.applyFilter();
  }

  changeSelectedCompletionStatuses(event: MatSelectChange<any>) {
    this.selectedCompletionStatuses = event.value;
    this.applyFilter();
  }

  applyFilter(): void {
    const filter: FilterData = {
      text: this.filterText,
      consoles: this.selectedConsoles,
      completionStatuses:this.selectedCompletionStatuses,
    }
    console.log("Filter is " + filter)
    //Pass multiple parameters to filterPredicate
    this.data.filter = JSON.stringify(filter);
  }

  clearFilter(): void {
    this.filterText = '';
    this.applyFilter();
  }

  updateConsolesList(): void {
    this.consolesList = [];
    this.model.getConsoleData().forEach(c => this.consolesList.push(c.Name));
  }
}