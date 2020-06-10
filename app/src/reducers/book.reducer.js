import {PENDING, SUCCESS, FAILURE} from './index.js';
import {FETCH_BOOKS, FETCH_BOOK, LOOKUP_BOOK} from '../actions/book.action';

export default function (state = {}, action) {
    switch (action.type) {
        case PENDING(FETCH_BOOKS):
        case PENDING(FETCH_BOOK):
        case PENDING(LOOKUP_BOOK):{
            return {...state, pending: true, error:false};
        }
        case FAILURE(FETCH_BOOKS):
        case FAILURE(FETCH_BOOK):
        case FAILURE(LOOKUP_BOOK):{
            return {...state, pending: false, error: true};
        }
        case SUCCESS(LOOKUP_BOOK): {
            return {...state, pending: false, error: false, redirectTo: action.payload.data.id};
        }
        case SUCCESS(FETCH_BOOKS): {
            return {...state, pending: false, error: false, list: action.payload.data};
        }
        case SUCCESS(FETCH_BOOK): {
            return {...state, pending: false, error: false, detail: action.payload.data, redirectTo: undefined};
        }
        default:
            return state;
    }
}

