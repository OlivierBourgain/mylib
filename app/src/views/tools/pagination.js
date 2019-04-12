import React from 'react';
import {Link} from "react-router-dom";

function Pagination(props) {
    const page = props.page;
    const nbPages = props.nbPages;

    if (nbPages === 1) return null;

    var start = page <= 4 ? 0 : page - 3;
    var end = page <= 4 ? 7 : page < nbPages - 5 ? page + 3 : nbPages;

    return <ul className="pagination pagination-sm">
        <li className="page-item" key="page-start">
            <Link className="page-link" to="/books?page=1">&laquo;</Link>
        </li>
        {start > 0 && <li className="page-item disabled" key="page-break-start">
            <span className="page-link">...</span>
        </li>}
        {
            [...Array(end - start).keys()].map(i =>
            <li className={"page-item " + (i === page ? 'active':'')} key={`page-${i+1}`}>
                <Link className="page-link" to={`/books?page=${i+1}`}>{i + 1}</Link>
            </li>
        )}
        <li className="page-item disabled" key="page-break-end">
            <span className="page-link">...</span>
        </li>
        <li className="page-item" key="page-end">
            <Link className="page-link" to={`/books?page=${nbPages}`}>&raquo;</Link>
        </li>
    </ul>;
}

export default Pagination;