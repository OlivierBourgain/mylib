import {combineReducers} from 'redux';

import BookReducer from './book.reducer';
import ReadingReducer from './reading.reducer';
import AccountReducer from './account.reducer';

/**
 * For Redux promise, generate action types for PENDING, SUCCESS and FAILURE
 */
export const PENDING = actionType => `${actionType}_PENDING`;
export const SUCCESS = actionType => `${actionType}_FULFILLED`;
export const FAILURE = actionType => `${actionType}_REJECTED`;


const rootReducer = combineReducers({
    book: BookReducer,
    reading: ReadingReducer,
    account: AccountReducer
});

export default rootReducer;
