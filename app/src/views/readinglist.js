import React, {Component} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import {Col, Container, Row, Table} from 'reactstrap';
import {fetchReadings} from '../actions/reading.action';
import {Link} from "react-router-dom";

class ReadingList extends Component {

    constructor(props) {
        super(props);
        if (!this.props.reading.list) this.props.fetchReadings();
    }

    render() {
        const reading = this.props.reading;
        if (reading.error) {
            return (<Container>Something went wrong</Container>);
        }
        if (reading.pending) {
            return (<Container>Loading...</Container>);
        }
        return (<Container>
            <Row id="readings-list">
                <h3>Your reading history</h3>
                {!reading.list && <Row className="col-12">
                    <Col>No book read</Col>
                </Row>}
                {reading.list && <>
                    <Row className="col-12">
                        <Col>{reading.list.length} results</Col>
                    </Row>
                    <Table bordered striped size="sm">
                        <thead>
                        <tr className="row">
                            <th className="col-5">Title</th>
                            <th className="col-3">Author</th>
                            <th className="col-1">Pages</th>
                            <th className="col-2">Date read</th>
                            <th className="col-1"></th>
                        </tr>
                        </thead>
                        <tbody>
                        {reading.list && reading.list.map(it => this.doItem(it))}
                        </tbody>
                    </Table>
                </>}
            </Row>
        </Container>);
    }

    doItem = (it) => {
        return (
            <tr className="row" key={`reading-${it.id}`}>
                <td className="col-5">
                    <Link to={`/book/${it.book.id}`}>{it.book.title}</Link>
                </td>
                <td className="col-3">{it.book.author}</td>
                <td className="col-1">{it.book.pages}</td>
                <td className="col-2">{it.date}</td>
                <td className="col-1"></td>
            </tr>
        )
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchReadings}, dispatch);
}

function mapStateToProps({reading}) {
    return {reading};
}

export default connect(mapStateToProps, mapDispatchToProps)(ReadingList);