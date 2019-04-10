import {PENDING, SUCCESS, FAILURE} from './index.js';
import {FETCH_BOOKS} from '../actions/book.action';

export default function (state = {}, action) {
    switch (action.type) {
        case PENDING(FETCH_BOOKS): {
            return {...state, pending: true, error:false};
        }
        case SUCCESS(FETCH_BOOKS): {
            return {...state, pending: false, error: false, data: action.payload.data};
        }
        case FAILURE(FETCH_BOOKS): {
            return {...state, pending: false, error: true};
        }
        default:
            return state;
    }
}
