import axios from "axios";
import {store} from "../index";
import {ROOT_URL} from "./index";

export const FETCH_STATS = 'FETCH_STATS';
export const FETCH_STATS_DETAIL = 'FETCH_STATS_DETAIL';

export function fetchStats(year, discarded) {
    var url = `${ROOT_URL}/stats/${year}`;
    if (discarded) url += '?discarded=true';
    const idToken = store.getState().user.tokenObj.id_token;
    const request = axios.get(url, {headers: {"Authorization": `Bearer ${idToken}`}});

    return {
        type: FETCH_STATS,
        payload: request
    }
}

export function fetchStatsDetail(type, year, discarded) {
    var url = `${ROOT_URL}/stat/${type}/${year}`;
    if (discarded) url += '?discarded=true';
    const idToken = store.getState().user.tokenObj.id_token;
    const request = axios.get(url, {headers: {"Authorization": `Bearer ${idToken}`}});

    return {
        type: FETCH_STATS_DETAIL,
        payload: request
    }
}
