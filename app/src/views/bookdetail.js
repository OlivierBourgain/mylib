import React, {Component} from 'react'
import {Link} from 'react-router-dom'
import {Redirect} from 'react-router';
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import moment from 'moment'

import {fetchTags} from '../actions/tag.action'
import {fetchBook, updateBook, deleteBook} from '../actions/book.action'

import {Button, Col, Container, FormGroup, Label, Row} from 'reactstrap'
import {Field, Form, Formik} from 'formik';

import CreatableSelect from 'react-select/creatable';

class BookDetail extends Component {
    state = {
        selectedTags: null,
    }

    constructor(props) {
        super(props);
        this.props.fetchBook(props.id);
        this.props.fetchTags();
    }

    handleSubmit = values => {
        values.tagString = this.state.selectedTags ? this.state.selectedTags.map(tag => tag.value).join(",") : "";
        this.props.updateBook(values);
    }

    tagChange = values => {
        this.setState({selectedTags: values});
    }

    deleteBook = (id) => {
        this.props.deleteBook(id);
    }

    render() {
        const book = this.props.book;

        if (book.redirectTo) {
            return (<Redirect to={book.redirectTo}/>)
        }

        if (book.pending || !book.detail || book.detail === {}) {
            return (<Container>Loading...</Container>);
        }

        const allTags = this.props.tag.pending ? [] : this.props.tag.list.map(tag => ({
            value: tag.text,
            label: tag.text
        }));
        const bookTags = book.detail.tags ? book.detail.tags.map(tag => ({value: tag.text, label: tag.text})) : [];
        const imgUrl = `http://localhost:2017/store/${book.detail.largeImage}`;

        // Styling the tag list
        const customStyles = {
            multiValue: (styles, {data}) => {
                const tag = this.props.tag.list ? this.props.tag.list.filter(tag => tag.text === data.label) : null;
                if (!tag || tag.length === 0) return {
                    ...styles,
                    border: '1px solid black',
                    borderRadius: '4px'
                }
                else return {
                    ...styles,
                    backgroundColor: tag[0].backgroundColor,
                    border: '1px solid ' + tag[0].borderColor,
                    borderRadius: '4px'
                };
            },
            multiValueLabel: (styles, {data}) => {
                const tag = this.props.tag.list ? this.props.tag.list.filter(tag => tag.text === data.label) : null;
                if (!tag || tag.length === 0) return {
                    ...styles,
                    fontSize: '1em',
                    padding: '0px'
                }
                else return {
                    ...styles,
                    color: tag[0].color,
                    fontSize: '1em',
                    padding: '0px'
                };
            },
            multiValueRemove: (styles, {data}) => {
                const tag = this.props.tag.list ? this.props.tag.list.filter(tag => tag.text === data.label) : null;
                if (!tag || tag.length === 0) return {
                    ...styles,
                    ':hover': {// Clear background color
                        }
                }

                return {
                    ...styles,
                    color: tag[0].color,
                    ':hover': {
                        backgroundColor: tag[0].backgroungColor,
                        color: tag[0].color,
                        fontWeight: 'bolder',
                        fontSize:'120%'
                    },
                }
            },
            dropdownIndicator: (style) => { return {display: 'none'} },
            indicatorSeparator: (style) => { return { display: 'none'} }
        }

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
                                        <Button className="btn btn-danger"
                                                onClick={() => { if (window.confirm('Are you sure?')) this.deleteBook(book.detail.id) }}>Delete
                                        </Button>{' '}
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
                                    <Label for="tagString" sm={3}>Tags</Label>
                                    <Col sm={9}>
                                        <CreatableSelect
                                            isMulti={true}
                                            isClearable={false}
                                            defaultValue={bookTags}
                                            placeHolder=""
                                            options={allTags}
                                            styles={customStyles}
                                            onChange={this.tagChange}
                                        />
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
                                <FormGroup row>
                                    <Label for="lang" sm={3}>Creation date</Label>
                                    <Col sm={9} className="col-form-label">
                                        {moment(book.detail.created).format('DD/MM/YYYY h:mm:ss')}
                                    </Col>
                                </FormGroup>
                                <FormGroup row>
                                    <Label for="lang" sm={3}>Update date</Label>
                                    <Col sm={9} className="col-form-label">
                                        {moment(book.detail.updated).format('DD/MM/YYYY h:mm:ss')}
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
    return bindActionCreators({fetchBook, updateBook, fetchTags, deleteBook}, dispatch);
}

function mapStateToProps(state) {
    return {
        book: state.book,
        tag: state.tag
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(BookDetail);