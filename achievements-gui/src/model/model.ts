import { Injectable } from "@angular/core";
import { GameData } from "./gameData";
import { BehaviorSubject, Observable } from "rxjs";
import { DataSource } from "@angular/cdk/collections";

@Injectable({
  providedIn: 'root'
})
export class Model{
    private gameListSubject = new BehaviorSubject<GameData[]>([]);

    getGameListSubject():BehaviorSubject<GameData[]>{
        return this.gameListSubject;
    }
}