import { Injectable } from '@angular/core';
import { webSocket } from 'rxjs/webSocket';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  connect(endPoint:string){
    const path:string = environment.SOCKET_ENDPOINT + endPoint;
    console.log("Connecting socket at " + path)
    return webSocket(path);
  }
}
