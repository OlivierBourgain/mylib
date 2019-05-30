import React, {Component} from 'react'
import {fetchBook, updateBook} from '../actions/book.action'
import {Button, Col, Container, FormGroup, Label, Row} from 'reactstrap'
import {Link} from 'react-router-dom'
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import {Field, Form, Formik} from 'formik';

class BookDetail extends Component {

    constructor(props) {
        super(props);
        this.props.fetchBook(props.id);
    }

    handleSubmit = values => {
        this.props.updateBook(values);
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
                    <img src={imgUrl} alt="Book cover"/>
                </Col>
                <Col className="col-8">
                    <Formik
                        initialValues={book.detail}
                        onSubmit={(values, {setSubmitting}) => {
                            setTimeout(() => {
                                this.handleSubmit(values);
                                setSubmitting(false);
                            }, 400);
                        }}>
                        {({isSubmitting}) => (
                            <Form>
                                <FormGroup row>
                                    <div className="text-right col-12">
                                        <Button type="submit" disabled={isSubmitting}
                                                color="success">Submit</Button>{' '}
                                    </div>
                                </FormGroup>
                                <FormGroup row>
                                    <Label for="title" sm={3}>Title</Label>
                                    <Col sm={9}>
                                        <Field type="text" name="title" className="form-control"/>
                                    </Col>
                                </FormGroup>
                                <FormGroup row>
                                    <Label for="subtitle" sm={3}>Subtitle</Label>
                                    <Col sm={9}>
                                        <Field type="text" name="subtitle" className="form-control"/>
                                    </Col>
                                </FormGroup>
                                <FormGroup row>
                                    <Label for="author" sm={3}>Author</Label>
                                    <Col sm={9}>
                                        <Field type="text" name="author" className="form-control"/>
                                    </Col>
                                </FormGroup>
                                <FormGroup row>
                                    <Label for="isbn" sm={3}>ISBN</Label>
                                    <Col sm={9}>
                                        <Field type="text" name="isbn" className="form-control"/>
                                    </Col>
                                </FormGroup>
                                <FormGroup row>
                                    <Label for="publisher" sm={3}>Publisher</Label>
                                    <Col sm={9}>
                                        <Field type="text" name="publisher" className="form-control"/>
                                    </Col>
                                </FormGroup>
                                <FormGroup row>
                                    <Label for="pages" sm={3}>Publication date</Label>
                                    <Col sm={9}>
                                        <Field type="text" name="publicationDate" className="form-control"/>
                                    </Col>
                                </FormGroup>
                                <FormGroup row>
                                    <Label for="pages" sm={3}>Pages</Label>
                                    <Col sm={9}>
                                        <Field type="text" name="pages" className="form-control"/>
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
                                        <Field type="text" name="comment" className="form-control"/>
                                    </Col>
                                </FormGroup>
                                <FormGroup row>
                                    <Label for="lang" sm={3}>Language</Label>
                                    <Col sm={9}>
                                        <Field type="text" name="lang" className="form-control"/>
                                    </Col>
                                </FormGroup>

                            </Form>
                        )}
                    </Formik>
                </Col>
            </Row>
        </Container>
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchBook, updateBook}, dispatch);
}

function mapStateToProps(state) {
    console.log("State", state);
    return state.book;
}

export default connect(mapStateToProps, mapDispatchToProps)(BookDetail);