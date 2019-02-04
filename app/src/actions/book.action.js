import axios from "axios";
import {store} from "../index";

const ROOT_URL = 'http://localhost:2017/api';

export const FETCH_BOOKS = 'FETCH_BOOKS';

export function fetchBooks() {
    const url = `${ROOT_URL}/books`;
    const tokenStr = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${tokenStr}`} });

    return {
        type: FETCH_BOOKS,
        payload: request
    }
}
