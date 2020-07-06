import React, {Component} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import {Col, Container, Row} from 'reactstrap';

import BarChart from "./tools/barchart";

import {fetchStats, fetchStatsDetail} from '../actions/stat.action'
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import { faExpandAlt, faTimes } from '@fortawesome/free-solid-svg-icons'

class Stats extends Component {
    state = {
        year: 2020,
        showDetail: false,
        titleDetail: ''
    }

    constructor(props) {
        super(props);
        this.update();
    }

    update = () => { this.props.fetchStats(this.state.year); }

    changeYear = (event) => {
        this.setState({year: event.target.value, showDetail:false}, this.update)
    }

    showDetail = (statName) => {
        this.props.fetchStatsDetail(statName, this.state.year);
        switch(statName) {
            case 'booksbytag':
                this.setState({titleDetail:'Top tags (books)', cssDetail: statName});
                break;
            case 'pagesbytag':
                this.setState({titleDetail:'Top tags (pages)', cssDetail: statName});
                break;
            case 'booksbyauthor':
                this.setState({titleDetail:'Top authors (books)', cssDetail: statName});
                break;
            case 'pagesbyauthor':
                this.setState({titleDetail:'Top authors (pages)', cssDetail: statName});
                break;
            default: break;
        }
        this.setState({showDetail: true})
    };

    closeDetail = () => {
        this.setState({showDetail: false})
    };

    render() {
        if (!this.props.list) return null;
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
                {this.state.showDetail && !this.props.detailpending && <Row>
                    <Col className="stat-box col-12">
                        <div id="statdetail" className={"inner-stat-box " + this.state.cssDetail}>
                            <FontAwesomeIcon icon={faTimes} onClick={() => this.closeDetail()}/>
                            <h4>{this.state.titleDetail}</h4>
                            <BarChart width={1000} height={231} type='bar' data={this.props.detail}/>
                        </div>
                    </Col>
                </Row>
                }
                {(!this.state.showDetail || this.props.detailpending) && <Row>
                    <Col className="col-6 col-md-3 stat-box">
                        <div className="inner-stat-box booksbytag">
                            <FontAwesomeIcon className="d-none d-md-block" icon={faExpandAlt} onClick={() => this.showDetail('booksbytag')}/>
                            <h4>Top tags (books)</h4>
                            <BarChart type='horizontalBar' width={100} height={100} data={this.props.list.booksByTag}/>
                        </div>
                    </Col>
                    <Col className="col-6 col-md-3 stat-box">
                        <div id="pagesbytag" className="inner-stat-box pagesbytag">
                            <FontAwesomeIcon className="d-none d-md-block" icon={faExpandAlt} onClick={() => this.showDetail('pagesbytag')}/>
                            <h4>Top tags (pages)</h4>
                            <BarChart type='horizontalBar' width={100} height={100} data={this.props.list.pagesByTag}/>
                        </div>
                    </Col>
                    <Col className="col-6 col-md-3 stat-box">
                        <div className="inner-stat-box booksbyauthor">
                            <FontAwesomeIcon className="d-none d-md-block" icon={faExpandAlt} onClick={() => this.showDetail('booksbyauthor')}/>
                            <h4>Top authors (books)</h4>
                            <BarChart type='horizontalBar' width={100} height={100} data={this.props.list.booksByAuthor}/>
                        </div>
                    </Col>
                    <Col className="col-6 col-md-3 stat-box">
                        <div className="inner-stat-box pagesbyauthor">
                            <FontAwesomeIcon className="d-none d-md-block" icon={faExpandAlt} onClick={() => this.showDetail('pagesbyauthor')}/>
                            <h4>Top authors (pages)</h4>
                            <BarChart type='horizontalBar' width={100} height={100} data={this.props.list.pagesByAuthor}/>
                        </div>
                    </Col>
                </Row>
                }
                <Row>
                    <Col className="col-12 col-md-6 stat-box">
                        <div className="inner-stat-box booksbymonth">
                            <h4>Books per month</h4>
                            <BarChart type='bar' width={100} height={30} data={this.props.list.booksByMonth}/>
                        </div>
                    </Col>
                    <Col className="col-12 col-md-6 stat-box">
                        <div className="inner-stat-box pagesbymonth">
                            <h4>Pages per month</h4>
                            <BarChart type='bar' width={100} height={30} data={this.props.list.pagesByMonth}/>
                        </div>
                    </Col>
                </Row>
                <Row>
                    <Col className="col-12 col-md-6 stat-box">
                        <div className="inner-stat-box booksbyyear">
                            <h4>Books per year</h4>
                            <BarChart type='bar' width={100} height={30} data={this.props.list.booksByYear}/>
                        </div>
                    </Col>
                    <Col className="col-12 col-md-6 stat-box">
                        <div className="inner-stat-box pagesbyyear">
                            <h4>Pages per year</h4>
                            <BarChart type='bar' width={100} height={30} data={this.props.list.pagesByYear}/>
                        </div>
                    </Col>
                </Row>
            </Container>);
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchStats, fetchStatsDetail}, dispatch);
}

function mapStateToProps(state) {
    return state.stat;
}

export default connect(mapStateToProps, mapDispatchToProps)(Stats);