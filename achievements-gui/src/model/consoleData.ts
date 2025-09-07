import { GameData } from "./gameData";

export enum ConsoleSource {
    RETRO_ACHIEVEMENTS = "RETRO_ACHIEVEMENTS",
    STEAM = "STEAM",
    PS3 = "PS3",
    PSVITA = "PSVITA",
    XBOX_360 = "XBOX_360"
}

export type ConsoleData = {
    Name: string;
    ID: number;
    Active: number;
    IsGameSystem: string;
    Source: ConsoleSource;
    Games: Map<number, GameData>;
}