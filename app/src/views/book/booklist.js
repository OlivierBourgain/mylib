import React, {Component} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import {Col, Container, Row, Table} from 'reactstrap';
import Tag from './tag';

import {fetchBooks} from '../../actions/book.action';

class BookList extends Component {

    constructor(props) {
        super(props);
        this.props.fetchBooks();
    }

    render() {
        const {books} = this.props;
        return (
            <Container>
                <Row>
                    <Col>
                        <h3>Your library</h3>
                    </Col>
                </Row>
                {books === undefined && <Row>
                    <Col>Loading...</Col>
                </Row>}
                {books === [] && <Row>
                    <Col>No book found</Col>
                </Row>}
                {books && <Row>
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
                            {books && books.map(book => (
                                <tr key={`book-${book.id}`}>
                                    <td>{book.title}</td>
                                    <td>{book.author}</td>
                                    <td>{book.pages}</td>
                                    <td>
                                        {book.tags.map(tag =>
                                            <Tag key={`tag-${tag.id}`} tag={tag}/>
                                        )}
                                    </td>
                                </tr>))
                            }
                            </tbody>
                        </Table>
                    </Col>
                </Row>}
            </Container>);
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchBooks}, dispatch);
}

function mapStateToProps({books}) {
    return {
        books: books.data
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(BookList);