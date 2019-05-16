import axios from "axios";
import {store} from "../index";

const ROOT_URL = 'http://127.0.0.1:2017/api';

export const FETCH_TAGS = 'FETCH_TAGS';
export const UPDATE_TAG = 'UPDATE_TAG';

export function fetchTags() {
    const url = `${ROOT_URL}/tags`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`}, withCredentials: true });

    return {
        type: FETCH_TAGS,
        payload: request
    }
}

export function updateTag(tag) {
    const url = `${ROOT_URL}/tag`;
    const idToken = store.getState().account.tokenObj.id_token;
    const csrf = store.getState().account.csrfToken;

    const request = axios.post(url, tag,{
        headers: {
            "Authorization" : `Bearer ${idToken}`,
            "X-XSRF-TOKEN" : csrf
        },
        withCredentials: true
    });

    return {
        type: UPDATE_TAG,
        payload: request
    }
}

