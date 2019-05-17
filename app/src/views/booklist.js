import React, {Component} from 'react'
import {bindActionCreators} from 'redux'
import {connect} from 'react-redux'
import {Link} from 'react-router-dom'
import {Col, Container, Row, Table, Input, Button} from 'reactstrap'

import Tag from './tag'
import {fetchBooks} from '../actions/book.action'
import Pagination from "./tools/pagination";

class BookList extends Component {
    state = {
        page: 0,
        size: 20,
        term: ''
    }

    constructor(props) {
        super(props);
        if (!this.props.book.list) this.props.fetchBooks(this.state.page, this.state.size, this.state.term);
    }

    render() {
        const {book} = this.props;
        if (book.error) {
            return (<Container>Something went wrong</Container>);
        }
        if (!book || book.pending) {
            return (<Container>Loading...</Container>);
        }
        return (<Container>
            <Row>
                <Col className="col-8">
                    <Row>
                        <Col className="col-8"><Input type="text" value={this.state.term} onChange={this.updateTerm}/></Col>
                        <Button type="button" color="success" className="col-2" onClick={this.searchTerm}>Submit</Button>
                    </Row>
                </Col>
                <Col className="col-4">
                    <div className="row">
                        <label htmlFor="showDisc">
                            <input type="checkbox" id="showDisc" /> Show discarded
                        </label>
                    </div>
                </Col>
            </Row>
            <h3>Your library</h3>
            {!book.list && <Row>
                <Col>No book found</Col>
            </Row>}
            {book.list && <>
                <Row>
                    <Col className="col-4" id="list-header-summary">
                        {book.list.size} results
                        {book.list.totalPages > 1 && <span>
                            , showing page {book.list.number + 1} of {book.list.totalPages}
                        </span>}
                    </Col>
                    <Col className="col-5">
                        <Pagination page={book.list.number} nbPages={book.list.totalPages} updatePage={this.updatePage}/>
                    </Col>
                    <Col className="col-3">
                        <span>Show{' '}</span>
                        <select value={this.state.size} onChange={this.changeSize}>
                            <option value={10}>10</option>
                            <option value={20}>20</option>
                            <option value={50}>50</option>
                            <option value={100}>100</option>
                            <option value={1000}>1000</option>
                        </select>
                        <span> books</span>
                    </Col>
                </Row>
                <Table bordered striped size="sm">
                    <thead>
                    <tr className="row">
                        <th className="col-5">Title</th>
                        <th className="col-3">Author</th>
                        <th className="col-1">Pages</th>
                        <th className="col-3">Tags</th>
                    </tr>
                    </thead>
                    <tbody>
                        {book.list.content && book.list.content.map(book => this.renderBook(book))}
                    </tbody>
                </Table>
            </>}
        </Container>);
    }

    searchTerm = () => {
        this.props.fetchBooks(this.state.page, this.state.size, this.state.term);
    }

    updateTerm = event => {
        this.setState({term: event.target.value});
    }

    changeSize = event => {
        this.setState({ size : event.target.value });
        this.props.fetchBooks(this.state.page, event.target.value, this.state.term);
    }

    updatePage = page => {
        this.setState({ page });
        this.props.fetchBooks(page, this.state.size, this.state.term);
    }


    renderBook(book) {
        return (
            <tr className="row" key={`book-${book.id}`}>
                <td className="col-5">
                    <Link to={`/book/${book.id}`}>{book.title}</Link>
                    {book.status === 'DISCARDED' && <span>{' '}(discarded)</span>}
                </td>
                <td className="col-3">{book.author}</td>
                <td className="col-1">{book.pages}</td>
                <td className="col-3">
                    {book.tags.map(tag => <Tag key={`tag-${tag.id}`} tag={tag}/>)}
                </td>
            </tr>);
    }


}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchBooks}, dispatch);
}

function mapStateToProps({book}) {
    return {book};
}

export default connect(mapStateToProps, mapDispatchToProps)(BookList);