import React, {Component} from 'react'
import {fetchBook} from '../actions/book.action'
import {Col, Container, Row, Form, FormGroup, Label, Input, Button} from 'reactstrap'
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

        const imgUrl = `http://localhost:2017/store/${book.detail.largeImage}`;

        return <Container>
            <Row>
                <Link to="/books">&lt; back to list</Link>
            </Row>
            <Row>
                <h3>{book.detail.title}</h3>
            </Row>
            <Row id="book-detail">
                <Col className="col-4">
                    <img src={imgUrl} alt="Book cover" />
                </Col>
                <Col className="col-8">
                <Form>
                    <FormGroup row>
                        <div className="text-right col-12">
                            <Button type="submit" color="success" >Submit</Button>{' '}
                        </div>
                    </FormGroup>
                    <FormGroup row>
                        <Label for="title" sm={3}>Title</Label>
                        <Col sm={9}>
                            <Input type="text" name="title" value={book.detail.title} />
                        </Col>
                    </FormGroup>
                    <FormGroup row>
                        <Label for="subtitle" sm={3}>Subtitle</Label>
                        <Col sm={9}>
                            <Input type="text" name="subtitle" value={book.detail.subtitle} />
                        </Col>
                    </FormGroup>
                    <FormGroup row>
                        <Label for="author" sm={3}>Author</Label>
                        <Col sm={9}>
                            <Input type="text" name="author" value={book.detail.author} />
                        </Col>
                    </FormGroup>
                    <FormGroup row>
                        <Label for="isbn" sm={3}>ISBN</Label>
                        <Col sm={9}>
                            <Input type="text" name="isbn" value={book.detail.isbn} />
                        </Col>
                    </FormGroup>
                    <FormGroup row>
                        <Label for="publisher" sm={3}>Publisher</Label>
                        <Col sm={9}>
                            <Input type="text" name="publisher" value={book.detail.publisher} />
                        </Col>
                    </FormGroup>
                    <FormGroup row>
                        <Label for="pages" sm={3}>Publication date</Label>
                        <Col sm={9}>
                            <Input type="text" name="publicationDate" value={book.detail.publicationDate} />
                        </Col>
                    </FormGroup>
                    <FormGroup row>
                        <Label for="pages" sm={3}>Pages</Label>
                        <Col sm={9}>
                            <Input type="text" name="pages" value={book.detail.pages} />
                        </Col>
                    </FormGroup>
                    <FormGroup row>
                        <Label for="tagString" sm={3}>Tags</Label>
                        <Col sm={9}>
                            TODO
                        </Col>
                    </FormGroup>
                    <FormGroup row>
                        <Label for="comment" sm={3}>Comment</Label>
                        <Col sm={9}>
                            <Input type="text" name="comment" value={book.detail.comment} />
                        </Col>
                    </FormGroup>
                    <FormGroup row>
                        <Label for="lang" sm={3}>Language</Label>
                        <Col sm={9}>
                            <Input type="text" name="lang" value={book.detail.lang} />
                        </Col>
                    </FormGroup>

                    </Form>
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