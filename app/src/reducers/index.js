import {combineReducers} from 'redux';

import BookReducer from './book.reducer';
import ReadingReducer from './reading.reducer';
import TagReducer from './tag.reducer';
import StatReducer from './stat.reducer';
import UserReducer from './user.reducer';


/**
 * For Redux promise, generate action types for PENDING, SUCCESS and FAILURE
 */
export const PENDING = actionType => `${actionType}_PENDING`;
export const SUCCESS = actionType => `${actionType}_FULFILLED`;
export const FAILURE = actionType => `${actionType}_REJECTED`;


const rootReducer = combineReducers({
    book: BookReducer,
    reading: ReadingReducer,
    tag: TagReducer,
    stat: StatReducer,
    user: UserReducer
});

export default rootReducer;
