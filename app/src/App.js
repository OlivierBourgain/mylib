import React, {Component} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from "react-redux";
import {GoogleLogin} from 'react-google-login';
import {useParams} from "react-router-dom";
import Header from './views/header';
import BookList from './views/booklist';
import BookDetail from './views/bookdetail';
import ReadingList from './views/readinglist';
import TagList from './views/taglist';
import UserList from './views/userlist';
import Stats from './views/stats';
import {login, logout} from "./actions/user.action";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

const BookDetailWrapper = () => {
    const { id } = useParams();
    console.log("Here", id)
    return <BookDetail id={id} />;
};

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
                    <Routes>
                        <Route exact path="/book/:id" element={<BookDetailWrapper />}/>
                        <Route exact path="/books" element={<BookList/>} />
                        <Route exact path="/readings" element={<ReadingList/>} />
                        <Route exact path="/stats" element={<Stats/>} />
                        <Route exact path="/tags" element={<TagList/>} />
                    {role === 'ADMIN' && <Route exact path="/admin" element={<UserList/>}/>}
                    </Routes>
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
