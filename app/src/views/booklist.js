import React, {Component} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import {Col, Container, Row, Table} from 'reactstrap';
import Tag from './tag';

import {fetchBooks} from '../actions/book.action';

class BookList extends Component {

    constructor(props) {
        super(props);
        if (!this.props.books.data) this.props.fetchBooks();
    }

    render() {
        const {books} = this.props;
        if (books.error) {
            return (<Row><Col>Something went wrong</Col></Row>);
        }
        if (books.pending) {
            return (<Row><Col>Loading...</Col></Row>);
        }
        return (<Container>
            <Row>
                <Col>
                    <h3>Your library</h3>
                </Col>
            </Row>
            {!books.data && <Row>
                <Col>No book found</Col>
            </Row>}
            {books.data && <>
                <Row>
                    <Col>{books.data.length} results</Col>
                </Row>
                <Row>
                    <Col>
                        <Table bordered striped size={"sm"}>
                            <thead>
                            <tr>
                                <th>Title</th>
                                <th>Author</th>
                                <th>Pages</th>
                                <th>Tags</th>
                            </tr>
                            </thead>
                            <tbody>
                                {books.data && books.data.map(book => this.renderBook(book))}
                            </tbody>
                        </Table>
                    </Col>
                </Row>
            </>}
        </Container>);
    }

    renderBook(book) {
        return (
            <tr key={`book-${book.id}`}>
                <td>
                    {book.title}
                    {book.status === 'DISCARDED' && <span>{' '}(discarded)</span>}
                </td>
                <td>{book.author}</td>
                <td>{book.pages}</td>
                <td>
                    {book.tags.map(tag => <Tag key={`tag-${tag.id}`} tag={tag}/>)}
                </td>
            </tr>);
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchBooks}, dispatch);
}

function mapStateToProps({books}) {
    return {books};
}

export default connect(mapStateToProps, mapDispatchToProps)(BookList);