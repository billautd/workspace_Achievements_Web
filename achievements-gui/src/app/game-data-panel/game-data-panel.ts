import { Component, inject, Input } from '@angular/core';
import { Model } from '../../model/model';
import { HttpClient } from '@angular/common/http';
import { CompletionStatusType, GameData } from '../../model/gameData';
import { GameDataService } from '../../services/game-data-service';
import { AchievementData, AchievementType } from '../../model/achievementData';
import { CommonModule } from '@angular/common';
import { ConsoleData, ConsoleSource } from '../../model/consoleData';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { UtilsService } from '../../services/utils-service';


export enum SortOption {
  RARITY_DESCENDING = "RARITY_DESCENDING",
  RARITY_ASCENDING = "RARITY_ASCENDING",
  DISPLAY_NAME_DESCENDING = "DISPLAY_NAME_DESCENDING",
  DISPLAY_NAME_ASCENDING = "DISPLAY_NAME_ASCENDING",
  TYPE_DESCENDING = "TYPE_DESCENDING",
  TYPE_ASCENDING = "TYPE_ASCENDING",
  POINTS_DESCENDING = "POINTS_DESCENDING",
  POINTS_ASCENDING = "POINTS_ASCENDING",
  TRUE_POINTS_DESCENDING = "TRUE_POINTS_DESCENDING",
  TRUE_POINTS_ASCENDING = "TRUE_POINTS_ASCENDING",
  RATIO_DESCENDING = "RATIO_DESCENDING",
  RATIO_ASCENDING = "RATIO_ASCENDING",
  ID_DESCENDING = "ID_DESCENDING",
  ID_ASCENDING = "ID_ASCENDING"
}

@Component({
  selector: 'app-game-data',
  imports: [CommonModule, MatProgressBarModule, MatDividerModule, MatFormFieldModule, MatSelectModule, FormsModule, ReactiveFormsModule],
  templateUrl: './game-data-panel.html',
  styleUrl: './game-data-panel.scss'
})
export class GameDataPanel {
  @Input() selectedGame: GameData = {
    Title: '',
    ID: 0,
    ConsoleID: 0,
    ConsoleName: '',
    CompletionStatus: CompletionStatusType.NOT_PLAYED,
    MaxPossible: 0,
    NumAwardedHardcore: 0,
    Points: 0,
    TruePoints: 0,
    Ratio: 0,
    EarnedRatio: 0,
    AchievementData: [],
    Percent: 0,
    Image: '',
    EarnedPoints: 0,
    EarnedTruePoints: 0
  };

  readonly COMMON_MAX_RARITY: number = 50;
  readonly UNCOMMON_MAX_RARITY: number = 20;
  readonly RARE_MAX_RARITY: number = 10;
  readonly SUPER_RARE_MAX_RARITY: number = 5;

  selectedAchievement!: AchievementData | null;

  isRequestRunning: boolean = false;

  model: Model;
  gameDataService: GameDataService;
  sortedAchievements: AchievementData[] = [];

  defaultSort = SortOption.ID_ASCENDING
  sortOptions = new FormControl(this.defaultSort);
  sortOptionsList: SortOption[] = Object.values(SortOption)

  http: HttpClient = inject(HttpClient);

  constructor(model: Model,
    gameDataService: GameDataService) {
    this.model = model;
    this.gameDataService = gameDataService;
  }

  selectGame(data: GameData): void {
    this.isRequestRunning = true;
    this.gameDataService.requestGameData(data, this.model).then(newData => {
      this.selectedGame = newData
      this.clearAchievement();
      this.sort(this.sortOptions.value ? this.sortOptions.value : this.defaultSort)
      this.isRequestRunning = false;
    });
  }

  selectAchievement(ach: AchievementData): void {
    this.selectedAchievement = ach;
  }

  clearAchievement() {
    this.selectedAchievement = null;
  }

  achievementIconClass(percentage: number) {
    if (percentage >= this.COMMON_MAX_RARITY) {
      return "common-achievement";
    }
    if (percentage >= this.UNCOMMON_MAX_RARITY) {
      return "uncommon-achievement";
    }
    if (percentage >= this.RARE_MAX_RARITY) {
      return "rare-achievement";
    }
    if (percentage >= this.SUPER_RARE_MAX_RARITY) {
      return "super-rare-achievement";
    }
    return "ultra-rare-achievement";
  }

  sourceIcon(game: GameData): string {
    let source: ConsoleSource | undefined = this.model.getConsoleData().get(game.ConsoleID)?.Source;
    switch (source) {
      case ConsoleSource.PS3:
        return "ps3.png";
      case ConsoleSource.PSVITA:
        return "psvita.png";
      case ConsoleSource.RETRO_ACHIEVEMENTS:
        return "ra.png";
      case ConsoleSource.STEAM:
        return "steam.png";
      case ConsoleSource.XBOX_360:
        return "xbox360.png";
      default:
        console.log("No icon for source " + source);
        return "";
    }
  }

  refreshData() {
    this.selectGame(this.selectedGame)
  }

  isMissable(ach: AchievementData): boolean {
    return ach.Type === AchievementType.MISSABLE
  }

  isProgression(ach: AchievementData): boolean {
    return ach.Type === AchievementType.PROGRESSION;
  }
  isWinCondition(ach: AchievementData): boolean {
    return ach.Type === AchievementType.WIN_CONDITION;
  }

  changeSort(event: MatSelectChange<SortOption>): void {
    this.sort(event.value);
  }

  openAchievementURL(ach: AchievementData): void {
    let url: string = "";
    let csl: ConsoleData | undefined = this.model.getConsoleData().get(this.selectedGame.ConsoleID);
    if (csl?.Source === ConsoleSource.STEAM) {
      // url = "https://store.steampowered.com/app/" + data.ID;
    } else if (csl?.Source === ConsoleSource.PS3 || csl?.Source === ConsoleSource.PSVITA) {
      // url = "https://www.psnprofiles.com/trophies/" + data.ID;
    } else if (csl?.Source === ConsoleSource.XBOX_360) {
      // url = "https://www.xboxachievements.com/game/" + this.parseXBOXGameName(data.Title) + "/achievements";
    } else if (csl?.Source === ConsoleSource.RETRO_ACHIEVEMENTS) {
      url = "https://retroachievements.org/achievement/" + ach.ID;
    } else {
      console.log("No console source found for game " + this.selectedGame);
      return;
    }
    window.open(url, "_blank");
  }

  sort(sort: SortOption): void {
    let sortAlgo;
    switch (sort) {
      case SortOption.RARITY_DESCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => ach2.percent - ach1.percent;
        break;
      case SortOption.RARITY_ASCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => ach1.percent - ach2.percent;
        break;
      case SortOption.DISPLAY_NAME_DESCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => ach2.displayName.localeCompare(ach1.displayName);
        break;
      case SortOption.DISPLAY_NAME_ASCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => ach1.displayName.localeCompare(ach2.displayName);
        break;
      case SortOption.POINTS_DESCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => ach2.Points - ach1.Points;
        break;
      case SortOption.POINTS_ASCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => ach1.Points - ach2.Points;
        break;
      case SortOption.TRUE_POINTS_DESCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => ach2.TrueRatio - ach1.TrueRatio;
        break;
      case SortOption.TRUE_POINTS_ASCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => ach1.TrueRatio - ach2.TrueRatio;
        break;
      case SortOption.TYPE_DESCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => UtilsService.sortAchievementType(ach2.Type) - UtilsService.sortAchievementType(ach1.Type);
        break;
      case SortOption.TYPE_ASCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => UtilsService.sortAchievementType(ach1.Type) - UtilsService.sortAchievementType(ach2.Type);
        break;
      case SortOption.RATIO_DESCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => (ach2.TrueRatio / ach2.Points) - (ach1.TrueRatio / ach1.Points);
        break;
      case SortOption.RATIO_ASCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => (ach1.TrueRatio / ach1.Points) - (ach2.TrueRatio / ach2.Points);
        break;
      case SortOption.ID_DESCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => ach2.ID - ach1.ID;
        break;
      case SortOption.ID_ASCENDING:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => ach1.ID - ach2.ID;
        break;
    }
    this.sortedAchievements = [...this.selectedGame.AchievementData].sort(sortAlgo);
  }

  sortText(sort: SortOption) {
    switch (sort) {
      case SortOption.RARITY_DESCENDING:
        return "Rarity (Descending)"
      case SortOption.RARITY_ASCENDING:
        return "Rarity (Ascending)";
      case SortOption.DISPLAY_NAME_DESCENDING:
        return "Display name (Descending)"
      case SortOption.DISPLAY_NAME_ASCENDING:
        return "Display name (Ascending)"
      case SortOption.POINTS_DESCENDING:
        return "Points (Descending)"
      case SortOption.POINTS_ASCENDING:
        return "Points (Ascending)"
      case SortOption.TYPE_DESCENDING:
        return "Type (Descending)"
      case SortOption.TYPE_ASCENDING:
        return "Type (Ascending)";
      case SortOption.TRUE_POINTS_DESCENDING:
        return "True points (Descending)"
      case SortOption.TRUE_POINTS_ASCENDING:
        return "True points (Ascending)"
      case SortOption.RATIO_DESCENDING:
        return "Ratio (Descending)"
      case SortOption.RATIO_ASCENDING:
        return "Ratio (Ascending)"
      case SortOption.ID_DESCENDING:
        return "ID (Descending)"
      case SortOption.ID_ASCENDING:
        return "ID (Ascending)"
    }
  }
}
