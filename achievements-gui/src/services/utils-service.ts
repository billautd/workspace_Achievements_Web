import { AchievementType } from "../model/achievementData";
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

  static completionStatusIcon(completionStatus: CompletionStatusType) {
    switch (completionStatus) {
      case CompletionStatusType.MASTERED:
        return "status_mastered.svg";
      case CompletionStatusType.BEATEN:
        return "status_beaten.svg";
      case CompletionStatusType.NOT_PLAYED:
        return "status_not_played.svg";
      case CompletionStatusType.NO_ACHIEVEMENTS:
        return "status_no_achievements.svg";
      case CompletionStatusType.TRIED:
        return "status_tried.svg";
      default:
        return "status_no_achievements.svg";
    }
  }

  static consoleIcon(name: string) {
    let icon: string = "console/";
    switch (name) {
      case "32X":
        icon += "32x";
        break
      case "3DO Interactive Multiplayer":
        icon += "3do";
        break;
      case "Amstrad CPC":
        icon += "cpc";
        break;
      case "Apple II":
        icon += "a2";
        break;
      case "Arcade":
        icon += "arc";
        break;
      case "Arcadia 2001":
        icon += "a2001";
        break;
      case "Arduboy":
        icon += "ard";
        break;
      case "Atari 2600":
        icon += "2600";
        break;
      case "Atari 7800":
        icon += "7800";
        break;
      case "Atari Jaguar":
        icon += "jag";
        break;
      case "Atari Jaguar CD":
        icon += "jcd";
        break;
      case "Atari Lynx":
        icon += "lynx";
        break;
      case "ColecoVision":
        icon += "cv";
        break;
      case "Dreamcast":
        icon += "dc";
        break;
      case "Elektor TV Games Computer":
        icon += "elek";
        break;
      case "Fairchild Channel F":
        icon += "chf";
        break;
      case "Famicom Disk System":
        icon += "fds";
        break;
      case "Game Boy":
        icon += "gb";
        break;
      case "Game Boy Advance":
        icon += "gba";
        break;
      case "Game Boy Color":
        icon += "gbc";
        break;
      case "Game Gear":
        icon += "gg";
        break;
      case "GameCube":
        icon += "gc";
        break;
      case "Genesis/Mega Drive":
        icon += "md";
        break;
      case "Intellivision":
        icon += "intv";
        break;
      case "Interton VC 4000":
        icon += "vc4000";
        break;
      case "Magnavox Odyssey 2":
        icon += "mo2";
        break;
      case "Master System":
        icon += "sms";
        break;
      case "Mega Duck":
        icon += "duck";
        break;
      case "MSX":
        icon += "msx";
        break;
      case "Neo Geo CD":
        icon += "ngcd";
        break;
      case "Neo Geo Pocket":
        icon += "ngp";
        break;
      case "NES/Famicom":
        icon += "nes";
        break;
      case "Nintendo 64":
        icon += "n64";
        break;
      case "Nintendo DS":
        icon += "ds";
        break;
      case "Nintendo DSi":
        icon += "dsi";
        break;
      case "PC Engine CD/TurboGrafx-CD":
        icon += "pccd";
        break;
      case "PC Engine/TurboGrafx-16":
        icon += "pce";
        break;
      case "PC-8000/8800":
        icon += "8088";
        break;
      case "PC-FX":
        icon += "pc-fx";
        break;
      case "PlayStation":
        icon += "ps1";
        break;
      case "PlayStation 2":
        icon += "ps2";
        break;
      case "PlayStation 3":
        icon += "ps3";
        break;
      case "PlayStation Portable":
        icon += "psp";
        break;
      case "PlayStation Vita":
        icon += "psv";
        break;
      case "Pokemon Mini":
        icon += "mini";
        break;
      case "Saturn":
        icon += "sat";
        break;
      case "Sega CD":
        icon += "scd";
        break;
      case "SG-1000":
        icon += "sg1k";
        break;
      case "SNES/Super Famicom":
        icon += "snes";
        break;
      case "Standalone":
        icon += "exe";
        break;
      case "PC (Windows)":
      case "Steam":
        icon += "steam";
        break;
      case "Uzebox":
        icon += "uze";
        break;
      case "Vectrex":
        icon += "vect";
        break;
      case "Virtual Boy":
        icon += "vb";
        break;
      case "WASM-4":
        icon += "wasm4";
        break;
      case "Watara Supervision":
        icon += "wsv";
        break;
      case "Wii":
        icon += "wii";
        break;
      case "WonderSwan":
        icon += "ws";
        break;
      case "Xbox 360":
        icon += "xbox360";
        break;
    }
    return icon + ".png";
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

  static sortAchievementType(type: AchievementType) {
    switch (type) {
      case AchievementType.MISSABLE:
        return 1;
      case AchievementType.PROGRESSION:
        return 2;
      case AchievementType.WIN_CONDITION:
        return 3;
      default:
        return 999;
    }
  }

  static spaceNumber(n: number): string {
    return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
  }
}