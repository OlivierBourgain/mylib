import axios from "axios";
import {store} from "../index";

const ROOT_URL = 'http://localhost:2017/api';

export const FETCH_READINGS = 'FETCH_READINGS';

export function fetchReadings() {
    const url = `${ROOT_URL}/readings`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`} });

    return {
        type: FETCH_READINGS,
        payload: request
    }
}
