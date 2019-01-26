import React, { Component } from 'react';
import Header from './views/header';
import BookList from './views/book/booklist';
import './app.scss';

class App extends Component {
  render() {
    return (
      <div className="App">
        <Header />
        <BookList />
      </div>
    );
  }
}

export default App;
