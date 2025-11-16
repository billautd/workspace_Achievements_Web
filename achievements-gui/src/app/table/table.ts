import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, inject, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  MatDialog,
  MatDialogConfig,
  MatDialogRef
} from '@angular/material/dialog';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTable, MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ConsoleData, ConsoleSource } from '../../model/consoleData';
import { CompletionStatusType, GameData } from '../../model/gameData';
import { Model } from '../../model/model';
import { GameDataService } from '../../services/game-data-service';
import { LoadingDialog } from '../loading-dialog/loading-dialog';
import { UtilsService } from '../../services/utils-service';
import { MatProgressBarModule } from '@angular/material/progress-bar';

interface FilterData {
  text: string;
  consoles: string[];
  completionStatuses: string[];
  sources: string[];
}

@Component({
  selector: 'app-table',
  imports: [MatTableModule, MatIconModule, MatFormFieldModule, MatSelectModule,
    FormsModule, CommonModule, ReactiveFormsModule, MatInputModule,
    MatSortModule, MatPaginatorModule, MatCheckboxModule, MatProgressBarModule],
  templateUrl: './table.html',
  styleUrl: './table.scss'
})
export class Table {
  @Output() selectTableEntry = new EventEmitter<GameData>();

  dialog = inject(MatDialog);
  formBuilder = inject(FormBuilder);

  //Table data
  @ViewChild(MatTable) table!: MatTable<GameData>;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  data: MatTableDataSource<GameData> = new MatTableDataSource<GameData>();
  columnsToDisplay: string[] = ["ConsoleName", "Title", "CompletionStatus", "Achievements", "Points", "Ratio", "Percentage", "ID", "LinkToData"];
  filterText: string = "";

  selectedConsoles: string[] = [];
  consoles = new FormControl();
  consolesList: string[] = [];

  selectedCompletionStatuses: string[] = [];
  completionStatuses = new FormControl();
  completionStatusesList: string[] = [];

  selectedSources: string[] = [];
  sources = new FormControl();
  sourcesList: string[] = [];
  standalone: string = "Standalone"

  sourcesToRequest = this.formBuilder.group({
    ra: true,
    steam: true,
    ps3: true,
    psvita: true,
    xbox360: true
  });

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
    this.model.getUpdateBehaviorSubject().subscribe(() => {
      this.data.data = this.model.flattenMap();
    })

    //Init filters list
    this.completionStatusesList = Object.values(CompletionStatusType).map(UtilsService.completionStatusText);
    this.sourcesList = [UtilsService.consoleSourceText(ConsoleSource.RETRO_ACHIEVEMENTS),
    UtilsService.consoleSourceText(ConsoleSource.STEAM),
    this.standalone
    ]

    //Setup custom filter predicate on filter string and console
    this.data.filterPredicate = (data: GameData, filter: string): boolean => {
      const searchTerms: FilterData = JSON.parse(filter);


      const text: string = searchTerms.text;
      const consoles: string[] = searchTerms.consoles;
      const completionStatuses: string[] = searchTerms.completionStatuses;
      const sources: string[] = searchTerms.sources;

      //Contains string text
      const strFilter: boolean = (data.ID.toString().trim().toLowerCase().includes(text) ||
        data.Title.trim().toLowerCase().includes(text));
      //Contains filtered consoles
      const consoleFilter: boolean = consoles.includes(data.ConsoleName) || consoles.length == 0;
      //Contains completion status
      const completionStatusesFilter: boolean = completionStatuses.includes(UtilsService.completionStatusText(data.CompletionStatus)) || completionStatuses.length == 0;
      //Is from source
      const consoleData: ConsoleData | undefined = this.model.getConsoleData().get(data.ConsoleID);
      let sourcesFilter: boolean = true;
      if (consoleData) {
        const isStandalone: boolean = (consoleData.Source == ConsoleSource.PS3
          || consoleData.Source == ConsoleSource.PSVITA
          || consoleData.Source == ConsoleSource.XBOX_360) ? sources.includes(this.standalone) : false;
        sourcesFilter = (isStandalone || sources.includes(UtilsService.consoleSourceText(consoleData.Source))) || sources.length == 0;
      }

      return strFilter && consoleFilter && completionStatusesFilter && sourcesFilter;
    };
  }

  ngAfterViewInit() {
    //Init sort and paginator
    this.data.sort = this.sort;
    this.data.paginator = this.paginator;

    //init sorting data accessors
    this.data.sortingDataAccessor = (item, property) => {
      switch (property) {
        case "CompletionStatus":
          return item.CompletionStatus;
        case "ConsoleName":
          return item.ConsoleName;
        case "ID":
          return item.ID;
        case "Achievements":
          return item.MaxPossible;
        case "Percentage":
          return item.Percent;
        case "Points":
          return item.Points;
        case "Ratio":
          return item.Ratio;
        case "Title":
          return item.Title;
        default:
          return item.Title;
      }
    }
  }

  updateSources(): void {
    this.gameDataService.sourcesToRequest = [];
    if (this.sourcesToRequest.controls.ra.value) {
      this.gameDataService.sourcesToRequest.push(ConsoleSource.RETRO_ACHIEVEMENTS);
    }
    if (this.sourcesToRequest.controls.steam.value) {
      this.gameDataService.sourcesToRequest.push(ConsoleSource.STEAM);
    }
    if (this.sourcesToRequest.controls.ps3.value) {
      this.gameDataService.sourcesToRequest.push(ConsoleSource.PS3);
    }
    if (this.sourcesToRequest.controls.psvita.value) {
      this.gameDataService.sourcesToRequest.push(ConsoleSource.PSVITA);
    }
    if (this.sourcesToRequest.controls.xbox360.value) {
      this.gameDataService.sourcesToRequest.push(ConsoleSource.XBOX_360);
    }
    console.log("Sources to request : " + this.gameDataService.sourcesToRequest)
  }

  /**
   * Request games data to back
   * Data will come from websocket games_socket
   */
  requestAllData(): void {
    const dialogRef: MatDialogRef<LoadingDialog> = this.openDialog();

    this.isRequestRunning = true;
    this.gameDataService.requestConsoleData(this.model).then(() => {
      console.log("Console data OK")
      this.updateConsolesList();
      this.gameDataService.requestAllGameData(this.model).then(() => {
        console.log("Game data OK")
        this.isRequestRunning = false;
        dialogRef.close();
      })
    });
  }

  requestAllExistingData(): void {
    this.isRequestRunning = true;
    this.gameDataService.requestConsoleData(this.model).then(() => {
      console.log("Console data OK")
      this.updateConsolesList();
      this.gameDataService.requestExistingData(this.model).then(() => {
        console.log("Existing game data OK")
        this.isRequestRunning = false;
      })
    });
  }

  openDialog(): MatDialogRef<LoadingDialog> {
    const config: MatDialogConfig = new MatDialogConfig();
    config.disableClose = true;
    config.autoFocus = false;
    config.restoreFocus = true;
    config.minHeight = "40vh";
    config.minWidth = "50vw";
    return this.dialog.open(LoadingDialog, config);
  }


  /******************************/
  /* ROWS DISPLAY */
  /******************************/
  totalText(): string {
    return this.data.filteredData.length + " games";
  }

  achievementsText(data: GameData) {
    return data.NumAwardedHardcore + " / " + data.MaxPossible;
  }

  percentageText(data: GameData) {
    return Number(data.Percent).toLocaleString(undefined, { style: 'percent', minimumFractionDigits: 0 });
  }

  openURL(data: GameData): void {
    let url: string;
    let csl: ConsoleData | undefined = this.model.getConsoleData().get(data.ConsoleID);
    if (csl?.Source === ConsoleSource.STEAM) {
      url = "https://store.steampowered.com/app/" + data.ID;
    } else if (csl?.Source === ConsoleSource.PS3 || csl?.Source === ConsoleSource.PSVITA) {
      url = "https://www.psnprofiles.com/trophies/" + data.ID;
    } else if (csl?.Source === ConsoleSource.XBOX_360) {
      url = "https://www.xboxachievements.com/game/" + this.parseXBOXGameName(data.Title) + "/achievements";
    } else if (csl?.Source === ConsoleSource.RETRO_ACHIEVEMENTS) {
      url = "https://retroachievements.org/game/" + data.ID;
    } else {
      console.log("No console source found for game " + data);
      return;
    }
    window.open(url, "_blank");
  }

  selectGameData(data: GameData): void {
    this.selectTableEntry.emit(data);
  }

  parseXBOXGameName(name: string): string {
    return name.replace(/[()™:]/g, "").replace(/ /g, "-").toLowerCase();
  }

  isSocketDone(): boolean {
    return false;
  }

  getGamesNumberText(): string {
    return this.data.data.length + " games";
  }

  /******************************/
  /* SORTING */
  /******************************/
  announceSortChange() {
    this.table.renderRows();
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

  changeSelectedSources(event: MatSelectChange<any>) {
    this.selectedSources = event.value;
    this.applyFilter();
  }

  applyFilter(): void {
    const filter: FilterData = {
      text: this.filterText,
      consoles: this.selectedConsoles,
      completionStatuses: this.selectedCompletionStatuses,
      sources: this.selectedSources
    }
    console.log("Filter is [Text : " + filter.text + "], [Consoles : " + filter.consoles + "], [Statuses : " + filter.completionStatuses + "], [Sources : " + filter.sources + "]")
    //Pass multiple parameters to filterPredicate
    this.data.filter = JSON.stringify(filter);
  }

  clearFilterText(): void {
    this.filterText = '';
    this.applyFilter();
  }

  updateConsolesList(): void {
    this.consolesList = [];
    this.model.getConsoleData().forEach(c => this.consolesList.push(c.Name));
    this.consolesList.sort((o1, o2) => o1.localeCompare(o2))
  }

  completionStatusClass(status: CompletionStatusType): any {
    return UtilsService.completionStatusClass(status);
  }

  completionStatusText(status: CompletionStatusType): string {
    return UtilsService.completionStatusText(status);
  }

  clearFilters(): void {
    this.clearFilterText()

    this.completionStatuses.setValue([])
    this.selectedCompletionStatuses = []

    this.consoles.setValue([])
    this.selectedConsoles = []

    this.sources.setValue([])
    this.selectedSources = []

    this.applyFilter();
  }
}