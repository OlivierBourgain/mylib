import React from 'react';

function Tag(props) {
    const tag = props.tag;

    const style = {
        backgroundColor: tag.backgroundColor,
        color: tag.color,
        borderColor: tag.borderColor
    };

    return <span className="tag" style={style}>{tag.text}</span>
}

export default Tag;