import React, {Component} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import {Button, Col, Container, Input, Row, Table} from 'reactstrap';
import {Link} from "react-router-dom";
import Select, {createFilter} from "react-select";
import Pagination from "./tools/pagination";

import {addReading, deleteReading, fetchReadings} from '../actions/reading.action';
import {fetchBookTitles} from '../actions/book.action';

/**
 * State: page, size, sort
 * Props: reading{error, pending, list}, booktitles{list}
 */
class ReadingList extends Component {
    state = {
        page: 0,
        size: 100,
        sort: 'date',
        descending: true,
        selectedBook: undefined,
        selectedDate: new Date().toISOString().split('T')[0]
    }

    componentDidMount() {
        this.updateList()
        this.props.fetchBookTitles();
    }

    updateList = () => {
        this.props.fetchReadings(
            this.state.page,
            this.state.size,
            this.state.sort,
            this.state.descending);
    }

    changeSize = event => {
        this.setState({size: event.target.value}, this.updateList);
    }

    changePage = page => {
        this.setState({page}, this.updateList);
    }

    changeSort = col => {
        if (this.state.sort === col) this.setState({descending: !this.state.descending}, this.updateList)
        else this.setState({sort: col}, this.updateList)
    }

    delete = (reading) => {
        this.props.deleteReading(reading);
    }

    addReading = (e) => {
        e.preventDefault();
        if (!this.state.selectedBook || !this.state.selectedDate) return;
        this.props.addReading(this.state.selectedBook, this.state.selectedDate);
    }

    render() {
        const reading = this.props.reading;
        const titlelist = this.props.book.titlelist;
        // Get today's date formatted yyyy-mm-dd
        const today = new Date().toISOString().split('T')[0];

        if (reading.error) {
            return (<Container>Something went wrong</Container>);
        }
        if (reading.pending) {
            return (<Container>Loading...</Container>);
        }

        const list = this.props.reading.list;
        return (<Container>
            <Row className="filter-bar">
                <Col className="col-12">
                    <form onSubmit={this.addReading}>
                        <Row>
                            <Col className="col-12 col-md-5 pb-2 pb-md-0">
                                <Select name="title" filterOption={createFilter({ignoreAccents: false})}
                                        options={titlelist}
                                        onChange={(book) => this.setState({selectedBook: book.value})}/>
                            </Col>
                            <Col className="col-6 col-md-3">
                                <Input name="dateread" type="date" defaultValue={today}
                                       onChange={(e) => this.setState({selectedDate: e.target.value})}/>
                            </Col>
                            <Col className="col-6 col-md-3"><Button className="btn btn-success">Add</Button></Col>
                        </Row>
                    </form>
                </Col>
            </Row>

            {!list && <Row className="col-12">
                <Col>No book read</Col>
            </Row>}
            {list && <>
                <Row>
                    <Col className="col-12 col-md-4">
                        {list.totalElements} lines
                        {list.totalPages > 1 && <span>
                            , showing page {list.number + 1} of {list.totalPages}
                            </span>}
                    </Col>
                    <Col className="col-12 col-md-5">
                        <Pagination path="/readings" page={list.number} nbPages={list.totalPages}
                                    updatePage={this.changePage}/>
                    </Col>
                    <Col className="col-12 col-md-3">
                        <span>Show{' '}</span>
                        <select value={this.state.size} onChange={this.changeSize}>
                            <option value={10}>10</option>
                            <option value={20}>20</option>
                            <option value={50}>50</option>
                            <option value={100}>100</option>
                            <option value={1000}>1000</option>
                        </select>
                        <span> lines</span>
                    </Col>
                </Row>
                <Row className="mt-2">
                    <Table bordered striped size="sm">
                        <thead>
                        <tr className="row">
                            <th className="col-5 col-md-5">
                                <Link to='/readings' onClick={() => this.changeSort('Book.title')}>Title</Link>
                            </th>
                            <th className="col-4 col-md-3">
                                <Link to='/readings' onClick={() => this.changeSort('Book.author')}>Author</Link>
                            </th>
                            <th className="d-none d-md-block col-md-1">
                                <Link to='/readings' onClick={() => this.changeSort('Book.pages')}>Pages</Link>
                            </th>
                            <th className="col-3 col-md-2">
                                <Link to='/readings' onClick={() => this.changeSort('Date')}>Date read</Link>
                            </th>
                            <th className="d-none d-md-block col-md-1"></th>
                        </tr>
                        </thead>
                        <tbody>
                        {list.content && list.content.map(it => this.renderReading(it))}
                        </tbody>
                    </Table>
                </Row>
            </>}

        </Container>);
    }

    renderReading = (reading) => {
        return (
            <tr className="row" key={`reading-${reading.id}`}>
                <td className="col-5 col-md-5"><Link to={`/book/${reading.book.id}`}>{reading.book.title}</Link></td>
                <td className="col-4 col-md-3">{reading.book.author}</td>
                <td className="d-none d-md-block col-md-1">{reading.book.pages}</td>
                <td className="col-3 col-md-2">{reading.date}</td>
                <td className="d-none d-md-block col-md-1"><Link to='#' onClick={() => this.delete(reading)}>Delete</Link></td>
            </tr>
        )
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchReadings, deleteReading, fetchBookTitles, addReading}, dispatch);
}

function mapStateToProps({book, reading}) {
    return {book, reading};
}

export default connect(mapStateToProps, mapDispatchToProps)(ReadingList);