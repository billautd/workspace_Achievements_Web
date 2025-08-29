import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { CompareData, CompareDataStatusEnum } from '../../model/compareData';
import { Model } from '../../model/model';
import { GameDataService } from '../../services/game-data-service';

@Component({
  selector: 'app-compare',
  imports: [CommonModule],
  templateUrl: './compare.html',
  styleUrl: './compare.css',
})
export class Compare {
  completionStatusDifferent: CompareData[] = [];
  notInLocal: CompareData[] = [];
  notInDatabase: CompareData[] = [];

  model: Model;
  gameDataService: GameDataService;
  isRequestRunning: boolean = false;

  constructor(model: Model,
    gameDataService: GameDataService
  ) {
    this.model = model;
    this.gameDataService = gameDataService;
  }

  ngOnInit() {
    this.model.getCompareUpdateBehaviorSubject().subscribe(() => {
      this.completionStatusDifferent = [];
      this.notInLocal = [];
      this.notInDatabase = [];

      let okNbr: number = 0;

      for (const compare of this.model.getCompareData()) {
        for (const compareData of compare[1]) {
          const status: CompareDataStatusEnum = compareData.status
          if (status == CompareDataStatusEnum.COMPLETION_STATUS_DIFFERENT) {
            this.completionStatusDifferent.push(compareData);
          } else if (status == CompareDataStatusEnum.NOT_IN_DATABASE) {
            this.notInDatabase.push(compareData);
          } else if (status == CompareDataStatusEnum.NOT_IN_LOCAL) {
            this.notInLocal.push(compareData);
          } else if (status == CompareDataStatusEnum.OK) {
            okNbr++;
          } else {
            console.log("no status")
          }
        }
      }

      this.completionStatusDifferent.sort((a, b) => a.name.localeCompare(b.name));
      this.notInLocal.sort((a, b) => a.name.localeCompare(b.name));
      this.notInDatabase.sort((a, b) => a.name.localeCompare(b.name));

      console.log(this.completionStatusDifferent.length + " completion status different")
      console.log(this.notInLocal.length + " not in local")
      console.log(this.notInDatabase.length + " not in database")
      console.log(okNbr + " OK")
    })
  }

  async copyToClipboard(text: string): Promise<void> {
    try {
      await navigator.clipboard.writeText(text);
    } catch (err) {
      console.error('Failed to copy text: ', err);
    }
  }

  compare(): void {
    this.isRequestRunning = true;
    this.gameDataService.requestCompareData(this.model).then(() => {
      console.log("Compare data OK");
      this.isRequestRunning = false;
    })
  }

}
