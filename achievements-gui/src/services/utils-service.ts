import { ConsoleSource } from "../model/consoleData";
import { CompletionStatusType } from "../model/gameData";

export class UtilsService {
  static delay(ms: number): Promise<any> {
    return new Promise((res) => setTimeout(res, ms));
  }

  static completionStatusText(completionStatus: CompletionStatusType): string {
    switch (completionStatus) {
      case CompletionStatusType.MASTERED:
        return "Mastered";
      case CompletionStatusType.BEATEN:
        return "Beaten";
      case CompletionStatusType.NOT_PLAYED:
        return "Not played";
      case CompletionStatusType.NO_ACHIEVEMENTS:
        return "No achievements";
      case CompletionStatusType.TRIED:
        return "Tried";
      default:
        return "No status";
    }
  }

  static completionStatusClass(completionStatus: CompletionStatusType) {
    return {
      'status-not-played': completionStatus === 'NOT_PLAYED',
      'status-mastered': completionStatus === 'MASTERED',
      'status-tried': completionStatus === 'TRIED',
      'status-beaten': completionStatus === 'BEATEN',
      'status-no-achievements': completionStatus === 'NO_ACHIEVEMENTS'
    };
  }

  static consoleSourceText(consoleSource: ConsoleSource) {
    switch (consoleSource) {
      case ConsoleSource.PS3:
        return "PlayStation 3";
      case ConsoleSource.PSVITA:
        return "PlayStation Vita";
      case ConsoleSource.STEAM:
        return "Steam";
      case ConsoleSource.RETRO_ACHIEVEMENTS:
        return "Retro Achievements";
      case ConsoleSource.XBOX_360:
        return "Xbox 360";
      default:
        return "No source";
    }
  }
}