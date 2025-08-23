import { GameData } from "./gameData";

export enum ConsoleSource{
    RETRO_ACHIEVEMENTS = "RETRO_ACHIEVEMENTS",
    STEAM = "STEAM",
    STANDALONE = "STANDALONE"
}

export type ConsoleData = {
    Name:string;
    ID:number;
    Active:number;
    IsGameSystem:string;
    Source:ConsoleSource;
    Games:Map<number, GameData>;
}