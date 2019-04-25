import React, {Component} from 'react';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import {Col, Container, Row} from 'reactstrap';
import {fetchTags, updateTag} from "../actions/tag.action";
import Tag from "./tag";


const colors = [
    {id:'color-A', backgroundColor:"#E7E7E7", color:"#464646", borderColor:"#464646;"}, /* Mercury */
    {id:'color-B', backgroundColor:"#B6CFF5", color:"#0D3472", borderColor:"#0D3472;"}, /* Perano */
    {id:'color-C', backgroundColor:"#98D7E4", color:"#0D3B44", borderColor:"#0D3B44;"}, /* Regent St blue */
    {id:'color-D', backgroundColor:"#E3D7FF", color:"#3D188E", borderColor:"#3D188E;"}, /* Fog */
    {id:'color-E', backgroundColor:"#FBD3E0", color:"#711A36", borderColor:"#711A36;"}, /* classic rose */
    {id:'color-F', backgroundColor:"#F2B2A8", color:"#8A1C0A", borderColor:"#8A1C0A;"}, /* Mandys pink */
    {id:'color-G', backgroundColor:"#C2C2C2", color:"#FFFFFF", borderColor:"#464646;"}, /* Silver */
    {id:'color-H', backgroundColor:"#4986E7", color:"#FFFFFF", borderColor:"#0D3472;"}, /* Royal blue */
    {id:'color-I', backgroundColor:"#2DA2BB", color:"#FFFFFF", borderColor:"#0D3B44;"}, /* Curious blue */
    {id:'color-J', backgroundColor:"#B99AFF", color:"#FFFFFF", borderColor:"#3D188E;"}, /* Mauve */
    {id:'color-K', backgroundColor:"#F691B2", color:"#994A64", borderColor:"#994A64;"}, /* Persian pink */
    {id:'color-L', backgroundColor:"#FB4C2F", color:"#FFFFFF", borderColor:"#8A1C0A;"}, /* Red orange */
    {id:'color-M', backgroundColor:"#FFC8AF", color:"#7A2E0B", borderColor:"#7A2E0B;"}, /* Wax flower */
    {id:'color-N', backgroundColor:"#FFDEB5", color:"#7A4706", borderColor:"#7A4706;"}, /* Frangipani */
    {id:'color-O', backgroundColor:"#FBE983", color:"#594C05", borderColor:"#594C05;"}, /* Sweet corn */
    {id:'color-P', backgroundColor:"#FDEDC1", color:"#684E07", borderColor:"#684E07;"}, /* Beeswax */
    {id:'color-Q', backgroundColor:"#B3EFD3", color:"#0B4F30", borderColor:"#0B4F30;"}, /* Magic mint  */
    {id:'color-R', backgroundColor:"#A2DCC1", color:"#04502E", borderColor:"#04502E;"}, /* Fringy flower*/
    {id:'color-S', backgroundColor:"#FF7537", color:"#FFFFFF", borderColor:"#8A1C0A;"}, /* Burning orange */
    {id:'color-T', backgroundColor:"#FFAD46", color:"#FFFFFF", borderColor:"#8A1C0A;"}, /* Yellow orange*/
    {id:'color-U', backgroundColor:"#EBDBDE", color:"#662E37", borderColor:"#662E37;"}, /* Soft peach */
    {id:'color-V', backgroundColor:"#CCA6AC", color:"#FFFFFF", borderColor:"#662E37;"}, /* Clam shell */
    {id:'color-W', backgroundColor:"#42D692", color:"#094228", borderColor:"#094228;"}, /* Shamrock */
    {id:'color-X', backgroundColor:"#16A765", color:"#FFFFFF", borderColor:"#094228;"}, /* Mountain meadow */
];


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
        return <tr key={tag.id}>
            <td><Tag tag={tag}/></td>
            <td>
                {colors.map(c => this.colorChanger(c, tag))}
            </td>
            <td>{tag.priority}</td>
            <td>{/* Delete link */}</td>
        </tr>
    }

    colorChanger = (c, tag) => (
        <React.Fragment key={c.id}>
            <span className="tagcolor"
                  style={{
                      backgroundColor: c.backgroundColor,
                      color: c.color,
                      borderColor: c.border
                  }}
                  onClick={this.changeColor.bind(this, tag, c)}
            >a</span>{' '}
        </React.Fragment>
    )

    changeColor = (tag, color) => {
        console.log(tag, color)
        updateTag(tag)
    }


}

function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchTags}, dispatch);
}

function mapStateToProps({tag}) {
    return {tag};
}

export default connect(mapStateToProps, mapDispatchToProps)(TagList);