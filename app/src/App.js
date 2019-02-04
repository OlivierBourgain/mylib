import React, {Component} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from "react-redux";
import {GoogleLogin, GoogleLogout} from 'react-google-login';

import './app.scss';
import Header from './views/header';
import BookList from './views/book/booklist';
import {login, logout} from "./actions/account.action";

class App extends Component {

    login(response) {
        this.props.login(response)
    }

    error() {
        this.props.logout()
    }

    logout() {
        this.props.logout()
    }

    render() {
        const {logged} = this.props;

        return (
            <div className="App">
                {logged && <>
                    <GoogleLogout buttonText="Logout" onLogoutSuccess={this.logout.bind(this)}/>
                    <Header/>
                    <BookList/>
                </>}
                {!logged && <GoogleLogin
                    clientId="274726541955-21jeen018spaeumspmifv18hgomju0r9.apps.googleusercontent.com"
                    buttonText="Login"
                    onSuccess={this.login.bind(this)}
                    onFailure={this.error.bind(this)}
                />
                }
            </div>
        );
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({login, logout}, dispatch);
}

function mapStateToProps(state) {
    console.log(state);
    return {
        logged: state.account.logged
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(App);
