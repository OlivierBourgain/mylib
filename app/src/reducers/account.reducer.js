import {FETCH_CSRF, LOGIN, LOGOUT} from '../actions/account.action';
import {SUCCESS} from "./index";

export default function (state = {}, action) {
    switch (action.type) {
        case SUCCESS(FETCH_CSRF) : {
            return {
                ...state,
                csrfToken: action.payload.data
            }
        }
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
            return {
                ...state,
                logged: false
            }
        }
        default:
            return state;
    }
}
