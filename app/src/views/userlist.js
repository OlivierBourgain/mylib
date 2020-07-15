import React, {Component} from 'react'
import {bindActionCreators} from 'redux'
import {connect} from 'react-redux'
import {Button, Col, Container, Input, Row} from 'reactstrap';

import {fetchUsers, createUser, deleteUser, updateRole} from '../actions/user.action';

class UserList extends Component {
    state = {email: undefined}

    constructor(props) {
        super(props);
        this.props.fetchUsers();
    }

    createUser = () => {
        if (this.state.email) this.props.createUser(this.state.email);
    }

    deleteUser = (user) => {
        if (window.confirm('Are you sure?')) this.props.deleteUser(user.id);
    }

    changeRole = (user, role) => {
        if (window.confirm('Are you sure?')) this.props.updateRole(user.id, role);
    }

    render() {
        const user = this.props.user;
        return (
            <Container>
                <Row>
                    <Col className="col-8">
                        <Input type="text" placeholder="input email address" value={this.state.email} onChange={(e) => this.setState({email: e.target.value})}/>
                    </Col>
                    <Button type="button" color="success" onClick={this.createUser}>Submit</Button>
                </Row>
                <Row className="mt-3">
                    <table className="table table-sm">
                        <thead>
                        <tr>
                            <th>Email</th>
                            <th>Role</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        {user.list && user.list.map(u => this.displayUser(u))}
                        </tbody>
                    </table>
                </Row>
            </Container>
        );
    }

    displayUser = (user) => {
        return <tr key={user.id}>
            <td>{user.email}</td>
            <td>{user.role}</td>
            <td>
                <button className="btn btn-sm btn-danger" onClick={() => this.deleteUser(user)}>delete</button>
                {user.role !== 'ADMIN' && <button className="ml-2 btn btn-sm btn-info" onClick={() => this.changeRole(user, 'ADMIN')}>Make admin</button>}
                {user.role === 'ADMIN'  && <button className="ml-2 btn btn-sm btn-info" onClick={() => this.changeRole(user, 'USER')}>Make user</button>}
            </td>
        </tr>
    }

}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchUsers, createUser, deleteUser, updateRole}, dispatch);
}

function mapStateToProps({user}) {
    return {user};
}

export default connect(mapStateToProps, mapDispatchToProps)(UserList);