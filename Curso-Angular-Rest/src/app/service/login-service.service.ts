import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { url } from 'inspector';
import { AppConstants } from '../app-constants';

@Injectable({
  providedIn: 'root'
})
export class LoginServiceService {

  constructor(private http: HttpClient) { }

  login(usuario) {
    console.info('User' + usuario.login);
    return this.http.post(AppConstants.baseLogin, JSON.stringify(usuario)).subscribe(data => {

      /* Retorno Http */

      //console.info(JSON.parse(JSON.stringify(data)).Authorization.split(' ')[1]);
      var token = JSON.parse(JSON.stringify(data)).Authorization.split(' ')[1];

      localStorage.setItem("token", token);

      //console.info("Token: " + localStorage.getItem("token"));
    },
      error => {
        console.error('Erro ao fazer login');
      }
    );
  }
}
