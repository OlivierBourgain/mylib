import { FETCH_BOOKS } from '../actions/index';

export default function(state = {}, action) {
    switch(action.type) {
        case FETCH_BOOKS: {
            return {
                data:  action.payload.data,
            };
        }
        default:
            return state;
    }
}
