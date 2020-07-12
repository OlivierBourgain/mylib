import axios from "axios";
import {store} from "../index";
import {ROOT_URL} from "./index";

export const FETCH_TAGS = 'FETCH_TAGS';
export const UPDATE_TAG = 'UPDATE_TAG';
export const DELETE_TAG = 'DELETE_TAG';

export function fetchTags() {
    const url = `${ROOT_URL}/tags`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`}});

    return {
        type: FETCH_TAGS,
        payload: request
    }
}

export function updateTag(tag) {
    const url = `${ROOT_URL}/tag`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.post(url, tag,{ headers: {"Authorization" : `Bearer ${idToken}`}});

    return {
        type: UPDATE_TAG,
        payload: request
    }
}

export function deleteTag(id) {
    const url = `${ROOT_URL}/tag/${id}`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.delete(url, {headers: {"Authorization": `Bearer ${idToken}`}})

    return {
        type: DELETE_TAG,
        payload: request
    }
}

