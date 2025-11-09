export enum AchievementType {
    MISSABLE = "MISSABLE",
    PROGRESSION = "PROGRESSION",
    WIN_CONDITION = "WIN_CONDITION"
}

export type AchievementData = {
    achieved: boolean;
    apiname: string;
    description: string;
    displayName: string;
    icon: string;
    icongray: string;
    percent: number;
    Type: AchievementType;
    Points: number;
    TrueRatio: number;
}