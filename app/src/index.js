import React from 'react';
//import ReactDOM from 'react-dom';
import * as ReactDOM from 'react-dom/client';
import { Provider } from 'react-redux';
import { createStore, applyMiddleware } from 'redux';
import thunk from 'redux-thunk';
import promise from 'redux-promise-middleware';

import App from './App';
import reducers from './reducers';

import * as serviceWorker from './serviceWorker';

import 'bootstrap/dist/css/bootstrap.min.css';
import './index.css';
import './app.scss';

const createStoreWithMiddleware = applyMiddleware(promise, thunk)(createStore);
export const store = createStoreWithMiddleware(reducers);

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
    <React.StrictMode>
        <Provider store={store}>
            <App />
        </Provider>
    </React.StrictMode>
);

if (module.hot) {
    module.hot.accept('./App', () => {
        root.render(
            <React.StrictMode>
                <Provider store={store}>
                    <App />
                </Provider>
            </React.StrictMode>
        );
    })
}

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: http://bit.ly/CRA-PWA
serviceWorker.unregister();
