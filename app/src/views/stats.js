import React, {Component} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import {Col, Container, Row} from 'reactstrap';

import BarChart from "./tools/barchart";

import {fetchStats} from '../actions/stat.action'

class Stats extends Component {
    state = {
        year: 2020
    }

    constructor(props) {
        super(props);
        this.update();
    }

    update = () => { this.props.fetchStats(this.state.year); }

    changeYear = (event) => {
        this.setState({year: event.target.value}, this.update)

    }

    render() {
        if (!this.props.stats) return null;
        return (
            <Container>
                <Row>
                    <Col className="col-2">
                        <label htmlFor="year">
                            Year <select id="year" value={this.state.year} onChange={this.changeYear}>
                            <option value={""} >All</option>
                            <option value={2020} >2020</option>
                            <option value={2019} >2019</option>
                            <option value={2018} >2018</option>
                            <option value={2017} >2017</option>
                        </select>
                        </label>
                    </Col>
                </Row>
                <Row>
                    <Col className="stat-box col-3">
                        <div id="booksbytag" className="inner-stat-box">
                            <i className="fa fa-expand" aria-hidden="true"></i>
                            <h4>Top tags (books)</h4>
                            <BarChart type='horizontalBar' data={this.props.stats.booksByTag}/>
                        </div>
                    </Col>
                    <Col className="col-3 stat-box">
                        <div id="pagesbytag" className="inner-stat-box">
                            <i className="fa fa-expand" aria-hidden="true"></i>
                            <h4>Top tags (pages)</h4>
                            <BarChart type='horizontalBar' data={this.props.stats.pagesByTag}/>
                        </div>
                    </Col>
                    <Col className="col-3 stat-box">
                        <div id="booksbyauthor" className="inner-stat-box">
                            <i className="fa fa-expand" aria-hidden="true"></i>
                            <h4>Top authors (books)</h4>
                            <BarChart type='horizontalBar' data={this.props.stats.booksByAuthor}/>
                        </div>
                    </Col>
                    <Col className="col-3 stat-box">
                        <div id="pagesbyauthor" className="inner-stat-box">
                            <i className="fa fa-expand" aria-hidden="true"></i>
                            <h4>Top authors (pages)</h4>
                            <BarChart type='horizontalBar' data={this.props.stats.pagesByAuthor}/>
                        </div>
                    </Col>
                </Row>
                <Row>
                    <Col className="col-6 stat-box">
                        <div id="booksbymonth" className="inner-stat-box">
                            <h4>Books per month</h4>
                            <BarChart type='bar' data={this.props.stats.booksByMonth}/>
                        </div>
                    </Col>
                    <Col className="col-6 stat-box">
                        <div id="pagesbymonth" className="inner-stat-box">
                            <h4>Pages per month</h4>
                            <BarChart type='bar' data={this.props.stats.pagesByMonth}/>
                        </div>
                    </Col>
                </Row>
                <Row>
                    <Col className="col-6 stat-box">
                        <div id="booksbyyear" className="inner-stat-box">
                            <h4>Books per year</h4>
                            <BarChart type='bar' data={this.props.stats.booksByYear}/>
                        </div>
                    </Col>
                    <Col className="col-6 stat-box">
                        <div id="pagesbyyear" className="inner-stat-box">
                            <h4>Pages per year</h4>
                            <BarChart type='bar' data={this.props.stats.pagesByYear}/>
                        </div>
                    </Col>
                </Row>
            </Container>);
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchStats}, dispatch);
}

function mapStateToProps(state) {
    return {stats: state.stat.list};
}

export default connect(mapStateToProps, mapDispatchToProps)(Stats);