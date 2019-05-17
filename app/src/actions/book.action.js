import axios from "axios";
import {store} from "../index";

const ROOT_URL = 'http://127.0.0.1:2017/api';

export const FETCH_BOOKS = 'FETCH_BOOKS';
export const FETCH_BOOK = 'FETCH_BOOK';

export function fetchBooks(page, size, term) {
    const url = `${ROOT_URL}/books?page=${page}&size=${size}&criteria=${term}`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`}, withCredentials: true });

    return {
        type: FETCH_BOOKS,
        payload: request
    }
}

export function fetchBook(id) {
    const url = `${ROOT_URL}/book/${id}`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`}, withCredentials: true });

    return {
        type: FETCH_BOOK,
        payload: request
    }
}
