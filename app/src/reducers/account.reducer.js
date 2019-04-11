import {LOGIN, LOGOUT} from '../actions/account.action';

export default function (state = {}, action) {
    switch (action.type) {
        case LOGIN: {
            return {
                ...state,
                logged: true,
                googleId: action.payload.googleId,
                profileObj: action.payload.profileObj,
                tokenObj: action.payload.tokenObj
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
