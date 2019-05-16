import axios from "axios";
import {store} from "../index";

export const LOGIN = 'LOGIN';
export const LOGOUT = 'LOGOUT';
export const FETCH_CSRF = 'FETCH_CSRF';

const ROOT_URL = 'http://127.0.0.1:2017/api';

export const login = response => {
    return {
        type: LOGIN,
        payload: {
            googleId: response.googleId,
            profileObj: response.profileObj,
            tokenObj: response.tokenObj
        }
    }
}

export const csrf = response => {
    const url = `${ROOT_URL}/csrf-token`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`}, withCredentials: true });
    return {
        type: FETCH_CSRF,
        payload: request
    }
}

export const logout = () => {
    return {
        type: LOGOUT,
        payload: {}
    }
}