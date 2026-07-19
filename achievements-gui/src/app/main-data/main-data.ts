import { Component, ViewChild } from '@angular/core';
import { ConsoleData, ConsoleSource } from '../../model/consoleData';
import { Model, PS3_CONSOLE_ID, PSVITA_CONSOLE_ID, STEAM_CONSOLE_ID, XBOX360_CONSOLE_ID } from '../../model/model';
import { GameDataService } from '../../services/game-data-service';
import { ChartCanvas } from '../chart-canvas/chart-canvas';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { UtilsService } from '../../services/utils-service';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTable, MatTableDataSource, MatTableModule } from '@angular/material/table';
import { CompletionStatusType, GameData } from '../../model/gameData';
import { MatSort, MatSortModule } from '@angular/material/sort';

@Component({
  selector: 'app-main-data',
  imports: [MatTableModule, MatFormFieldModule, FormsModule, ReactiveFormsModule, MatSelectModule, MatProgressBarModule, MatSortModule],
  templateUrl: './main-data.html',
  styleUrl: './main-data.scss'
})
export class MainData {
  //Table data
  @ViewChild(MatTable) table!: MatTable<GameData>;
  @ViewChild(MatSort) sort!: MatSort;

  data: MatTableDataSource<ConsoleData> = new MatTableDataSource<ConsoleData>();
  columnsToDisplay: string[] = ["ConsoleName", "Games", "NoAchievements", "NotPlayed", "Tried", "Beaten", "Mastered", "Achievements", "Points"];

  model: Model;

  // Expose the enum to the template
  CompletionStatusType = CompletionStatusType;
  ConsoleSource = ConsoleSource;

  constructor(model: Model) {
    this.model = model;
  }

  ngOnInit() {
    //No data is passed through this behavior subject, it's only a trigger to refresh data
    this.model.getUpdateBehaviorSubject().subscribe(() => {
      this.data.data = Array.from(this.model.getConsoleData().values());
    })
  }

  ngAfterViewInit() {
    this.data.sort = this.sort;

    //init sorting data accessors
    this.data.sortingDataAccessor = (item, property) => {
      switch (property) {
        case "ConsoleName":
          return item.Name;
        case "Games":
          return Array.from(item.Games.values()).length;
        case "NoAchievements":
          return this.statusValue(item, CompletionStatusType.NO_ACHIEVEMENTS);
        case "NotPlayed":
          return this.statusValue(item, CompletionStatusType.NOT_PLAYED);
        case "Tried":
          return this.statusValue(item, CompletionStatusType.TRIED);
        case "Beaten":
          return this.statusValue(item, CompletionStatusType.BEATEN);
        case "Mastered":
          return this.statusValue(item, CompletionStatusType.MASTERED);
        case "Achievements":
          return this.achievementsValue(item);
        case "Points":
          return this.pointsValue(item);
        default:
          return item.Name;
      }
    }
  }

  gamesText(console: ConsoleData): string {
    return Array.from(console.Games.values()).length.toString();
  }

  statusValue(console: ConsoleData, status: CompletionStatusType): number {
    return Array.from(console.Games.values()).filter(game => game.CompletionStatus == status).length;
  }

  statusPercentValue(console: ConsoleData, status: CompletionStatusType): number {
    const gamesNbr: number = Array.from(console.Games.values()).length;
    return 100 * this.statusValue(console, status) / gamesNbr;
  }

  statusText(console: ConsoleData, status: CompletionStatusType): string {
    const statusGamesNbr: number = Array.from(console.Games.values()).filter(game => game.CompletionStatus == status).length;
    return statusGamesNbr.toString();
  }

  totalStatusValue(status: CompletionStatusType): number {
    let total: number = 0;
    this.data.data.forEach(c => {
      total += Array.from(c.Games.values()).filter(g => g.CompletionStatus == status).length;
    });
    return total;
  }

  totalStatusPercentValue(status: CompletionStatusType): number {
    return 100 * this.totalStatusValue(status) / this.model.flattenMap().length;
  }

  achievementsValue(console: ConsoleData): number {
    let earned: number = 0;
    Array.from(console.Games.values()).forEach(game => {
      earned += game.NumAwardedHardcore;
    });
    return earned
  }

  achievementsTotal(console: ConsoleData): number {
    let total: number = 0;
    Array.from(console.Games.values()).forEach(game => {
      total += game.MaxPossible;
    });
    return total
  }

  achievementsPercentValue(console: ConsoleData): number {
    return 100 * this.achievementsValue(console) / this.achievementsTotal(console);
  }

  totalAchievementsValue(): number {
    let earned: number = 0;
    this.data.data.forEach(console => {
      Array.from(console.Games.values()).forEach(game => {
        earned += game.NumAwardedHardcore;
      });
    })
    return earned
  }

  totalAchievementsTotal(): number {
    let total: number = 0;
    this.data.data.forEach(console => {
      Array.from(console.Games.values()).forEach(game => {
        total += game.MaxPossible;
      });
    });
    return total
  }

  totalAchievementsPercentValue(): number {
    return 100 * this.totalAchievementsValue() / this.totalAchievementsTotal();
  }

  pointsValue(console: ConsoleData): number {
    let earned: number = 0;
    Array.from(console.Games.values()).forEach(game => {
      earned += game.EarnedPoints;
    });
    return earned
  }

  pointsTotal(console: ConsoleData): number {
    let total: number = 0;
    Array.from(console.Games.values()).forEach(game => {
      total += game.Points;
    });
    return total
  }

  pointsPercentValue(console: ConsoleData): number {
    let earned: number = 0;
    let total: number = 0;
    Array.from(console.Games.values()).forEach(game => {
      earned += game.EarnedPoints;
      total += game.Points;
    });
    return 100 * earned / total;
  }

  totalPointsValue(): number {
    let earned: number = 0;
    this.data.data.forEach(console => {
      Array.from(console.Games.values()).forEach(game => {
        earned += game.EarnedPoints;
      });
    });
    return earned
  }

  totalPointsTotal(): number {
    let total: number = 0;
    this.data.data.forEach(console => {
      Array.from(console.Games.values()).forEach(game => {
        total += game.Points;
      });
    });
    return total
  }

  totalPointsPercentValue(): number {
    let earned: number = 0;
    let total: number = 0;
    this.data.data.forEach(console => {
      Array.from(console.Games.values()).forEach(game => {
        earned += game.EarnedPoints;
        total += game.Points;
      });
    });
    return 100 * earned / total;
  }

  totalText(): string {
    return this.data.data.length + " consoles";
  }

  totalGamesText(): string {
    let total: number = this.model.flattenMap().length;
    return total + " games";
  }

  /******************************/
  /* SORTING */
  /******************************/
  announceSortChange() {
    this.table.renderRows();
  }

  completionStatusText(status: CompletionStatusType): string {
    return UtilsService.completionStatusText(status);
  }

  completionStatusIcon(status: CompletionStatusType): string {
    return UtilsService.completionStatusIcon(status);
  }

  consoleIcon(name: string): string {
    return UtilsService.consoleIcon(name);
  }
}
