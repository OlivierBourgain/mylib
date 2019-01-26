import axios from 'axios';

const ROOT_URL = 'http://localhost:2017/api';

export const FETCH_BOOKS = 'FETCH_BOOKS';

export function fetchBooks() {
    const url = `${ROOT_URL}/books`;
    const request = axios.get(url);

    return {
        type: FETCH_BOOKS,
        payload: request
    }
}
