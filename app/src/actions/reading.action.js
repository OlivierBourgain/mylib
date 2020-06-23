import axios from "axios";
import {store} from "../index";

const ROOT_URL = 'http://127.0.0.1:2017/api';

export const FETCH_READINGS = 'FETCH_READINGS';
export const DELETE_READING = 'DELETE_READING';
export const ADD_READING = 'ADD_READING';

export function fetchReadings(page, size, sort, descending) {
    const url = `${ROOT_URL}/readings?page=${page}&size=${size}`
        + `&sort=${sort},${descending?'DESC':'ASC'}`;
    const idToken = store.getState().account.tokenObj.id_token;
    const request = axios.get(url, { headers: {"Authorization" : `Bearer ${idToken}`}});

    return {
        type: FETCH_READINGS,
        payload: request
    }
}

export const deleteReading = (reading) => {
    return (dispatch, getState) => {
        const url = `${ROOT_URL}/reading/${reading.id}`;
        const idToken = store.getState().account.tokenObj.id_token;

        const res = dispatch({
            type: DELETE_READING,
            payload: axios.delete(url, {headers: {"Authorization": `Bearer ${idToken}`}})
        });

        res.then((data) => {
            // Return the reading list, on the first page, ordered by date desc
            const pageable = getState().reading.list.pageable;
            dispatch(fetchReadings(pageable.pageNumber, pageable.pageSize, 'Date', true));
        });
    }
}

export const addReading =  (bookId, date) => {
    return (dispatch, getState) => {
        const url = `${ROOT_URL}/reading/`;
        const idToken = store.getState().account.tokenObj.id_token;

        const res = dispatch({
            type: ADD_READING,
            payload: axios.post(url, {book: bookId, date: date}, {headers: {"Authorization": `Bearer ${idToken}`}})
        });

        res.then((data) => {
            // Return the reading list, on the first page, ordered by date desc
            const pageable = getState().reading.list.pageable;
            dispatch(fetchReadings( pageable.pageNumber, pageable.pageSize, 'Date', true));
        });
    }
}

