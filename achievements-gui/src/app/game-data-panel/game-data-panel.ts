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
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxChange, MatCheckboxModule } from '@angular/material/checkbox';


interface FilterData {
  text: string;
}

export enum SortOption {
  RARITY = "RARITY",
  DISPLAY_NAME = "DISPLAY_NAME",
  TYPE = "TYPE",
  POINTS = "POINTS",
  DISPLAY_ORDER = "DISPLAY_ORDER",
  RANDOM = "RANDOM"
}

@Component({
  selector: 'app-game-data',
  imports: [CommonModule, MatProgressBarModule, MatDividerModule, MatIconModule,
    MatFormFieldModule, MatSelectModule, FormsModule, ReactiveFormsModule, MatInputModule, MatCheckboxModule],
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

  readonly TIMER: number = 3 * 60 * 1000;

  readonly UP_ICON: string = "up_arrow.svg"
  readonly DOWN_ICON: string = "down_arrow.svg"

  selectedAchievement!: AchievementData | null;

  isRequestRunning: boolean = false;

  isAutoRefresh: boolean = false;

  model: Model;
  gameDataService: GameDataService;
  sortedAchievements: AchievementData[] = [];

  defaultSort = SortOption.DISPLAY_ORDER;
  sortOptions = new FormControl(this.defaultSort);
  sortOptionsList: SortOption[] = Object.values(SortOption)
  isSortAscending: boolean = true;
  sortDirectionIcon: string = this.UP_ICON;

  filterText: string = "";

  http: HttpClient = inject(HttpClient);

  constructor(model: Model,
    gameDataService: GameDataService) {
    this.model = model;
    this.gameDataService = gameDataService;
  }

  ngOnInit(): void {
    this.setupTimer();
  }

  setupTimer(): void {
    setInterval(() => {
      if (this.isAutoRefresh) {
        console.log("Sending refresh");
        this.refreshData();
      } else {
        console.log("No auto refresh")
      }
    }, this.TIMER);
  }

  requestGameData(data: GameData): void {
    this.isRequestRunning = true;
    this.gameDataService.requestGameData(data, this.model).then(newData => {
      this.selectedGame = newData
      this.clearAchievement();
      this.applySortFilter(this.sortOptions.value ? this.sortOptions.value : this.defaultSort)
      this.isRequestRunning = false;
    });
  }


  selectAchievement(ach: AchievementData): void {
    this.selectedAchievement = ach;
  }

  clearAchievement() {
    this.selectedAchievement = null;
  }

  setAutoRefresh(event: MatCheckboxChange): void {
    this.isAutoRefresh = event.checked;
  }

  achievementIconClass(percentage: number) {
    if (percentage == 0) {
      return "no-data-achievement";
    }
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
    if (this.selectedGame) {
      this.requestGameData(this.selectedGame)
    }
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

  changeSortDirection(): void {
    this.isSortAscending = !this.isSortAscending;
    this.sortDirectionIcon = this.isSortAscending ? this.UP_ICON : this.DOWN_ICON;
    this.applySortFilter(this.sortOptions.value ? this.sortOptions.value : this.defaultSort)
  }

  changeSort(event: MatSelectChange<SortOption>): void {
    this.applyDefaultSortDirection(event.value);
    this.applySortFilter(event.value);
  }

  changeFilter(): void {
    this.applySortFilter(this.sortOptions.value ? this.sortOptions.value : this.defaultSort)
  }

  clearFilter(): void {
    this.filterText = "";
    this.changeFilter();
  }

  openAchievementURL(ach: AchievementData): void {
    let url: string = "";
    let csl: ConsoleData | undefined = this.model.getConsoleData().get(this.selectedGame.ConsoleID);
    if (csl?.Source === ConsoleSource.STEAM) {
      url = "https://steamcommunity.com/stats/" + this.selectedGame.ID + "/achievements";
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

  applyDefaultSortDirection(sort: SortOption) {
    switch (sort) {
      case SortOption.RARITY:
        this.isSortAscending = false;
        break;
      case SortOption.DISPLAY_NAME:
      case SortOption.RANDOM:
      case SortOption.POINTS:
      case SortOption.TYPE:
      case SortOption.DISPLAY_ORDER:
        this.isSortAscending = true;
        break;
    }
    this.sortDirectionIcon = this.isSortAscending ? this.UP_ICON : this.DOWN_ICON;
  }

  applySortFilter(sort: SortOption): void {
    let sortAlgo;
    switch (sort) {
      case SortOption.RARITY:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => this.isSortAscending ? (ach1.percent - ach2.percent) : (ach2.percent - ach1.percent);
        break;
      case SortOption.DISPLAY_NAME:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => this.isSortAscending ? ach1.displayName.localeCompare(ach2.displayName) : ach2.displayName.localeCompare(ach1.displayName);
        break;
      case SortOption.POINTS:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => this.isSortAscending ? (ach1.Points - ach2.Points) : (ach2.Points - ach1.Points);
        break;
      case SortOption.TYPE:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => this.isSortAscending ?
          (UtilsService.sortAchievementType(ach1.Type) - UtilsService.sortAchievementType(ach2.Type)) : (UtilsService.sortAchievementType(ach2.Type) - UtilsService.sortAchievementType(ach1.Type));
        break;
      case SortOption.DISPLAY_ORDER:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => this.isSortAscending ? (ach1.DisplayOrder - ach2.DisplayOrder) : (ach2.DisplayOrder - ach1.DisplayOrder);
        break;
      case SortOption.RANDOM:
        sortAlgo = (ach1: AchievementData, ach2: AchievementData) => Math.random() < 0.5 ? 1 : -1;
        break;
    }

    this.sortedAchievements = [...this.selectedGame.AchievementData.
      filter(ach => ach.displayName.trim().toLocaleLowerCase().includes(this.filterText.trim().toLocaleLowerCase())
        || ach.description.trim().toLocaleLowerCase().includes(this.filterText.trim().toLocaleLowerCase()))
      .sort(sortAlgo)];
  }

  sortText(sort: SortOption) {
    switch (sort) {
      case SortOption.RARITY:
        return "Rarity";
      case SortOption.DISPLAY_NAME:
        return "Display name"
      case SortOption.POINTS:
        return "Points"
      case SortOption.TYPE:
        return "Type";
      case SortOption.DISPLAY_ORDER:
        return "Display order"
      case SortOption.RANDOM:
        return "Random";
    }
  }

  completionStatusClass(status: CompletionStatusType): any {
    return UtilsService.completionStatusClass(status);
  }

  completionStatusText(status: CompletionStatusType): string {
    return UtilsService.completionStatusText(status);
  }
}
