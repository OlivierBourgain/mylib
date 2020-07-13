import axios from "axios";
import {store} from "../index";
import {ROOT_URL} from "./index";

export const FETCH_BOOKS = 'FETCH_BOOKS';
export const FETCH_BOOK_TITLES = 'FETCH_BOOK_TITLES';
export const FETCH_BOOK = 'FETCH_BOOK';
export const UPDATE_BOOK = 'UPDATE_BOOK';
export const LOOKUP_BOOK = 'LOOKUP_BOOK';
export const DELETE_BOOK = 'DELETE_BOOK';
export const EXPORT_BOOKS = 'EXPORT BOOKS';
export const REBUILD_INDEX = 'REBUILD INDEX';

export const lookup = (term) => {
    const url = `${ROOT_URL}/lookup?term=${term}`
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`}});

    return {
        type: LOOKUP_BOOK,
        payload: request
    }
}

export function fetchBooks(page, size, term, discarded, sort, descending) {
    const url = `${ROOT_URL}/books?page=${page}&size=${size}&criteria=${term}&discarded=${discarded}`
                + `&sort=${sort},${descending?'DESC':'ASC'}`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`}});

    return {
        type: FETCH_BOOKS,
        payload: request
    }
}

export function fetchBookTitles() {
    const url = `${ROOT_URL}/booktitles`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`}});

    return {
        type: FETCH_BOOK_TITLES,
        payload: request
    }
}

export function fetchBook(id) {
    const url = `${ROOT_URL}/book/${id}`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`}});

    return {
        type: FETCH_BOOK,
        payload: request
    }
}

export function updateBook(book) {
    const url = `${ROOT_URL}/book`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.post(url, book,{headers: {"Authorization" : `Bearer ${idToken}`}});

    return {
        type: UPDATE_BOOK,
        payload: request
    }
}

export function updateDiscard(id, discard) {
    const url = `${ROOT_URL}/bookdiscard`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.post(url, `book=${id}&discard=${discard}`,{headers: {"Authorization" : `Bearer ${idToken}`}});

    return {
        type: UPDATE_BOOK,
        payload: request
    }
}

export function deleteBook(bookId) {
    const url = `${ROOT_URL}/book/${bookId}`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.delete(url, {headers: {"Authorization" : `Bearer ${idToken}`}});

    return {
        type: DELETE_BOOK,
        payload: request
    }
}

export const exportcsv = () => {
    const url = `${ROOT_URL}/exportbooks`
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`}});

    return {
        type: EXPORT_BOOKS,
        payload: request
    }
}

export const rebuildindex =  (bookId, date) => {
    return (dispatch, getState) => {
        const url = `${ROOT_URL}/rebuildindex`
        const idToken = store.getState().account.tokenObj.id_token;

        const res = dispatch({
            type: REBUILD_INDEX,
            payload: axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`}})
        });

        res.then((data) => {
            // Return the reading list, on the first page, ordered by date desc
            const pageable = getState().book.list.pageable;
            dispatch(fetchBooks( pageable.pageNumber, pageable.pageSize, '', false,'Updated', true));
        });
    }
}

