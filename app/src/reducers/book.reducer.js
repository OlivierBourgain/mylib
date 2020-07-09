import {PENDING, SUCCESS, FAILURE} from './index.js';
import {FETCH_BOOKS, FETCH_BOOK_TITLES, FETCH_BOOK, LOOKUP_BOOK, DELETE_BOOK, UPDATE_BOOK, EXPORT_BOOKS} from '../actions/book.action';

export default function (state = {}, action) {
    switch (action.type) {
        case PENDING(FETCH_BOOKS):
        case PENDING(FETCH_BOOK):
        case PENDING(DELETE_BOOK):
        case PENDING(UPDATE_BOOK):
        case PENDING(LOOKUP_BOOK):{
            return {...state, pending: true, error:false, redirectTo: undefined};
        }
        case FAILURE(FETCH_BOOKS):
        case FAILURE(FETCH_BOOK):
        case FAILURE(UPDATE_BOOK):
        case FAILURE(DELETE_BOOK):
        case FAILURE(LOOKUP_BOOK):{
            return {...state, pending: false, error: true, redirectTo: undefined};
        }
        case SUCCESS(LOOKUP_BOOK): {
            return {...state, pending: false, error: false, redirectTo: '/book/' + action.payload.data.id};
        }
        case SUCCESS(FETCH_BOOKS): {
            return {...state, pending: false, error: false, list: action.payload.data, redirectTo: undefined};
        }
        case SUCCESS(FETCH_BOOK_TITLES): {
            // Transform the result
            // from  {1: "Fondation", 2: "Dune"}
            // to    [{ value: '1', label: 'Fondation }, { value: '2', label: 'Dune'}]
            const titleList = [];
            for (const item in action.payload.data) {
                titleList.push({ value: item, label: action.payload.data[item]})
            }
            titleList.sort((a, b) => a.label.localeCompare(b.label));
            return {...state, titlelist: titleList };
        }
        case SUCCESS(UPDATE_BOOK): {
            return {...state, pending: false, error: false, redirectTo: "/books"};
        }
        case SUCCESS(FETCH_BOOK): {
            return {...state, pending: false, error: false, detail: action.payload.data, redirectTo: undefined};
        }
        case SUCCESS(DELETE_BOOK): {
            return {...state, pending: false, error: false, detail: action.payload.data, redirectTo: "/books"};
        }
        case SUCCESS(EXPORT_BOOKS): {
            const a =  document.createElement("a");
            document.body.appendChild(a);
            a.style = "display: none";
            const blob = new Blob([action.payload.data], {type: "octet/stream"});
            const url = window.URL.createObjectURL(blob);
            a.href = url;
            a.download = "export.csv";
            a.click();
            window.URL.revokeObjectURL(url);
            return state;
        }
        default:
            return state;
    }
}

