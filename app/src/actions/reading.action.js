import axios from "axios";
import {store} from "../index";

const ROOT_URL = 'http://127.0.0.1:2017/api';

export const FETCH_READINGS = 'FETCH_READINGS';
export const DELETE_READING = 'DELETE_READING';

export function fetchReadings(page, size, sort, descending) {
    const url = `${ROOT_URL}/readings?page=${page}&size=${size}`
        + `&sort=${sort},${descending?'DESC':'ASC'}`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`}});

    return {
        type: FETCH_READINGS,
        payload: request
    }
}

export const deleteReading = (reading) => {
    const url = `${ROOT_URL}/reading/${reading.id}`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.delete(url, {headers: {"Authorization" : `Bearer ${idToken}`}});

    // TODO: Use redux-thunk to chain delete and fetch
    // https://stackoverflow.com/questions/43692851/react-redux-chaining-a-second-api-request-and-dispatch
    return {
        type: DELETE_READING,
        payload: request
    }
}