export enum CompletionStatusType{
    NOT_PLAYED = "NOT_PLAYED",
    TRIED = "TRIED",
    BEATEN = "BEATEN",
    MASTERED = "MASTERED",
    NO_ACHIEVEMENTS = "NO_ACHIEVEMENTS",
    CANNOT_PLAY = "CANNOT_PLAY"
}

export type GameData = {
    Title:string;
    ID:number;
    ConsoleId:number;
    ConsoleName:string;
    CompletionStatus:CompletionStatusType,
    NumAchievements:number,
    NumAwarded:number;
    Points:number;
}