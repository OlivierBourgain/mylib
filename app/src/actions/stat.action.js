import axios from "axios";
import {store} from "../index";

const ROOT_URL = 'http://127.0.0.1:2017/api';

export const FETCH_STATS = 'FETCH_STATS';

export function fetchStats(year) {
    const url = `${ROOT_URL}/stats/${year}`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, {headers: {"Authorization": `Bearer ${idToken}`}});

    return {
        type: FETCH_STATS,
        payload: request
    }
}
