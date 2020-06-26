import {PENDING, SUCCESS, FAILURE} from './index.js';
import {FETCH_STATS} from '../actions/stat.action';

export default function (state = {}, action) {
    switch (action.type) {
        case PENDING(FETCH_STATS): {
            return {...state, pending: true, error:false};
        }
        case FAILURE(FETCH_STATS): {
            return {...state, pending: false, error: true};
        }
        case SUCCESS(FETCH_STATS): {
            return {...state, pending: false, error: false, list: action.payload.data};
        }
        default:
            return state;
    }
}

