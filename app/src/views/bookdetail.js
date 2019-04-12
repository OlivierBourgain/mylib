import React, {Component} from 'react'
import {fetchBook} from '../actions/book.action'
import {Col, Container, Row} from 'reactstrap'
import {Link} from 'react-router-dom'
import {bindActionCreators} from "redux";
import {connect} from "react-redux";

class BookDetail extends Component {

    constructor(props) {
        super(props);
        this.props.fetchBook(props.id);
    }

    render() {
        const book = this.props;

        if (book.pending || !book.detail || book.detail === {}) {
            return (<Container>Loading...</Container>);
        }
        return <Container>
            <Row id="book-detail">
                <Col className="col-12">
                <Row>
                    <Link to="/books">&lt; back to list</Link>
                </Row>
                <Row>
                    <h3>{book.detail.title}</h3>
                </Row>
                <Row>
                    <label className="col-sm-2 col-form-label">Title</label>
                    <div className="col-sm-10">
                        {book.detail.title}
                    </div>
                </Row>
                <Row>
                    <label className="col-sm-2 col-form-label">Subtitle</label>
                    <div className="col-sm-10">
                        {book.detail.subtitle}
                    </div>
                </Row>
                <Row>
                    <label className="col-sm-2 col-form-label">Author</label>
                    <div className="col-sm-10">
                        {book.detail.author}
                    </div>
                </Row>
                <Row>
                    <label className="col-sm-2 col-form-label">ISBN</label>
                    <div className="col-sm-10">
                        {book.detail.isbn}
                    </div>
                </Row>
                <Row>
                    <label className="col-sm-2 col-form-label">Editor</label>
                    <div className="col-sm-10">
                        {book.detail.publisher}
                    </div>
                </Row>
                <Row>
                    <label className="col-sm-2 col-form-label">Pages</label>
                    <div className="col-sm-10">
                        {book.detail.pages}
                    </div>
                </Row>
                </Col>
            </Row>
        </Container>
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchBook}, dispatch);
}

function mapStateToProps(state) {
    return state.book;
}

export default connect(mapStateToProps, mapDispatchToProps)(BookDetail);