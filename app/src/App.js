import React, {Component} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from "react-redux";
import {GoogleLogin} from 'react-google-login';

import Header from './views/header';
import BookList from './views/booklist';
import BookDetail from './views/bookdetail';
import ReadingList from './views/readinglist';
import TagList from './views/taglist';
import UserList from './views/userlist';
import Stats from './views/stats';
import {login, logout} from "./actions/user.action";
import { BrowserRouter as Router, Route } from "react-router-dom";

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
        const {logged, role} = this.props;

        return (
            <div className="App">
                {logged && role && <Router>
                    <Header role={role}/>
                    <Route exact path="/book/:id" render={props => <BookDetail id={props.match.params.id} />}/>
                    <Route exact path="/books" render={() => <BookList/>} />
                    <Route exact path="/readings" render={() => <ReadingList/>} />
                    <Route exact path="/stats" render={() => <Stats/>} />
                    <Route exact path="/tags" render={() => <TagList/>} />
                    {role === 'ADMIN' && <Route exact path="/admin" render={() => <UserList/>}/>}
                </Router>}
                {!logged && <>
                    <GoogleLogin
                        clientId="274726541955-21jeen018spaeumspmifv18hgomju0r9.apps.googleusercontent.com"
                        buttonText="Login"
                        onSuccess={this.login.bind(this)}
                        onFailure={this.error.bind(this)}
                    />
                </>
                }
            </div>
        );
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({login, logout}, dispatch);
}

function mapStateToProps(state) {
    return {
        logged: state.user.logged,
        role: state.user.role
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(App);
