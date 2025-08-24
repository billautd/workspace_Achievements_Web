import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import Chart from 'chart.js/auto';
import { CompletionStatusType } from '../../model/gameData';
import { Model } from '../../model/model';
import { GameDataService } from '../../services/game-data-service';
import { ConsoleData } from '../../model/consoleData';

@Component({
  selector: 'app-chart-canvas',
  imports: [],
  templateUrl: './chart-canvas.html',
  styleUrl: './chart-canvas.css'
})
export class ChartCanvas {
  @Input() consoleIds:number[] = [];
  @ViewChild("chartCanvas") canvas!: ElementRef<HTMLCanvasElement>

  model: Model;
  gameDataService: GameDataService;

  chart!: Chart<"pie", number[], string>;
  chartData: Map<CompletionStatusType, number> = new Map();
  chartBackgroundColors: Map<CompletionStatusType, string> = new Map();
  chartLabels: Map<CompletionStatusType, string> = new Map();

  isTabVisible: boolean = false;

  constructor(model: Model,
    gameDataService: GameDataService
  ) {
    this.model = model;
    this.gameDataService = gameDataService;
  }

  ngOnInit() {
    //No data is passed through this behavior subject, it's only a trigger to refresh data
    this.model.getUpdateBehaviorSubject().subscribe((gameData) => {
      const consoleDataList:ConsoleData[] = [];
      for(const id of this.consoleIds){
        const consoleData:ConsoleData | undefined = this.model.getConsoleData().get(id);
        if(consoleData){
          consoleDataList.push(consoleData);
        }
      }
      this.updateChartData(consoleDataList);
    })

    this.resetChartData();
    this.initGlobalChartData();
  }

  ngAfterViewInit() {
    this.createChart();
  }

  createChart(): void {
    this.chart = new Chart<"pie", number[], string>(this.getCanvas(),
      {
        type: "pie",
        options: {
          responsive: true,
          maintainAspectRatio: false
        },
        data: {
          labels: Array.from(this.chartLabels.keys()).map(this.gameDataService.completionStatusText),
          datasets: [
            {
              label: '',
              data: [],
              backgroundColor: Array.from(this.chartBackgroundColors.values())
            }
          ]
        }
      }
    )
  }

  initGlobalChartData(): void {
    //Labels
    Object.values(CompletionStatusType).forEach(t => this.chartLabels.set(t, this.gameDataService.completionStatusText(t)));
    //Background colors
    Object.values(CompletionStatusType).forEach(t => this.chartBackgroundColors.set(t, ""));

    const css: CSSStyleDeclaration = getComputedStyle(document.body);
    this.chartBackgroundColors.set(CompletionStatusType.BEATEN, css.getPropertyValue("--beaten"))
    this.chartBackgroundColors.set(CompletionStatusType.TRIED, css.getPropertyValue("--tried"))
    this.chartBackgroundColors.set(CompletionStatusType.NO_ACHIEVEMENTS, css.getPropertyValue("--no-achievements"))
    this.chartBackgroundColors.set(CompletionStatusType.NOT_PLAYED, css.getPropertyValue("--not-played"))
    this.chartBackgroundColors.set(CompletionStatusType.MASTERED, css.getPropertyValue("--mastered"))
  }

  resetChartData(): void {
    if(this.chart){
      Object.values(CompletionStatusType).forEach(t => this.chartData.set(t, 0));
    }
  }

  updateChart(): void {
    if (this.chart && this.isTabVisible) {
      this.chart.update();
    }
  }

  updateChartData(consoleDataList: ConsoleData[]): void {
    if(!this.chart){
      return;
    }
    this.resetChartData();

    for(const console of consoleDataList){
      for (const game of console.Games) {
        const status: CompletionStatusType = game[1].CompletionStatus;
        const val: number | undefined = this.chartData.get(status);
        this.chartData.set(status, (val ? val : 0) + 1);
      }
    }
    //Update data
    this.chart.data.datasets[0].data = Array.from(this.chartData.values());
    const labels:string[] = []
    for(const entry of this.chartData){
      labels.push(this.gameDataService.completionStatusText(entry[0]) + " (" + entry[1] + ")");
    }
    this.chart.data.labels = labels;
    this.updateChart();
  }

  getCanvas(): HTMLCanvasElement {
    return this.canvas.nativeElement;
  }
}
