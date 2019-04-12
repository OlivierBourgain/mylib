import {PENDING, SUCCESS, FAILURE} from './index.js';
import {FETCH_TAGS} from '../actions/tag.action';

export default function (state = {}, action) {
    switch (action.type) {
        case PENDING(FETCH_TAGS): {
            return {...state, pending: true, error:false};
        }
        case SUCCESS(FETCH_TAGS): {
            return {...state, pending: false, error: false, list: action.payload.data};
        }
        case FAILURE(FETCH_TAGS): {
            return {...state, pending: false, error: true};
        }
        default:
            return state;
    }
}
