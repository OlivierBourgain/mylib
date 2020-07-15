import {PENDING, SUCCESS, FAILURE} from './index.js';
import {FETCH_READINGS, FETCH_BOOKREADINGS, DELETE_READING} from '../actions/reading.action';

export default function (state = {}, action) {
    switch (action.type) {
        case PENDING(FETCH_READINGS):
        case PENDING(DELETE_READING): {
            return {...state, pending: true, error:false};
        }
        case SUCCESS(FETCH_READINGS): {
            return {...state, pending: false, error: false, list: action.payload.data};
        }
        case SUCCESS(FETCH_BOOKREADINGS): {
            return {...state, bookreadings: action.payload.data};
        }
        case SUCCESS(DELETE_READING): {
            return {...state, pending: false, error:false};
        }
        case FAILURE(FETCH_READINGS):
        case FAILURE(DELETE_READING):{
            return {...state, pending: false, error: true};
        }
        default:
            return state;
    }
}
