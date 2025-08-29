import { ConsoleSource } from "./consoleData";
import { CompletionStatusType } from "./gameData";

export enum CompareDataStatusEnum{
    OK = "OK",
    NOT_IN_LOCAL = "NOT_IN_LOCAL",
    NOT_IN_DATABASE = "NOT_IN_DATABASE",
    COMPLETION_STATUS_DIFFERENT = "COMPLETION_STATUS_DIFFERENT"
}

export type CompareDataMap = {
    compareData:Map<string, CompareData>
}

export type CompareData = {
    source:ConsoleSource;
    consoleName:string;
    consoleId:number;
    name:string;
    status:CompareDataStatusEnum;
    playniteStatus:CompletionStatusType;
    databaseStatus:CompletionStatusType;
}