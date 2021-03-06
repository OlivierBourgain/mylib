import React, {Component} from 'react'
import {Link} from 'react-router-dom'
import {Redirect} from 'react-router';
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import moment from 'moment'

import {fetchTags} from '../actions/tag.action'
import {fetchBook, updateBook, deleteBook, updateDiscard} from '../actions/book.action'
import {fetchBookReadings} from '../actions/reading.action'

import {Button, Col, Container, FormGroup, Label, Row} from 'reactstrap'
import {Field, Form, Formik} from 'formik';

import CreatableSelect from 'react-select/creatable';

/**
 * This page is the book detail. It is a form, with two actions: update and delete.
 * Both actions lead back to the book list.
 *
 * Props
 * - id: the book id
 * // Mapped from global state
 * - book.detail: The book object
 * - book.redirectTo: This property is set after an action, indicates the page we should redirect to (typically /books)
 * - tag.list: List of tags for the tag selector
 */
class BookDetail extends Component {
    state = {
        selectedTags: null,
        tagUpdate: false   // Indicate if the tags have changed.
    }

    constructor(props) {
        super(props);
        this.props.fetchBook(props.id);
        this.props.fetchTags();
        this.props.fetchBookReadings(props.id);
    }

    handleSubmit = values => {
        if (this.state.tagUpdate)
            values.tagString = this.state.selectedTags ? this.state.selectedTags.map(tag => tag.value).join(",") : '';
        else
            values.tagString = this.props.book.detail.tags.map(tag => tag.text).join(",");
        this.props.updateBook(values);
    }

    tagChange = values => {
        this.setState({selectedTags: values, tagUpdate: true});
    }

    deleteBook = (id) => { if (window.confirm('Are you sure?')) this.props.deleteBook(id); }
    discardBook = (id) => { this.props.updateDiscard(id, true); }
    undiscardBook = (id) => { this.props.updateDiscard(id, false); }

    format = (dateStr) => { return moment(dateStr).format("MMMM YYYY"); }

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
        const imgUrl = process.env.NODE_ENV === 'development' ? `http://localhost:2017/store/${book.detail.largeImage}`:`/store/${book.detail.largeImage}`;

        // Styling the tag list
        const customStyles = this.tagListStyle();

        const nbReading = this.props.readings && this.props.readings.length;
        const lastReading = (!this.props.readings || nbReading === 0) ? "" : this.format(this.props.readings[this.props.readings.length - 1].date);

        return <Container>
            <Row>
                <Link className="col-12" to="/books">&lt; back to list</Link>
            </Row>
            <Row>
                <h3 className="col-12">{book.detail.title} {book.detail.status === 'DISCARDED' && <span>(Discarded)</span>}</h3>
            </Row>
            {nbReading === 1 && <Row>
                <div className="col-12 alert alert-success">
                    You've read this book in {lastReading}.
                </div>
            </Row>
            }
            {nbReading > 1 && <Row>
                <div className="col-12 alert alert-success">
                    You've read this book {this.props.readings.length} times. Last time was in {lastReading}.
                </div>
            </Row>
            }
            <Row id="book-detail">
                <Col className="col-12 col-md-4 order-2 order-md-1 mt-4">
                    <img src={imgUrl} alt="Book cover"/>
                </Col>
                <Col className="col-12 col-md-8 order-1 order-md-2">
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
                                        {book.detail.status === 'DISCARDED' &&
                                            <Button className="btn btn-info mr-3"
                                                    onClick={() => this.undiscardBook(book.detail.id)}>Undiscard</Button>
                                        }
                                        {book.detail.status !== 'DISCARDED' &&
                                            <Button className="btn btn-info mr-3"
                                                    onClick={() => this.discardBook(book.detail.id) }>Discard</Button>
                                        }
                                        <Button className="btn btn-danger mr-1"
                                                onClick={() => this.deleteBook(book.detail.id) }>Delete
                                        </Button>
                                        <Button type="submit" disabled={isSubmitting}
                                                color="success">Submit</Button>
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
                            </Form>
                        )}
                    </Formik>
                </Col>
            </Row>
            <Row className="col-12">
                Created {moment(book.detail.created).format('DD/MM/YYYY h:mm:ss')}
            </Row>
            <Row className="col-12">
                Updated {moment(book.detail.updated).format('DD/MM/YYYY h:mm:ss')}
            </Row>
        </Container>
    }

    tagListStyle() {
        return {
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
                        fontSize: '120%'
                    },
                }
            },
            dropdownIndicator: (style) => {
                return {display: 'none'}
            },
            indicatorSeparator: (style) => {
                return {display: 'none'}
            }
        };
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchBook, fetchBookReadings, updateBook, fetchTags, deleteBook, updateDiscard}, dispatch);
}

function mapStateToProps(state) {
    return {
        book: state.book,
        tag: state.tag,
        readings: state.reading.bookreadings
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(BookDetail);