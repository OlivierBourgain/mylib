import {PENDING, SUCCESS, FAILURE} from './index.js';
import {FETCH_STATS, FETCH_STATS_DETAIL} from '../actions/stat.action';

export default function (state = {}, action) {
    switch (action.type) {

        case PENDING(FETCH_STATS): {
            return {...state, listpending: true, error:false};
        }
        case FAILURE(FETCH_STATS): {
            return {...state, listpending: false, error: true};
        }
        case SUCCESS(FETCH_STATS): {
            return {...state, listpending: false, error: false, list: action.payload.data};
        }

        case PENDING(FETCH_STATS_DETAIL): {
            return {...state, detailpending: true, error:false};
        }
        case FAILURE(FETCH_STATS_DETAIL): {
            return {...state, detailpending: false, error: true};
        }
        case SUCCESS(FETCH_STATS_DETAIL): {
            return {...state, detailpending: false, detail: action.payload.data};
        }
        default:
            return state;
    }
}

