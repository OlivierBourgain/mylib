import axios from "axios";
import {store} from "../index";

const ROOT_URL = 'http://localhost:2017/api';

export const FETCH_TAGS = 'FETCH_TAGS';

export function fetchTags() {
    const url = `${ROOT_URL}/tags`;
    //const tokenStr = store.getState().account.tokenObj.id_token;
    const tokenStr = "";
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${tokenStr}`} });

    return {
        type: FETCH_TAGS,
        payload: request
    }
}
