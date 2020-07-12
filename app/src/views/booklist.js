import React, {Component} from 'react'
import {bindActionCreators} from 'redux'
import {connect} from 'react-redux'
import {Link} from 'react-router-dom'
import {Redirect} from 'react-router';
import {Button, Col, Container, Input, Row, Table} from 'reactstrap'

import Tag from './tag'
import {fetchBooks, lookup, exportcsv, rebuildindex} from '../actions/book.action'
import Pagination from "./tools/pagination";

class BookList extends Component {
    state = {
        page: 0,
        size: 100,
        activeFilter: '',
        term: '',
        discarded: false,
        sort: 'Updated',
        descending: true
    }

    componentDidMount() {
        this.updateList()
    }

    updateList = () => {
        const term = this.state.term;
        if (this.isIsbn(term) || this.isAsin(term)) {
            this.props.lookup(term);
            return;
        }

        this.setState({activeFilter: this.state.term})
        this.props.fetchBooks(
            this.state.page,
            this.state.size,
            this.state.term,
            this.state.discarded,
            this.state.sort,
            this.state.descending);
    }

    export = () => {
        // Call the export API to receive data
        this.props.exportcsv();
    }

    rebuildindex = () => {
        this.props.rebuildindex();
    }

    changeTerm = event => {
        this.setState({term: event.target.value});
    }

    changeSize = event => {
        this.setState({size: event.target.value}, this.updateList);
    }

    changeDiscarded = event => {
        this.setState({discarded: event.target.checked}, this.updateList);
    }

    changePage = page => {
        this.setState({page}, this.updateList);
    }

    changeSort = col => {
        if (this.state.sort === col) this.setState({descending: !this.state.descending}, this.updateList)
        else this.setState({sort: col}, this.updateList)
    }

    clearFilter = () => {
        this.setState({term: ''}, this.updateList)
    }

    /** Check if the search term looks like an ISBN number */
    isIsbn = s => {
        if (!s) return false;
        const isbnRegex = /^(97(8|9))?\d{9}(\d|X)$/
        return s.replace(/[-\s]/g, '').match(isbnRegex) !== null;
    }

    /** Check if the search term looks like an ASIN number */
    isAsin = s => {
        if (!s) return false;
        return s.toLowerCase().startsWith("asin:")
    }


    render() {
        const {book} = this.props;
        if (book.redirectTo) {
            return (<Redirect to={book.redirectTo}/>)
        }
        if (book.error) {
            return (<Container>Something went wrong</Container>);
        }
        if (book.pending) {
            return (<Container>Loading...</Container>);
        }

        const list = this.props.book.list;
        return (<Container>
            <Row className="filter-bar">
                <Col className="col-12 col-md-8">
                    <Row>
                        <Col className="col-8"><Input type="text" value={this.state.term} onChange={this.changeTerm}/></Col>
                        <Button type="button" color="success" className="col-2" onClick={this.updateList}>Submit</Button>
                    </Row>
                </Col>
                <Col className="col-12 col-md-4">
                    <Row>
                        <button className="link-button col-12" onClick={() => this.export()}>Export CSV</button>
                    </Row>
                    <Row>
                        <button className="link-button col-12" onClick={() => this.rebuildindex()}>Rebuild index</button>
                    </Row>
                    <Row>
                        <label className="col-12" htmlFor="showDisc">
                            <input type="checkbox" id="showDisc" checked={this.state.discarded}
                                   onChange={this.changeDiscarded}/> Show discarded
                        </label>
                    </Row>
                </Col>
            </Row>
            {list && <>
                <Row>
                    <Col className="col-12 col-md-4" >
                        {list.totalElements} results
                        {list.totalPages > 1 && <span>
                            , showing page {list.number + 1} of {list.totalPages}
                        </span>}
                        {this.state.activeFilter && <span>
                            , filter on <strong>{this.state.activeFilter}</strong>{' '}
                            (<button id="removeFilter" onClick={this.clearFilter}
                                     className="link-button">remove</button>)
                        </span>}
                    </Col>
                    <Col className="col-12 col-md-5">
                        <Pagination path="/books" page={list.number} nbPages={list.totalPages}
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
                        <span> books</span>
                    </Col>
                </Row>
                {list.numberOfElements === 0 && <Row>
                    <Col>No book found</Col>
                </Row>}
                {list.numberOfElements > 0 && <Row className="mt-2">
                    <Table bordered striped size="sm">
                        <thead>
                        <tr className="row">
                            <th className="col-6 col-md-5"><Link to='/books'
                                                        onClick={() => this.changeSort('Title')}>Title</Link></th>
                            <th className="col-4 col-md-3"><Link to='/books'
                                                        onClick={() => this.changeSort('Author')}>Author</Link></th>
                            <th className="col-2 col-md-1"><Link to='/books'
                                                        onClick={() => this.changeSort('Pages')}>Pages</Link></th>
                            <th className="d-none d-md-block col-3"><Link to='/books' onClick={() => this.changeSort('Tags')}>Tags</Link>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        {list.content && list.content.map(book => this.renderBook(book))}
                        </tbody>
                    </Table>
                </Row>}
            </>}
        </Container>);
    }

    renderBook(book) {
        return (
            <tr className="row" key={`book-${book.id}`}>
                <td className="col-6 col-md-5">
                    <Link to={`/book/${book.id}`}>{book.title}</Link>
                    {book.status === 'DISCARDED' && <span>{' '}(discarded)</span>}
                </td>
                <td className="col-4 col-md-3">{book.author}</td>
                <td className="col-2 col-md-1">{book.pages}</td>
                <td className="col-12 col-md-3">
                    {book.tags.map(tag => <Tag key={`tag-${tag.id}`} tag={tag}/>)}
                </td>
            </tr>);
    }


}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchBooks, lookup, exportcsv, rebuildindex}, dispatch);
}

function mapStateToProps({book}) {
    return {book};
}

export default connect(mapStateToProps, mapDispatchToProps)(BookList);