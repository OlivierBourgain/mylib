import React, {Component} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import {Col, Container, Row, Table} from 'reactstrap';
import {fetchReadings, deleteReading} from '../actions/reading.action';
import {Link} from "react-router-dom";
import Pagination from "./tools/pagination";

class ReadingList extends Component {
    state = {
        page: 0,
        size: 100,
        sort: 'date',
        descending: true
    }

    componentDidMount() {
        this.updateList()
    }

    updateList = () => {
        this.props.fetchReadings(
            this.state.page,
            this.state.size,
            this.state.sort,
            this.state.descending);
    }

    changeSize = event => {
        this.setState({ size : event.target.value }, this.updateList);
    }

     changePage = page => {
        this.setState({ page }, this.updateList);
    }

    changeSort = col => {
        if (this.state.sort === col) this.setState({descending: !this.state.descending}, this.updateList)
        else this.setState({sort: col}, this.updateList)
    }

    delete = (reading) => {
        console.log("deleting", reading);
        this.props.deleteReading(reading);
    }

    render() {
        const reading = this.props.reading;

        if (reading.error) {
            return (<Container>Something went wrong</Container>);
        }
        if (reading.pending) {
            return (<Container>Loading...</Container>);
        }

        const list = this.props.reading.list;
        return (<Container>
            <Row id="readings-list">
                <h3>Your reading history</h3>
                {!list && <Row className="col-12">
                    <Col>No book read</Col>
                </Row>}
                {list && <>
                    <Row className="col-12">
                        <Col id="list-header-summary" className="col-4">
                            {list.totalElements} results
                            {list.totalPages > 1 && <span>
                            , showing page {list.number + 1} of {list.totalPages}
                        </span>}
                        </Col>
                        <Col className="col-5">
                            <Pagination path="/readings" page={list.number} nbPages={list.totalPages} updatePage={this.changePage}/>
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
                            <th className="col-5"><Link to='/readings' onClick={() => this.changeSort('Book.title')}>Title</Link></th>
                            <th className="col-3"><Link to='/readings' onClick={() => this.changeSort('Book.author')}>Author</Link></th>
                            <th className="col-1"><Link to='/readings' onClick={() => this.changeSort('Book.pages')}>Pages</Link></th>
                            <th className="col-2"><Link to='/readings' onClick={() => this.changeSort('Date')}>Date read</Link></th>
                            <th className="col-1"></th>
                        </tr>
                        </thead>
                        <tbody>
                        {list.content && list.content.map(it => this.renderReading(it))}
                        </tbody>
                    </Table>
                </>}
            </Row>
        </Container>);
    }

    renderReading = (reading) => {
        return (
            <tr className="row" key={`reading-${reading.id}`}>
                <td className="col-5">
                    <Link to={`/book/${reading.book.id}`}>{reading.book.title}</Link>
                </td>
                <td className="col-3">{reading.book.author}</td>
                <td className="col-1">{reading.book.pages}</td>
                <td className="col-2">{reading.date}</td>
                <td className="col-1"><Link to='#' onClick={() => this.delete(reading)}>Delete</Link></td>
            </tr>
        )
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchReadings, deleteReading}, dispatch);
}

function mapStateToProps({reading}) {
    return {reading};
}

export default connect(mapStateToProps, mapDispatchToProps)(ReadingList);