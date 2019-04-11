import {PENDING, SUCCESS, FAILURE} from './index.js';
import {FETCH_READINGS} from '../actions/reading.action';

export default function (state = {}, action) {
    switch (action.type) {
        case PENDING(FETCH_READINGS): {
            return {...state, pending: true, error:false};
        }
        case SUCCESS(FETCH_READINGS): {
            return {...state, pending: false, error: false, list: action.payload.data};
        }
        case FAILURE(FETCH_READINGS): {
            return {...state, pending: false, error: true};
        }
        default:
            return state;
    }
}
