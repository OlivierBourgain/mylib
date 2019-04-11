import React, {Component} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import {Col, Container, Row} from 'reactstrap';

class Stats extends Component {
    render() {
        return (
            <Container>
                <Row>
                    <Col>
                        <h3>Stats</h3>
                    </Col>
                </Row>
            </Container>);
    }
}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({}, dispatch);
}

function mapStateToProps(state) {
    return {};
}

export default connect(mapStateToProps, mapDispatchToProps)(Stats);