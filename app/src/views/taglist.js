import React, {Component} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import {Col, Container, Row} from 'reactstrap';
import {fetchTags} from "../actions/tag.action";
import Tag from "./tag";

class TagList extends Component {

    constructor(props) {
        super(props);
        if (!this.props.tag.list) this.props.fetchTags();
    }

    render() {
        const tag = this.props.tag;
        if (tag.error) {
            return (<Container>Something went wrong</Container>);
        }
        if (tag.pending) {
            return (<Container>Loading...</Container>);
        }

        return (
            <Container>
                <Row>
                    <Col>
                        <h3>Tag list</h3>
                    </Col>
                </Row>
                <Row>
                    <table className="table table-sm">
                        <thead>
                        <tr>
                            <th></th>
                            <th></th>
                            <th>Priority</th>
                            <th></th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        {tag.list && tag.list.map(t => this.displayTag(t))}
                        </tbody>
                    </table>
                </Row>
            </Container>);
    }

    displayTag = (tag) => {
        return <tr>
            <td><Tag tag={tag}/></td>
            <td>
                <span id="tag-color-A" className="tagcolor">a</span>{' '}
                <span id="tag-color-B" className="tagcolor">a</span>{' '}
                <span id="tag-color-C" className="tagcolor">a</span>{' '}
                <span id="tag-color-D" className="tagcolor">a</span>{' '}
                <span id="tag-color-E" className="tagcolor">a</span>{' '}
                <span id="tag-color-F" className="tagcolor">a</span>{' '}
                <span id="tag-color-G" className="tagcolor">a</span>{' '}
                <span id="tag-color-H" className="tagcolor">a</span>{' '}
                <span id="tag-color-I" className="tagcolor">a</span>{' '}
                <span id="tag-color-J" className="tagcolor">a</span>{' '}
                <span id="tag-color-K" className="tagcolor">a</span>{' '}
                <span id="tag-color-L" className="tagcolor">a</span>{' '}
                <span id="tag-color-M" className="tagcolor">a</span>{' '}
                <span id="tag-color-N" className="tagcolor">a</span>{' '}
                <span id="tag-color-O" className="tagcolor">a</span>{' '}
                <span id="tag-color-P" className="tagcolor">a</span>{' '}
                <span id="tag-color-Q" className="tagcolor">a</span>{' '}
                <span id="tag-color-R" className="tagcolor">a</span>{' '}
                <span id="tag-color-S" className="tagcolor">a</span>{' '}
                <span id="tag-color-T" className="tagcolor">a</span>{' '}
                <span id="tag-color-U" className="tagcolor">a</span>{' '}
                <span id="tag-color-V" className="tagcolor">a</span>{' '}
                <span id="tag-color-W" className="tagcolor">a</span>{' '}
                <span id="tag-color-X" className="tagcolor">a</span>{' '}
            </td>
            <td>{tag.priority}</td>
            <td>{/* Delete link */}</td>
        </tr>
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchTags}, dispatch);
}

function mapStateToProps({tag}) {
    return {tag};
}

export default connect(mapStateToProps, mapDispatchToProps)(TagList);