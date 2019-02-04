import {LOGIN, LOGOUT} from '../actions/account.action';

export default function (state = {}, action) {
    switch (action.type) {
        case LOGIN: {
            return {
                logged: true,
                googleId: action.payload.googleId,
                profileObj: action.payload.profileObj,
                tokenObj: action.payload.tokenObj
            };
        }
        case LOGOUT: {
            return {
                logged: false
            }
        }
        default:
            return state;
    }
}
