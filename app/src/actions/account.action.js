export const LOGIN = 'LOGIN';
export const LOGOUT = 'LOGOUT';


export function login(response) {
    return {
        type: LOGIN,
        payload: {
            googleId: response.googleId,
            profileObj: response.profileObj,
            tokenObj: response.tokenObj
        }
    }
}

export function logout() {
    return {
        type: LOGOUT,
        payload: {}
    }
}