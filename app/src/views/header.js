import React from 'react';
import {
    Collapse,
    Nav,
    Navbar,
    NavbarToggler,
    NavItem,
    NavLink
} from 'reactstrap';
import { Link } from "react-router-dom";

class Header extends React.Component {
    constructor(props) {
        super(props);

        this.toggle = this.toggle.bind(this);
        this.state = {
            isOpen: false
        };
    }

    toggle() {
        this.setState({
            isOpen: !this.state.isOpen
        });
    }

    render() {
        return (
            <div>
                <Navbar color="light" light expand="sm">
                    <NavbarToggler onClick={this.toggle}/>
                    <Collapse isOpen={this.state.isOpen} navbar>
                        <Nav navbar>
                            <NavItem>
                                <Link className="nav-link" to="/books">Books</Link>
                            </NavItem>
                            <NavItem>
                                <Link className="nav-link" to="/readings">Reading list</Link>
                            </NavItem>
                            <NavItem>
                                <Link className="nav-link" to="/stats">Stats</Link>
                            </NavItem>
                            <NavItem>
                                <Link className="nav-link" to="/tags">Tags</Link>
                            </NavItem>
                            <NavItem>
                                <NavLink href="">Logout</NavLink>
                            </NavItem>
                        </Nav>
                    </Collapse>
                </Navbar>
            </div>
        );
    }
}


export default Header;