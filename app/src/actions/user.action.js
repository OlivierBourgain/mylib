import axios from "axios";
import {store} from "../index";
import {ROOT_URL} from "./index";

export const FETCH_USERS = 'FETCH_USERS';
export const FETCH_ROLE = 'FETCH_ROLE';
export const CREATE_USER = 'CREATE_USER';
export const DELETE_USER = 'DELETE_USER';
export const UPDATE_USER = 'UPDATE_USER';

export const LOGIN = 'LOGIN';
export const LOGOUT = 'LOGOUT';

export const login = (response) => {
    return (dispatch, getState) => {
        const url = `${ROOT_URL}/user?email=${response.profileObj.email}`;
        const idToken = response.tokenObj.id_token;

        dispatch({
            type: FETCH_ROLE,
            payload: axios.get(url, {headers: {"Authorization": `Bearer ${idToken}`}})
        }).then(dispatch({
                type: LOGIN,
                payload: {
                    googleId: response.googleId,
                    profileObj: response.profileObj,
                    tokenObj: response.tokenObj
                }
            }
            )
        );
    }
}

export const logout = () => {
    return {
        type: LOGOUT,
        payload: {}
    }
}

export const fetchUsers = () => {
    const url = `${ROOT_URL}/users`;
    const idToken = store.getState().user.tokenObj.id_token;
    const request = axios.get(url, {headers: {"Authorization": `Bearer ${idToken}`}});

    return {
        type: FETCH_USERS,
        payload: request
    }
}

export const createUser = (email) => {
    return (dispatch, getState) => {
        const url = `${ROOT_URL}/user`;
        const idToken = store.getState().user.tokenObj.id_token;

        dispatch({
            type: CREATE_USER,
            payload: axios.post(url, `email=${email}`, {headers: {"Authorization": `Bearer ${idToken}`}})
        }).then(() => {
            dispatch(fetchUsers());
        });
    }
}

export const deleteUser = (id) => {
    const url = `${ROOT_URL}/user/${id}`;
    const idToken = store.getState().user.tokenObj.id_token;
    const request = axios.delete(url, {headers: {"Authorization": `Bearer ${idToken}`}});

    return {
        type: DELETE_USER,
        payload: request
    }
}

export const updateRole = (id, role) => {
    return (dispatch, getState) => {
        const url = `${ROOT_URL}/user/${id}`;
        const idToken = store.getState().user.tokenObj.id_token;

        dispatch({
            type: UPDATE_USER,
            payload: axios.post(url, `role=${role}`, {headers: {"Authorization": `Bearer ${idToken}`}})
        }).then(() => {
            dispatch(fetchUsers());
        });
    }
}