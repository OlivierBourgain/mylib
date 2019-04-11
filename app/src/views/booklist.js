import React, {Component} from 'react'
import {bindActionCreators} from 'redux'
import {connect} from 'react-redux'
import {Link} from 'react-router-dom'
import {Col, Container, Row, Table} from 'reactstrap'

import Tag from './tag'
import {fetchBooks} from '../actions/book.action'

class BookList extends Component {

    constructor(props) {
        super(props);
        if (!this.props.book.list) this.props.fetchBooks();
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
            <Row id="book-list">
                <h3>Your library</h3>
                {!book.list && <Row className="col-12">
                    <Col>No book found</Col>
                </Row>}
                {book.list && <>
                    <Row className="col-12">
                        <Col>{book.list.length} results</Col>
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
                            {book.list && book.list.map(book => this.renderBook(book))}
                        </tbody>
                    </Table>
                </>}
            </Row>
        </Container>);
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