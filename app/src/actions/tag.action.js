import axios from "axios";
import {store} from "../index";

const ROOT_URL = 'http://localhost:2017/api';

export const FETCH_TAGS = 'FETCH_TAGS';
export const UPDATE_TAG = 'UPDATE_TAG';

export function fetchTags() {
    const url = `${ROOT_URL}/tags`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`} });

    return {
        type: FETCH_TAGS,
        payload: request
    }
}

export function updateTag(tag) {
    const url = `${ROOT_URL}/tag`;
    const idToken = store.getState().account.tokenObj.id_token;

    const request = axios.post(url, tag,{ headers: {"Authorization" : `Bearer ${idToken}`} });

    return {
        type: UPDATE_TAG,
        payload: request
    }
}

