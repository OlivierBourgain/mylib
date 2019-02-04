import {combineReducers} from 'redux';

import BookReducer from './book.reducer';
import AccountReducer from './account.reducer';

const rootReducer = combineReducers({
    books: BookReducer,
    account: AccountReducer
});

export default rootReducer;
