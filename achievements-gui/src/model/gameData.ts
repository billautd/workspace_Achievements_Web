import { AchievementData } from "./achievementData";

export enum CompletionStatusType {
    NOT_PLAYED = "NOT_PLAYED",
    TRIED = "TRIED",
    BEATEN = "BEATEN",
    MASTERED = "MASTERED",
    NO_ACHIEVEMENTS = "NO_ACHIEVEMENTS",
    CANNOT_PLAY = "CANNOT_PLAY"
}

export type GameData = {
    Title: string;
    ID: number;
    ConsoleID: number;
    ConsoleName: string;
    CompletionStatus: CompletionStatusType,
    MaxPossible: number,
    NumAwardedHardcore: number;
    Points: number;
    TruePoints: number;
    EarnedPoints: number;
    EarnedTruePoints: number;
    Ratio: number;
    EarnedRatio: number;
    AchievementData: AchievementData[];
    Percent: number;
    Image: string;
}