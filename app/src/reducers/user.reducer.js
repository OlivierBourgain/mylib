import {FAILURE, PENDING, SUCCESS} from './index.js';
import {DELETE_USER, FETCH_USERS, FETCH_ROLE, LOGIN, LOGOUT} from '../actions/user.action';

export default function (state = {}, action) {
    switch (action.type) {
        case LOGIN: {
            return {
                ...state,
                logged: true,
                googleId: action.payload.googleId,
                profileObj: action.payload.profileObj,
                tokenObj: action.payload.tokenObj,
            };
        }
        case LOGOUT: {
            return {...state, logged: false}
        }
        case SUCCESS(FETCH_ROLE): {
            return {...state, role: action.payload.data.role};
        }
        case FAILURE(FETCH_ROLE): {
            return {...state, logged: false};
        }
        case PENDING(FETCH_USERS): {
            return {...state, pending: true, error: false};
        }
        case SUCCESS(DELETE_USER):
        case SUCCESS(FETCH_USERS): {
            return {...state, pending: false, error: false, list: action.payload.data};
        }
        case FAILURE(FETCH_USERS): {
            return {...state, pending: false, error: true};
        }
        default:
            return state;
    }
}
