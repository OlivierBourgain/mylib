import React from 'react';
import {
    Collapse,
    DropdownItem,
    DropdownMenu,
    DropdownToggle,
    Nav,
    Navbar,
    NavbarToggler,
    NavItem,
    NavLink,
    UncontrolledDropdown
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
                            <UncontrolledDropdown nav inNavbar>
                                <DropdownToggle nav caret>
                                    More...
                                </DropdownToggle>
                                <DropdownMenu right>
                                    <DropdownItem>
                                        Rebuild search index
                                    </DropdownItem>
                                </DropdownMenu>
                            </UncontrolledDropdown>
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