export const LOGIN = 'LOGIN';
export const LOGOUT = 'LOGOUT';

export const login = response => {
    return {
        type: LOGIN,
        payload: {
            googleId: response.googleId,
            profileObj: response.profileObj,
            tokenObj: response.tokenObj
        }
    }
}

export const logout = () => {
    return {
        type: LOGOUT,
        payload: {}
    }
}