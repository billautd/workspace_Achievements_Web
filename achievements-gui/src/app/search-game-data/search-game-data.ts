import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { CompletionStatusType, GameData } from '../../model/gameData';
import { Model } from '../../model/model';
import { UtilsService } from '../../services/utils-service';
import { MatDialogRef } from '@angular/material/dialog';
import { go } from 'fuzzysort';

@Component({
  selector: 'app-search-game-data',
  imports: [MatFormFieldModule, MatInputModule, FormsModule, CommonModule, ReactiveFormsModule,
    MatIconModule, MatListModule
  ],
  templateUrl: './search-game-data.html',
  styleUrl: './search-game-data.scss'
})
export class SearchGameData {
  filterText: string = "";
  filteredGamesList: GameData[] = [];

  model: Model;
  dialogRef: MatDialogRef<SearchGameData>;

  constructor(model: Model,
    dialogRef: MatDialogRef<SearchGameData>
  ) {
    this.model = model;
    this.dialogRef = dialogRef;
  }

  applyFilter(): void {
    if (this.filterText.trim().length < 3) {
      this.filteredGamesList = [];
      return;
    }

    //Fuzzysearch
    this.filteredGamesList = go<GameData>(this.filterText, this.model.flattenMap(), { key: "Title", threshold: 0.5 }).map(r => r.obj);
  }

  clearFilterText(): void {
    this.filterText = "";
  }

  sanitizeString(str: string): string {
    return str.trim().toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
  }

  completionStatusClass(status: CompletionStatusType): any {
    return UtilsService.completionStatusClass(status);
  }

  completionStatusText(status: CompletionStatusType): string {
    return UtilsService.completionStatusText(status);
  }

  selectGame(game: GameData) {
    this.dialogRef.close(game);
  }
}
